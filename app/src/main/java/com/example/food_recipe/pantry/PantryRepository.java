package com.example.food_recipe.pantry;

import android.util.Log;
import com.example.food_recipe.model.PantryItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Pantry(냉장고) 관련 데이터 처리를 담당하는 Repository 클래스 (싱글톤)
 * MVP 패턴의 Model 파트에 해당합니다.
 */
public class PantryRepository {

    private static PantryRepository instance;
    private final FirebaseAuth mAuth;
    private final FirebaseFirestore db;
    private static final String TAG = "PantryRepository";

    private PantryRepository() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    public static synchronized PantryRepository getInstance() {
        if (instance == null) {
            instance = new PantryRepository();
        }
        return instance;
    }

    public void addPantryItem(PantryItem newItem) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "Error: User not signed in! Cannot add item.");
            return;
        }
        db.collection("users").document(currentUser.getUid())
                .update("myIngredients", FieldValue.arrayUnion(newItem))
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Pantry item successfully added to array: " + newItem.getName()))
                .addOnFailureListener(e -> Log.w(TAG, "Error updating pantry array", e));
    }

    public void deletePantryItem(PantryItem itemToRemove) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "Error: User not signed in! Cannot remove item.");
            return;
        }
        db.collection("users").document(currentUser.getUid())
                .update("myIngredients", FieldValue.arrayRemove(itemToRemove))
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Pantry item successfully removed from array: " + itemToRemove.getName()))
                .addOnFailureListener(e -> Log.w(TAG, "Error updating pantry array for removal", e));
    }

    public interface PantryLoadCallback {
        void onPantryLoaded(List<PantryItem> pantryItems);
        void onError(String message);
    }

    /**
     * [개선] Firestore에서 현재 사용자의 모든 재료 목록을 가져오는 메서드
     * [개선] 위험한 '수동 조립' 방식 대신, Firestore의 안전한 '자동 조립(POJO 매핑)' 방식으로 전면 교체합니다.
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
                        // [핵심 수정] 문서 전체를 UserData라는 임시 클래스로 자동 변환합니다.
                        // 이 클래스 안에 PantryItem 리스트가 포함되어 있습니다.
                        // 이 방식은 Firestore가 알아서 Long->Double 변환 등을 처리해주므로 매우 안전합니다.
                        UserData userData = documentSnapshot.toObject(UserData.class);
                        if (userData != null && userData.myIngredients != null) {
                            callback.onPantryLoaded(userData.myIngredients);
                        } else {
                            callback.onPantryLoaded(new ArrayList<>()); // myIngredients 필드가 없거나 비어있으면 빈 리스트 전달
                        }
                    } else {
                        callback.onPantryLoaded(new ArrayList<>()); // 사용자 문서가 없으면 빈 리스트 전달
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting pantry items", e);
                    callback.onError("재료를 불러오는 데 실패했습니다: " + e.getMessage());
                });
    }

    /**
     * [추가] Firestore의 자동 POJO 매핑을 돕기 위한 내부 정적 클래스입니다.
     * users 문서의 구조와 정확히 일치하며, 'myIngredients' 필드를 포함합니다.
     */
    public static class UserData {
        public List<PantryItem> myIngredients;

        // Firestore 자동 매핑을 위한 기본 생성자
        public UserData() {}
    }
}
