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
 * 홈 화면 기능의 데이터 처리를 담당하는 모델 클래스입니다.
 * MVP 패턴에서 'Model'의 역할을 수행하며, {@link HomeContract.Model} 인터페이스를 구현합니다.
 * Firebase Firestore 및 Firebase Auth와 직접 통신하여 데이터를 가져오는 로직을 포함합니다.
 */
public class HomeModel implements HomeContract.Model {

    /**
     * Firestore 데이터베이스에 접근하기 위한 인스턴스입니다.
     */
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * 현재 로그인된 사용자의 이름을 Firestore에서 비동기적으로 가져옵니다.
     *
     * @param callback 데이터 조회 완료 후 결과를 전달받을 콜백 객체.
     */
    @Override
    public void fetchUsername(UsernameCallback callback) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        // 사용자가 로그인하지 않은 경우, null을 즉시 반환합니다.
        if (user == null) {
            callback.onSuccess(null);
            return;
        }
        String uid = user.getUid();

        // 'users' 컬렉션에서 해당 사용자의 문서를 조회합니다.
        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener((@Nullable DocumentSnapshot doc) -> {
                    String username = null;
                    if (doc != null && doc.exists()) {
                        // 문서가 존재하면 'username' 필드의 값을 읽어옵니다.
                        username = doc.getString("username");
                    }
                    callback.onSuccess(username);
                })
                .addOnFailureListener(callback::onError);
    }

    /**
     * Firestore에서 인기 레시피 목록을 비동기적으로 가져옵니다.
     * 'recommend_count' 필드를 기준으로 내림차순 정렬하여 상위 10개의 레시피를 가져옵니다.
     *
     * @param callback 데이터 조회 완료 후 결과를 전달받을 콜백 객체.
     */
    @Override
    public void fetchPopularRecipes(RecipesCallback callback) {
        db.collection("recipes")
                .orderBy("recommend_count", Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Recipe> recipes = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        // Firestore 문서를 Recipe 객체로 변환합니다.
                        Recipe recipe = new Recipe();
                        recipe.setRcpSno(document.getString("RCP_SNO"));
                        recipe.setTitle(document.getString("title"));
                        recipe.setImageUrl(document.getString("imageUrl"));
                        recipe.setCookingTime(document.getString("cooking_time"));

                        // 'ingredients' 필드가 List 타입인지 확인하고 안전하게 캐스팅합니다.
                        Object ingredientsObj = document.get("ingredients");
                        if (ingredientsObj instanceof List) {
                            recipe.setIngredients((List<String>) ingredientsObj);
                        }

                        recipes.add(recipe);
                    }
                    callback.onSuccess(recipes);
                })
                .addOnFailureListener(callback::onError);
    }

    /**
     * Firestore에서 추천 레시피 목록을 비동기적으로 가져옵니다.
     * 먼저 50개의 레시피를 가져온 후, 무작위로 섞어 상위 10개를 선택하여 반환합니다.
     * (현재 추천 로직은 임시적인 랜덤 방식입니다.)
     *
     * @param callback 데이터 조회 완료 후 결과를 전달받을 콜백 객체.
     */
    @Override
    public void fetchRecommendedRecipes(RecipesCallback callback) {
        db.collection("recipes")
                .limit(50)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Recipe> recipes = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        // Firestore 문서를 Recipe 객체로 변환합니다.
                        Recipe recipe = new Recipe();
                        recipe.setRcpSno(document.getString("RCP_SNO"));
                        recipe.setTitle(document.getString("title"));
                        recipe.setImageUrl(document.getString("imageUrl"));
                        recipe.setCookingTime(document.getString("cooking_time"));

                        // 'ingredients' 필드가 List 타입인지 확인하고 안전하게 캐스팅합니다.
                        Object ingredientsObj = document.get("ingredients");
                        if (ingredientsObj instanceof List) {
                            recipe.setIngredients((List<String>) ingredientsObj);
                        }

                        recipes.add(recipe);
                    }
                    // 가져온 레시피 목록을 무작위로 섞습니다.
                    Collections.shuffle(recipes);
                    // 섞인 목록에서 상위 10개 (또는 전체 크기가 10보다 작을 경우 그 크기만큼)를 잘라내어 최종 목록을 만듭니다.
                    List<Recipe> recommendedRecipes = new ArrayList<>(recipes.subList(0, Math.min(10, recipes.size())));
                    callback.onSuccess(recommendedRecipes);
                })
                .addOnFailureListener(callback::onError);
    }
}
