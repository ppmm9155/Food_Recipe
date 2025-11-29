/**
 * Firebase 및 외부 라이브러리에서 필요한 기능들을 가져옵니다.
 */
import {onDocumentUpdated} from "firebase-functions/v2/firestore";
import * as logger from "firebase-functions/logger";
import algoliasearch from "algoliasearch";
// [추가] Firestore 데이터베이스 접근을 위한 Firebase Admin SDK를 가져옵니다.
import * as admin from "firebase-admin";


// --- Algolia 클라이언트 초기화 (최신 .env 방식) ---
// .env.foodrecipe-dfc6e 파일에 정의된 환경 변수를 직접 사용합니다.
const ALGOLIA_APP_ID = process.env.ALGOLIA_APP_ID!;
const ALGOLIA_ADMIN_KEY = process.env.ALGOLIA_ADMIN_KEY!;

// Algolia 클라이언트를 생성합니다.
// .env 파일의 키를 바탕으로 클라이언트를 초기화합니다.
const algoliaClient = algoliasearch(ALGOLIA_APP_ID, ALGOLIA_ADMIN_KEY);

// 'recipes' 인덱스를 지정합니다.
const recipesIndex = algoliaClient.initIndex("recipes");

// [추가] Firebase Admin SDK를 초기화합니다. Cloud Functions 환경에서 자동으로 구성됩니다.
admin.initializeApp();


// --- Firebase Function 정의 ---
/**
 * 'recipes' 컬렉션의 문서가 '업데이트'될 때마다 자동으로 실행되는 함수입니다.
 */
export const syncRecommendCountToAlgolia = onDocumentUpdated(
  "recipes/{recipeId}",
  async (event) => {
    const {recipeId} = event.params;
    const beforeData = event.data?.before.data();
    const afterData = event.data?.after.data();

    // 데이터 유효성 검사
    if (!beforeData || !afterData || !("recommend_count" in afterData)) {
      logger.log(`[${recipeId}] 데이터 부족으로 동기화 건너뜁니다.`);
      return;
    }

    const beforeCount = beforeData.recommend_count;
    const afterCount = afterData.recommend_count;

    // recommend_count 변경 여부 확인
    if (beforeCount === afterCount) {
      logger.log(
        `[${recipeId}] recommend_count 변경 없음 (값: ${afterCount}).`
      );
      return;
    }

    // 변경 로그 기록
    logger.info(
      `[${recipeId}] recommend_count 변경: ${beforeCount} -> ${afterCount}.`,
      "Algolia 동기화 시작.",
    );

    const objectToUpdate = {
      objectID: recipeId,
      recommend_count: afterCount,
    };

    try {
      // Algolia에 부분 업데이트 요청
      const result = await recipesIndex.partialUpdateObject(objectToUpdate, {
        createIfNotExists: true,
      });
      logger.info(
        `[${recipeId}] Algolia 업데이트 성공. 작업 ID: ${result.taskID}`
      );
    } catch (error) {
      logger.error(`[${recipeId}] Algolia 업데이트 실패.`, error);
    }
  },
);

// --- 신규 함수 추가 ---

/**
 * [추가] 'users' 컬렉션의 문서가 업데이트될 때마다 자동으로 실행되는 함수입니다.
 * 'myIngredients' 배열의 변경사항을 감지하여 'expiringIngredients' 컬렉션과 동기화합니다.
 */
export const syncExpiringIngredients = onDocumentUpdated(
  "users/{uid}",
  async (event) => {
    // 1. 컨텍스트 및 데이터 추출
    const {uid} = event.params;
    const beforeData = event.data?.before.data();
    const afterData = event.data?.after.data();

    // 2. 변경 전/후의 myIngredients 배열을 안전하게 가져옵니다.
    const beforeIngredients = (beforeData?.myIngredients && Array.isArray(beforeData.myIngredients)) ? beforeData.myIngredients : [];
    const afterIngredients = (afterData?.myIngredients && Array.isArray(afterData.myIngredients)) ? afterData.myIngredients : [];

    // 3. 성능 최적화: 변경이 없으면 함수를 일찍 종료합니다.
    if (JSON.stringify(beforeIngredients) === JSON.stringify(afterIngredients)) {
      logger.log(`[${uid}] myIngredients 배열에 변경 사항이 없어 동기화를 건너뜁니다.`);
      return;
    }

    // 4. 비교를 위해 재료 배열을 Map으로 변환합니다 (Key: id, Value: PantryItem).
    const beforeMap = new Map(beforeIngredients.map((item: any) => [item.id, item]));
    const afterMap = new Map(afterIngredients.map((item: any) => [item.id, item]));

    // 5. 모든 비동기 Firestore 작업을 저장할 프로미스 배열을 준비합니다.
    const promises: Promise<any>[] = [];

    // 6. [삭제된 재료] 처리: beforeMap에만 있는 재료를 찾습니다.
    for (const [id, item] of beforeMap.entries()) {
      if (!afterMap.has(id)) {
        logger.info(`[${uid}] 재료 삭제 감지: ${item.name}. 'expiringIngredients'에서 삭제합니다.`);
        const deletePromise = admin.firestore().collection("expiringIngredients").doc(id).delete();
        promises.push(deletePromise);
      }
    }

    // 7. [추가/수정된 재료] 처리: afterMap의 모든 재료를 순회합니다.
    for (const [id, item] of afterMap.entries()) {
      const beforeItem = beforeMap.get(id);

      // 7-1. 변경 여부 판단
      const isNew = !beforeItem;
      let isModified = false;
      if (beforeItem) {
        const nameChanged = beforeItem.name !== item.name;
        const beforeDate = beforeItem.expirationDate; // Timestamp object or null
        const afterDate = item.expirationDate; // Timestamp object or null
        let dateChanged = false;
        if (beforeDate && afterDate) {
          // 둘 다 유효한 Timestamp면 isEqual로 비교
          dateChanged = !beforeDate.isEqual(afterDate);
        } else if (beforeDate || afterDate) {
          // 한쪽만 있거나 없으면(추가/삭제) 변경된 것으로 간주
          dateChanged = true;
        }
        isModified = nameChanged || dateChanged;
      }

      // 7-2. 변경된 경우에만 동기화 작업 수행
      if (isNew || isModified) {
        // 7-3. 유통기한이 있는 재료는 'expiringIngredients'에 추가/수정
        if (item.expirationDate) {
          logger.info(`[${uid}] 재료 추가/수정 감지: ${item.name}. 'expiringIngredients'에 반영합니다.`);
          const docToSet = {
            uid: uid,
            ingredientName: item.name,
            expirationDate: item.expirationDate,
            notificationStatus: "PENDING", // 항상 'PENDING'으로 초기화
          };
          const upsertPromise = admin.firestore().collection("expiringIngredients").doc(id).set(docToSet);
          promises.push(upsertPromise);
        } else {
          // 7-4. 유통기한이 없는 재료는 'expiringIngredients'에서 삭제
          logger.info(`[${uid}] 재료 '${item.name}'에 유통기한이 없어 'expiringIngredients'에서 삭제합니다.`);
          const deletePromise = admin.firestore().collection("expiringIngredients").doc(id).delete();
          promises.push(deletePromise);
        }
      }
    }

    // 8. 모든 동기화 작업을 한 번에 실행합니다.
    if (promises.length > 0) {
      await Promise.all(promises);
      logger.log(`[${uid}] 'expiringIngredients' 동기화 완료. 처리된 작업 수: ${promises.length}`);
    } else {
      logger.log(`[${uid}] 'expiringIngredients' 동기화할 내용이 없습니다.`);
    }
  },
);
