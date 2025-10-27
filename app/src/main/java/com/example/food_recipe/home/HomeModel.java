package com.example.food_recipe.home;

import androidx.annotation.Nullable;

import com.example.food_recipe.model.Recipe;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * [수정] ARCHITECTURE_SUMMARY.md 설계도에 맞춰 Firebase와의 통신을 책임지는 최종 Model입니다.
 */
public class HomeModel implements HomeContract.Model {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * 사용자 이름을 가져옵니다. (기존 유지)
     */
    @Override
    public void fetchUsername(UsernameCallback cb) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            cb.onSuccess(null);
            return;
        }
        String uid = user.getUid();

        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener((@Nullable DocumentSnapshot doc) -> {
                    String username = null;
                    if (doc != null && doc.exists()) {
                        username = doc.getString("username");
                    }
                    cb.onSuccess(username);
                })
                .addOnFailureListener(cb::onError);
    }

    /**
     * [구현] 설계도에 따라 Firebase에서 인기 레시피를 가져오는 로직을 구현합니다.
     */
    @Override
    public void fetchPopularRecipes(RecipesCallback cb) {
        db.collection("recipes")
                .orderBy("recommend_count", Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Recipe> recipes = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        // Firestore 문서를 Recipe 객체로 변환합니다.
                        String id = document.getId();
                        String title = document.getString("title");
                        String imageUrl = document.getString("imageUrl");
                        String cookingTime = document.getString("cooking_time");
                        long viewCount = document.getLong("view_count") != null ? document.getLong("view_count") : 0;
                        long recommendCount = document.getLong("recommend_count") != null ? document.getLong("recommend_count") : 0;

                        // 재료(ingredients) 필드를 문자열로 변환합니다.
                        List<String> ingredientsList = (List<String>) document.get("ingredients");
                        String ingredientsString = "";
                        if (ingredientsList != null && !ingredientsList.isEmpty()) {
                            ingredientsString = String.join(", ", ingredientsList);
                        }

                        recipes.add(new Recipe(id, title, ingredientsString, imageUrl, cookingTime, viewCount, recommendCount));
                    }
                    cb.onSuccess(recipes);
                })
                .addOnFailureListener(cb::onError);
    }

    @Override
    public void fetchRecommendedRecipes(RecipesCallback cb) {
        db.collection("recipes")
                .limit(50) // 충분히 많은 레시피를 가져와서 섞습니다.
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Recipe> recipes = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        // Firestore 문서를 Recipe 객체로 변환합니다.
                        String id = document.getId();
                        String title = document.getString("title");
                        String imageUrl = document.getString("imageUrl");
                        String cookingTime = document.getString("cooking_time");
                        long viewCount = document.getLong("view_count") != null ? document.getLong("view_count") : 0;
                        long recommendCount = document.getLong("recommend_count") != null ? document.getLong("recommend_count") : 0;

                        // 재료(ingredients) 필드를 문자열로 변환합니다.
                        List<String> ingredientsList = (List<String>) document.get("ingredients");
                        String ingredientsString = "";
                        if (ingredientsList != null && !ingredientsList.isEmpty()) {
                            ingredientsString = String.join(", ", ingredientsList);
                        }

                        recipes.add(new Recipe(id, title, ingredientsString, imageUrl, cookingTime, viewCount, recommendCount));
                    }
                    // [추가] 레시피 목록을 무작위로 섞습니다.
                    Collections.shuffle(recipes);

                    // [추가] 섞인 목록에서 10개만 선택합니다.
                    List<Recipe> recommendedRecipes = new ArrayList<>(recipes.subList(0, Math.min(10, recipes.size())));

                    cb.onSuccess(recommendedRecipes);
                })
                .addOnFailureListener(cb::onError);
    }
}
