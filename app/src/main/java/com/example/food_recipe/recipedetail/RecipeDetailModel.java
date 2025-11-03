
package com.example.food_recipe.recipedetail;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.Transaction;
import com.example.food_recipe.model.CookingStep;
import com.example.food_recipe.model.Recipe;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
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

    /**
     * [재설계] 불안정한 toObject() 대신, Recipe 클래스에 구현된 전용 매퍼 메소드를 사용하여
     * 데이터 변환의 일관성과 안정성을 보장합니다.
     */
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
                        // 새로 만든 안정적인 매퍼 메소드를 호출합니다.
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

    @Override
    public void toggleBookmark(String recipeId, OnFinishedListener<Boolean> callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            callback.onError(new Exception("로그인이 필요합니다."));
            return;
        }

        final DocumentReference userDocRef = db.collection("users").document(user.getUid());
        final DocumentReference recipeDocRef = db.collection("recipes").document(recipeId);

        userDocRef.get().addOnSuccessListener(userSnapshot -> {
            boolean isCurrentlyBookmarked = false;
            if (userSnapshot.exists()) {
                List<String> bookmarkedRecipes = (List<String>) userSnapshot.get("bookmarked_recipes");
                if (bookmarkedRecipes != null && bookmarkedRecipes.contains(recipeId)) {
                    isCurrentlyBookmarked = true;
                }
            }

            final boolean newState = !isCurrentlyBookmarked;
            final FieldValue userUpdate;
            final FieldValue recipeCountUpdate;

            if (newState) { // 즐겨찾기 추가
                userUpdate = FieldValue.arrayUnion(recipeId);
                recipeCountUpdate = FieldValue.increment(1);
            } else { // 즐겨찾기 해제
                userUpdate = FieldValue.arrayRemove(recipeId);
                recipeCountUpdate = FieldValue.increment(-1);
            }

            userDocRef.update("bookmarked_recipes", userUpdate)
                .addOnSuccessListener(aVoid -> {
                    recipeDocRef.update("recommend_count", recipeCountUpdate);
                    callback.onSuccess(newState);
                })
                .addOnFailureListener(callback::onError);

        }).addOnFailureListener(callback::onError);
    }
}
