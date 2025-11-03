package com.example.food_recipe.favorites;

import com.example.food_recipe.model.Recipe;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class FavoritesModel implements FavoritesContract.Model {

    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    public FavoritesModel() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    /**
     * [재설계] 불안정한 toObject() 대신, Recipe 클래스에 구현된 전용 매퍼 메소드를 사용하여
     * 데이터 변환의 일관성과 안정성을 보장합니다.
     */
    @Override
    public void getBookmarkedRecipes(OnFinishedListener<List<Recipe>> callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            callback.onSuccess(new ArrayList<>());
            return;
        }

        DocumentReference userDocRef = db.collection("users").document(user.getUid());
        userDocRef.get().addOnSuccessListener(userSnapshot -> {
            if (!userSnapshot.exists()) {
                callback.onSuccess(new ArrayList<>());
                return;
            }

            List<String> bookmarkedRecipeIds = (List<String>) userSnapshot.get("bookmarked_recipes");
            if (bookmarkedRecipeIds == null || bookmarkedRecipeIds.isEmpty()) {
                callback.onSuccess(new ArrayList<>());
                return;
            }

            List<Task<DocumentSnapshot>> tasks = new ArrayList<>();
            for (String recipeId : bookmarkedRecipeIds) {
                DocumentReference recipeDocRef = db.collection("recipes").document(recipeId);
                tasks.add(recipeDocRef.get());
            }

            Tasks.whenAllSuccess(tasks).addOnSuccessListener(results -> {
                List<Recipe> recipes = new ArrayList<>();
                for (Object snapshotObject : results) {
                    if (snapshotObject instanceof DocumentSnapshot) {
                        DocumentSnapshot snapshot = (DocumentSnapshot) snapshotObject;
                        if (snapshot.exists()) {
                            // 새로 만든 안정적인 매퍼 메소드를 호출합니다.
                            Recipe recipe = Recipe.fromDocumentSnapshot(snapshot);
                            recipes.add(recipe);
                        }
                    }
                }
                callback.onSuccess(recipes);
            }).addOnFailureListener(callback::onError);

        }).addOnFailureListener(callback::onError);
    }
}
