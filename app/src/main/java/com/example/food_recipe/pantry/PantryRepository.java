package com.example.food_recipe.pantry;

import android.util.Log;
import androidx.annotation.NonNull;
import com.example.food_recipe.model.PantryItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

/**
 * [기존 주석 유지] Pantry(냉장고) 관련 데이터 처리를 담당하는 Repository 클래스 (싱글톤)
 * [변경] 데이터 쓰기 작업(추가/수정)의 완료 시점을 알리기 위한 콜백이 추가되었습니다.
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

    public interface PantryWriteCallback {
        void onWriteSuccess();
        void onWriteFailure(String message);
    }

    public void addPantryItem(PantryItem newItem, @NonNull final PantryWriteCallback callback) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            String errorMsg = "Error: User not signed in! Cannot add item.";
            Log.e(TAG, errorMsg);
            callback.onWriteFailure(errorMsg);
            return;
        }
        db.collection("users").document(currentUser.getUid())
                .update("myIngredients", FieldValue.arrayUnion(newItem))
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Pantry item successfully added to array: " + newItem.getName());
                    callback.onWriteSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error updating pantry array", e);
                    callback.onWriteFailure(e.getMessage());
                });
    }

    /**
     * [복원] 실수로 삭제되었던 재료 삭제 메서드를 복원합니다.
     */
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

    public void updatePantryItem(PantryItem itemToUpdate, @NonNull final PantryWriteCallback callback) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null || itemToUpdate == null || itemToUpdate.getId() == null) {
            String errorMsg = "Error: User not signed in or item is invalid! Cannot update item.";
            Log.e(TAG, errorMsg);
            callback.onWriteFailure(errorMsg);
            return;
        }

        String userId = currentUser.getUid();
        db.collection("users").document(userId).get().addOnSuccessListener(documentSnapshot -> {
            if (!documentSnapshot.exists()) {
                String errorMsg = "User document does not exist, cannot update.";
                Log.e(TAG, errorMsg);
                callback.onWriteFailure(errorMsg);
                return;
            }

            UserData userData = documentSnapshot.toObject(UserData.class);
            if (userData == null || userData.myIngredients == null) {
                String errorMsg = "User data or ingredients list is null, cannot update.";
                Log.e(TAG, errorMsg);
                callback.onWriteFailure(errorMsg);
                return;
            }

            List<PantryItem> updatedList = new ArrayList<>();
            boolean itemFound = false;
            for (PantryItem currentItem : userData.myIngredients) {
                if (itemToUpdate.getId().equals(currentItem.getId())) {
                    updatedList.add(itemToUpdate);
                    itemFound = true;
                } else {
                    updatedList.add(currentItem);
                }
            }

            if (itemFound) {
                db.collection("users").document(userId)
                        .update("myIngredients", updatedList)
                        .addOnSuccessListener(aVoid -> {
                            Log.d(TAG, "Pantry item successfully updated: " + itemToUpdate.getName());
                            callback.onWriteSuccess();
                        })
                        .addOnFailureListener(e -> {
                            Log.w(TAG, "Error updating pantry item", e);
                            callback.onWriteFailure(e.getMessage());
                        });
            } else {
                String errorMsg = "Item to update not found in the list: " + itemToUpdate.getName();
                Log.w(TAG, errorMsg);
                callback.onWriteFailure(errorMsg);
            }

        }).addOnFailureListener(e -> {
            Log.e(TAG, "Failed to get user document for update", e);
            callback.onWriteFailure(e.getMessage());
        });
    }

    public interface PantryLoadCallback {
        void onPantryLoaded(List<PantryItem> pantryItems);
        void onError(String message);
    }

    /**
     * [복원] 실수로 삭제되었던 재료 로딩 메서드를 복원합니다.
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
                        UserData userData = documentSnapshot.toObject(UserData.class);
                        if (userData != null && userData.myIngredients != null) {
                            callback.onPantryLoaded(userData.myIngredients);
                        } else {
                            callback.onPantryLoaded(new ArrayList<>());
                        }
                    } else {
                        callback.onPantryLoaded(new ArrayList<>());
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting pantry items", e);
                    callback.onError("재료를 불러오는 데 실패했습니다: " + e.getMessage());
                });
    }

    public static class UserData {
        public List<PantryItem> myIngredients;
        public UserData() {}
    }
}
