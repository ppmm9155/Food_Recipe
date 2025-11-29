package com.example.food_recipe.repository;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.Timestamp;
import java.util.Date;
import java.util.Calendar;

/**
 * [수정] 유통기한 관련 데이터를 Firestore와 통신하는 역할을 담당하는 클래스입니다.
 * 이제 모든 쿼리에 사용자 UID를 포함하여, 특정 사용자의 데이터만 가져오도록 수정합니다.
 */
public class ExpirationRepository {

    private static final String COLLECTION_NAME = "expiringIngredients";
    private final FirebaseFirestore db;

    public ExpirationRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    /**
     * [수정] 특정 사용자의 유통기한 임박 재료 목록을 가져오는 쿼리를 반환합니다.
     * @param uid 조회할 사용자의 ID
     * @return Firestore 쿼리 객체
     */
    public Query getExpiringIngredientsQuery(String uid) {
        // 1. "3일 뒤" 날짜의 '하루의 끝' 시간을 계산합니다.
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, 3);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        Date threeDaysFromNow = calendar.getTime();
        Timestamp endOfThreeDaysFromNowTimestamp = new Timestamp(threeDaysFromNow);

        // 2. '오늘 날짜의 시작(0시 0분)' 시간을 계산합니다.
        Calendar todayStart = Calendar.getInstance();
        todayStart.set(Calendar.HOUR_OF_DAY, 0);
        todayStart.set(Calendar.MINUTE, 0);
        todayStart.set(Calendar.SECOND, 0);
        todayStart.set(Calendar.MILLISECOND, 0);
        Timestamp startOfTodayTimestamp = new Timestamp(todayStart.getTime());

        // 3. [수정] 쿼리 필터의 순서를 변경하여 Firestore가 더 명확하게 이해하도록 합니다.
        // (동일 조건 필터를 먼저 적용하고, 범위 필터를 나중에 적용)
        return db.collection(COLLECTION_NAME)
                .whereEqualTo("uid", uid)
                .whereEqualTo("notificationStatus", "PENDING")
                .whereGreaterThanOrEqualTo("expirationDate", startOfTodayTimestamp)
                .whereLessThanOrEqualTo("expirationDate", endOfThreeDaysFromNowTimestamp);
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
