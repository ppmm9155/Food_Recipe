/**
 * Firebase 및 외부 라이브러리에서 필요한 기능들을 가져옵니다.
 */
import {onDocumentUpdated} from "firebase-functions/v2/firestore";
import * as logger from "firebase-functions/logger";
import algoliasearch from "algoliasearch";

// --- Algolia 클라이언트 초기화 (최신 .env 방식) ---
// .env.foodrecipe-dfc6e 파일에 정의된 환경 변수를 직접 사용합니다.
const ALGOLIA_APP_ID = process.env.ALGOLIA_APP_ID!;
const ALGOLIA_ADMIN_KEY = process.env.ALGOLIA_ADMIN_KEY!;

// Algolia 클라이언트를 생성합니다.
// .env 파일의 키를 바탕으로 클라이언트를 초기화합니다.
const algoliaClient = algoliasearch(ALGOLIA_APP_ID, ALGOLIA_ADMIN_KEY);

// 'recipes' 인덱스를 지정합니다.
const recipesIndex = algoliaClient.initIndex("recipes");


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
