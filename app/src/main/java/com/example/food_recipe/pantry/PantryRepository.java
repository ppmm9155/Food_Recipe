package com.example.food_recipe.pantry;

import android.util.Log;
import com.example.food_recipe.model.PantryItem;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * [추가] Pantry(냉장고) 관련 데이터 처리를 담당하는 Repository 클래스 (싱글톤)
 * MVP 패턴의 Model 파트에 해당합니다.
 */
public class PantryRepository {

    // [추가] 싱글톤 인스턴스
    private static PantryRepository instance;

    // [추가] Firebase 인증 및 Firestore 인스턴스
    private final FirebaseAuth mAuth;
    private final FirebaseFirestore db;
    private static final String TAG = "PantryRepository"; // [추가] 로그 태그

    // [추가] private 생성자 (싱글톤)
    private PantryRepository() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    /**
     * [추가] PantryRepository의 싱글톤 인스턴스를 반환하는 정적 메서드
     */
    public static synchronized PantryRepository getInstance() {
        if (instance == null) {
            instance = new PantryRepository();
        }
        return instance;
    }

    /**
     * [변경] 새로운 재료 아이템을 'myIngredients' 배열에 추가하는 메서드로 수정
     * @param newItem 추가할 재료 아이템
     */
    public void addPantryItem(PantryItem newItem) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "Error: User not signed in! Cannot add item.");
            return;
        }

        // [변경] users/{uid} 문서의 myIngredients 필드에 새로운 재료를 배열 원소로 추가합니다.
        // [변경] 이 방식은 LoginModel, JoinModel과의 데이터 구조 일관성을 완벽하게 유지합니다.
        db.collection("users").document(currentUser.getUid())
                .update("myIngredients", FieldValue.arrayUnion(newItem))
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Pantry item successfully added to array: " + newItem.getName());
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error updating pantry array", e);
                });
    }

    /**
     * [추가] 특정 재료 아이템을 'myIngredients' 배열에서 삭제하는 메서드
     * @param itemToRemove 삭제할 재료 아이템
     */
    public void deletePantryItem(PantryItem itemToRemove) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "Error: User not signed in! Cannot remove item.");
            return;
        }

        // [추가] users/{uid} 문서의 myIngredients 필드에서 특정 재료 객체를 배열 원소에서 제거합니다.
        // [추가] 이 방식은 Firestore의 원자적 연산을 사용하며, addPantryItem과의 일관성을 유지합니다.
        db.collection("users").document(currentUser.getUid())
                .update("myIngredients", FieldValue.arrayRemove(itemToRemove))
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Pantry item successfully removed from array: " + itemToRemove.getName());
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error updating pantry array for removal", e);
                });
    }

    /**
     * [추가] Firestore 비동기 호출 결과를 전달하기 위한 콜백 인터페이스
     */
    public interface PantryLoadCallback {
        void onPantryLoaded(List<PantryItem> pantryItems);
        void onError(String message);
    }

    /**
     * [추가] Firestore에서 현재 사용자의 모든 재료 목록을 가져오는 메서드
     * @param callback 결과를 전달받을 콜백 객체
     */
    public void getPantryItems(final PantryLoadCallback callback) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            callback.onError("사용자 인증 정보가 없습니다.");
            return;
        }

        db.collection("users").document(currentUser.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // [추가] myIngredients 필드를 List<Map<String, Object>> 형태로 가져옵니다.
                        List<Map<String, Object>> itemsMap = (List<Map<String, Object>>) documentSnapshot.get("myIngredients");
                        if (itemsMap != null) {
                            List<PantryItem> pantryItems = new ArrayList<>();
                            // [추가] Map을 순회하며 PantryItem 객체로 변환합니다.
                            for (Map<String, Object> itemMap : itemsMap) {
                                // [추가] Firestore의 Timestamp를 Java의 Date 객체로 변환합니다.
                                Timestamp timestamp = (Timestamp) itemMap.get("expirationDate");
                                Date expirationDate = timestamp != null ? timestamp.toDate() : new Date();

                                PantryItem item = new PantryItem(
                                        (String) itemMap.get("id"),
                                        (String) itemMap.get("name"),
                                        (String) itemMap.get("category"),
                                        ((Number) itemMap.get("quantity")).doubleValue(),
                                        (String) itemMap.get("unit"),
                                        (String) itemMap.get("storage"),
                                        expirationDate
                                );
                                pantryItems.add(item);
                            }
                            callback.onPantryLoaded(pantryItems);
                        } else {
                            // [추가] myIngredients 필드가 null이면 빈 리스트를 전달합니다.
                            callback.onPantryLoaded(new ArrayList<>());
                        }
                    } else {
                        // [추가] 사용자 문서가 없으면 빈 리스트를 전달합니다.
                        callback.onPantryLoaded(new ArrayList<>());
                    }
                })
                .addOnFailureListener(e -> {
                    // [추가] 데이터 로드 실패 시 에러 메시지를 전달합니다.
                    Log.e(TAG, "Error getting pantry items", e);
                    callback.onError("재료를 불러오는 데 실패했습니다: " + e.getMessage());
                });
    }
}
