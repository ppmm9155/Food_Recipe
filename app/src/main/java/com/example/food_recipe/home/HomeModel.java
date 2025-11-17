package com.example.food_recipe.home;

import android.content.Context;
import androidx.annotation.Nullable;
import com.example.food_recipe.model.Recipe;
import com.example.food_recipe.utils.RecentRecipeManager;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * [변경] HomeContract.Model 인터페이스를 구현하도록 수정하고, '최근 본/즐겨찾기' 로직을 추가합니다.
 */
public class HomeModel implements HomeContract.Model {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final Context context;

    public HomeModel(Context context) {
        this.context = context;
    }

    @Override
    public void getUserName(OnFinishedListener<String> callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            callback.onSuccess(null);
            return;
        }
        db.collection("users").document(user.getUid()).get()
            .addOnSuccessListener(doc -> {
                if (doc != null && doc.exists()) {
                    callback.onSuccess(doc.getString("username"));
                } else {
                    callback.onSuccess(null);
                }
            })
            .addOnFailureListener(callback::onError);
    }
    
    @Override
    public void getPopularRecipes(OnFinishedListener<List<Recipe>> callback) {
        db.collection("recipes")
            .orderBy("recommend_count", Query.Direction.DESCENDING)
            .limit(10)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<Recipe> recipes = new ArrayList<>();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    recipes.add(Recipe.fromDocumentSnapshot(document));
                }
                callback.onSuccess(recipes);
            })
            .addOnFailureListener(callback::onError);
    }
    
    @Override
    public void getRecommendedRecipes(OnFinishedListener<List<Recipe>> callback) {
        db.collection("recipes")
            .limit(100) // [수정] 후보군을 50개에서 100개로 늘림
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                
                // [추가] 유효한 레시피(재료 정보가 있는)만 담을 리스트 생성
                List<Recipe> validRecipes = new ArrayList<>();
                
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                     Recipe recipe = Recipe.fromDocumentSnapshot(document);
                     
                     // [추가] 재료 정보가 있는지 확인 (List 또는 String 형태 모두)
                     List<String> ingredients = recipe.getIngredients();
                     String ingredientsRaw = document.getString("ingredients_raw");

                     boolean hasIngredientsList = (ingredients != null && !ingredients.isEmpty());
                     boolean hasIngredientsRaw = (ingredientsRaw != null && !ingredientsRaw.isEmpty() && !"null".equalsIgnoreCase(ingredientsRaw));

                     // [추가] 재료 목록이나 원본 재료 문자열 둘 중 하나라도 정보가 있으면 유효한 레시피로 판단
                     if (hasIngredientsList || hasIngredientsRaw) {
                         validRecipes.add(recipe);
                     }
                }
                
                // [수정] 유효한 레시피 리스트를 섞음
                Collections.shuffle(validRecipes);
                
                // [수정] 유효한 레시피 중에서 최대 10개를 뽑아 최종 목록으로 만듬
                List<Recipe> recommendedRecipes = new ArrayList<>(validRecipes.subList(0, Math.min(10, validRecipes.size())));
                
                callback.onSuccess(recommendedRecipes);
            })
            .addOnFailureListener(callback::onError);
    }

    /**
     * [추가] 최근 본 레시피와 즐겨찾기 레시피 ID를 조합하여 최종 레시피 목록을 가져옵니다.
     */
    @Override
    public void getRecentAndFavoriteRecipes(OnFinishedListener<List<Recipe>> callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            // 로그인하지 않은 경우, 최근 본 레시피만 가져옵니다.
            fetchRecipesByIds(RecentRecipeManager.getRecentRecipeIds(context), callback);
            return;
        }

        db.collection("users").document(user.getUid()).get()
            .addOnSuccessListener(userDoc -> {
                // 1. 즐겨찾기 ID 목록 가져오기
                List<String> favoriteIds = new ArrayList<>();
                if (userDoc.exists() && userDoc.contains("bookmarked_recipes")) {
                    favoriteIds = (List<String>) userDoc.get("bookmarked_recipes");
                }

                // 2. 최근 본 ID 목록 가져오기
                List<String> recentIds = RecentRecipeManager.getRecentRecipeIds(context);
                
                // 3. 두 목록을 합치고 중복 제거 (순서 유지: 최근 본 항목이 앞으로)
                Set<String> combinedIdsSet = new LinkedHashSet<>(recentIds);
                if(favoriteIds != null) {
                    combinedIdsSet.addAll(favoriteIds);
                }
                
                // 4. 미리보기이므로 최대 5개로 제한
                List<String> finalIds = combinedIdsSet.stream().limit(5).collect(Collectors.toList());

                if (finalIds.isEmpty()) {
                    callback.onSuccess(new ArrayList<>());
                } else {
                    fetchRecipesByIds(finalIds, callback);
                }
            })
            .addOnFailureListener(e -> {
                // 즐겨찾기 로드 실패 시, 최근 본 레시피만이라도 로드
                fetchRecipesByIds(RecentRecipeManager.getRecentRecipeIds(context), callback);
            });
    }

    /**
     * [추가] 제공된 문서 ID 목록에 해당하는 레시피 정보 목록을 Firestore에서 가져옵니다.
     */
    private void fetchRecipesByIds(List<String> ids, OnFinishedListener<List<Recipe>> callback) {
        if (ids == null || ids.isEmpty()) {
            callback.onSuccess(new ArrayList<>());
            return;
        }

        List<Task<DocumentSnapshot>> tasks = ids.stream()
            .map(id -> db.collection("recipes").document(id).get())
            .collect(Collectors.toList());

        Tasks.whenAllSuccess(tasks).addOnSuccessListener(results -> {
            List<Recipe> recipes = new ArrayList<>();
            for (Object snapshotObject : results) {
                DocumentSnapshot snapshot = (DocumentSnapshot) snapshotObject;
                if (snapshot.exists()) {
                    recipes.add(Recipe.fromDocumentSnapshot(snapshot));
                }
            }
            callback.onSuccess(recipes);
        }).addOnFailureListener(callback::onError);
    }
}
