package com.example.food_recipe.repository;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.Timestamp;
import java.util.Date;
import java.util.Calendar;

/**
 * [추가] 유통기한 관련 데이터를 Firestore와 통신하는 역할을 담당하는 클래스입니다.
 */
public class ExpirationRepository {

    private static final String COLLECTION_NAME = "expiringIngredients";
    private final FirebaseFirestore db;

    public ExpirationRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    /**
     * [추가] 유통기한이 3일 이내로 임박했고, 아직 알림이 발송되지 않은 재료 목록을 가져오는 쿼리를 반환합니다.
     * @return Firestore 쿼리 객체
     */
    public Query getExpiringIngredientsQuery() {
        // 1. 현재 시간으로부터 3일 후의 시간을 계산합니다.
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, 3);
        Date threeDaysFromNow = calendar.getTime();
        Timestamp threeDaysFromNowTimestamp = new Timestamp(threeDaysFromNow);

        // 2. 현재 시간도 계산합니다. (유통기한이 이미 지난 것도 포함하기 위함)
        Date now = Calendar.getInstance().getTime();
        Timestamp nowTimestamp = new Timestamp(now);

        // 3. 쿼리를 생성합니다.
        // - 'expirationDate'가 "현재 시간"보다 크거나 같고 "3일 후"보다 작거나 같은,
        // - 'notificationStatus'가 "PENDING"인 문서를 찾습니다.
        return db.collection(COLLECTION_NAME)
                .whereGreaterThanOrEqualTo("expirationDate", nowTimestamp)
                .whereLessThanOrEqualTo("expirationDate", threeDaysFromNowTimestamp)
                .whereEqualTo("notificationStatus", "PENDING");
    }

    /**
     * [추가] 특정 재료 문서의 'notificationStatus' 필드를 "SENT"로 업데이트하는 Task를 반환합니다.
     * @param documentId 업데이트할 문서의 ID
     * @return Firestore 업데이트 작업 Task
     */
    public Task<Void> updateNotificationStatus(String documentId) {
        return db.collection(COLLECTION_NAME).document(documentId)
                .update("notificationStatus", "SENT");
    }
}
