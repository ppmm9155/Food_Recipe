
package com.example.food_recipe.recipedetail;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;
import com.example.food_recipe.model.Recipe;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecipeDetailModel implements RecipeDetailContract.Model {

    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    public RecipeDetailModel() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    @Override
    public void getRecipeDetails(String rcpSno, OnFinishedListener<Recipe> callback) {
        db.collection("recipes")
            .whereEqualTo("RCP_SNO", rcpSno)
            .limit(1)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                if (queryDocumentSnapshots.isEmpty()) {
                    callback.onError(new Exception("레시피를 찾을 수 없습니다."));
                    return;
                }
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    try {
                        Recipe recipe = Recipe.fromDocumentSnapshot(document);
                        callback.onSuccess(recipe);
                    } catch (Exception e) {
                        callback.onError(new Exception("레시피 데이터를 변환하는 중 오류가 발생했습니다.", e));
                    }
                    return;
                }
            })
            .addOnFailureListener(callback::onError);
    }

    @Override
    public void checkBookmarkState(String recipeId, OnFinishedListener<Boolean> callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            callback.onSuccess(false);
            return;
        }
        DocumentReference userDocRef = db.collection("users").document(user.getUid());
        userDocRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                List<String> bookmarkedRecipes = (List<String>) documentSnapshot.get("bookmarked_recipes");
                callback.onSuccess(bookmarkedRecipes != null && bookmarkedRecipes.contains(recipeId));
            } else {
                callback.onSuccess(false);
            }
        }).addOnFailureListener(callback::onError);
    }

    /**
     * [변경] Firestore 트랜잭션의 안정성은 유지하되, 실패했던 '신규 사용자 문서 자동 생성' 로직을 완전히 제거합니다.
     * 이제 이 기능은 자신의 책임(즐겨찾기 상태 변경 및 카운트 동기화)에만 집중하며,
     * 사용자 문서가 없는 예외적인 상황에서는 작업을 즉시 중단하고 명확한 오류를 반환하여 안정성을 확보합니다.
     */
    @Override
    public void toggleBookmark(String rcpSno, OnFinishedListener<Boolean> callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            callback.onError(new Exception("로그인이 필요합니다."));
            return;
        }

        db.collection("recipes")
            .whereEqualTo("RCP_SNO", rcpSno)
            .limit(1)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                if (queryDocumentSnapshots.isEmpty()) {
                    callback.onError(new Exception("업데이트할 레시피를 찾을 수 없습니다."));
                    return;
                }
                final DocumentReference recipeDocRef = queryDocumentSnapshots.getDocuments().get(0).getReference();
                final DocumentReference userDocRef = db.collection("users").document(user.getUid());

                db.runTransaction((Transaction.Function<Boolean>) transaction -> {
                    // --- 1. 데이터 읽기 단계 ---
                    DocumentSnapshot userSnapshot = transaction.get(userDocRef);
                    DocumentSnapshot recipeSnapshot = transaction.get(recipeDocRef);

                    // --- [추가] 가장 중요한 안전 장치: 사용자 문서 존재 여부 확인 ---
                    if (!userSnapshot.exists()) {
                        // 사용자 문서가 없으면 작업을 진행하는 것은 위험하므로, 트랜잭션을 즉시 실패 처리합니다.
                        throw new FirebaseFirestoreException("사용자 정보를 찾을 수 없습니다. 즐겨찾기를 처리할 수 없습니다.",
                                FirebaseFirestoreException.Code.ABORTED);
                    }

                    // --- 2. 상태 결정 및 값 계산 단계 (기존 로직과 동일) ---
                    List<String> bookmarkedRecipes = (List<String>) userSnapshot.get("bookmarked_recipes");
                    boolean isCurrentlyBookmarked = bookmarkedRecipes != null && bookmarkedRecipes.contains(rcpSno);
                    boolean newBookmarkState = !isCurrentlyBookmarked;

                    long currentCount = recipeSnapshot.contains("recommend_count") ? recipeSnapshot.getLong("recommend_count") : 0;
                    long newCount = newBookmarkState ? currentCount + 1 : Math.max(0, currentCount - 1);

                    // --- 3. 쓰기 작업 예약 단계 ---
                    if (newBookmarkState) { // 즐겨찾기 추가
                        transaction.update(userDocRef, "bookmarked_recipes", FieldValue.arrayUnion(rcpSno));
                    } else { // 즐겨찾기 삭제
                        transaction.update(userDocRef, "bookmarked_recipes", FieldValue.arrayRemove(rcpSno));
                    }
                    transaction.update(recipeDocRef, "recommend_count", newCount);

                    return newBookmarkState;
                }).addOnSuccessListener(callback::onSuccess)
                  .addOnFailureListener(e -> {
                    // [개선] 실패 원인을 분석하여 사용자에게 더 구체적이고 친절한 오류 메시지를 전달합니다.
                    Log.e("RecipeDetailModel", "Bookmark transaction failed: " + e.getMessage(), e);
                    if (e instanceof FirebaseFirestoreException && ((FirebaseFirestoreException) e).getCode() == FirebaseFirestoreException.Code.ABORTED) {
                        // ABORTED 코드는 우리가 트랜잭션 내부에서 의도적으로 발생시킨 예외입니다. (사용자 프로필이 없는 경우)
                        callback.onError(new Exception("사용자 프로필이 없어 즐겨찾기를 할 수 없습니다."));
                    } else {
                        // 그 외의 경우는 일반적인 네트워크 오류나 권한 문제일 수 있습니다.
                        callback.onError(new Exception("오류가 발생하여 즐겨찾기 상태를 변경하지 못했습니다."));
                    }
                });
            })
            .addOnFailureListener(e -> {
                Log.e("RecipeDetailModel", "Could not find recipe to update bookmark", e);
                callback.onError(e);
            });
    }
}
