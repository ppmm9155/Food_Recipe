package com.example.food_recipe.search;

import com.algolia.search.saas.Client;
import com.algolia.search.saas.Index;
import com.algolia.search.saas.Query;
import com.example.food_recipe.model.Recipe;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 검색 기능의 데이터 처리를 담당하는 모델 클래스입니다.
 * MVP 패턴에서 'Model'의 역할을 수행하며, {@link SearchContract.Model} 인터페이스를 구현합니다.
 * Algolia 검색 서비스와 직접 통신하여 레시피를 검색하고, Firestore와 통신하여 초기 데이터를 가져옵니다.
 */
public class SearchModel implements SearchContract.Model {

    /**
     * Algolia 검색 인덱스에 접근하기 위한 인스턴스입니다.
     */
    private final Index index;

    /**
     * Firestore 데이터베이스에 접근하기 위한 인스턴스입니다. (주로 초기 데이터 로드에 사용)
     */
    private final FirebaseFirestore db;

    /**
     * SearchModel의 생성자입니다.
     * Algolia 클라이언트와 Firestore 인스턴스를 초기화합니다.
     */
    public SearchModel() {
        // 주의: 실제 앱에서는 API 키를 보안에 안전한 방식으로 관리해야 합니다.
        Client client = new Client("6XOCPVQLP5", "ae27028a184d74d2fa885250257ce2dd");
        index = client.getIndex("recipes"); // "recipes" 인덱스를 사용
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Firestore에서 초기 레시피 목록을 비동기적으로 가져옵니다.
     * 이 기능은 Algolia 검색 기반의 초기 화면 로드가 구현됨에 따라 현재는 직접 사용되지 않지만,
     * 향후 다른 용도로 사용될 수 있어 코드를 유지합니다.
     *
     * @param listener 데이터 조회 완료 후 결과를 전달받을 콜백 객체.
     */
    @Override
    public void fetchInitialRecipes(OnRecipesFetchedListener listener) {
        db.collection("recipes")
                .limit(10)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Recipe> recipes = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Recipe recipe = document.toObject(Recipe.class);
                        // Firestore 문서의 ID를 레시피의 고유 ID(rcpSno)로 설정합니다.
                        recipe.setRcpSno(document.getId());
                        recipes.add(recipe);
                    }
                    listener.onSuccess(recipes);
                })
                .addOnFailureListener(e -> listener.onError(e.getMessage()));
    }

    /**
     * Algolia 검색 서비스를 사용하여 주어진 쿼리로 레시피를 비동기적으로 검색합니다.
     * 검색 성공 시, 결과를 {@link Recipe} 객체 리스트로 파싱하여 반환합니다.
     *
     * @param query    사용자가 입력한 검색어.
     * @param listener 검색 결과를 전달받을 콜백 객체.
     */
    @Override
    public void searchRecipes(String query, OnRecipesFetchedListener listener) {
        index.searchAsync(new Query(query), (json, e) -> {
            if (e != null) {
                listener.onError(e.getMessage());
                return;
            }

            try {
                List<Recipe> recipes = new ArrayList<>();
                JSONArray hits = json.getJSONArray("hits"); // 검색 결과의 메인 배열
                for (int i = 0; i < hits.length(); i++) {
                    JSONObject hit = hits.getJSONObject(i); // 각 레시피 결과
                    Recipe recipe = new Recipe();

                    // Algolia의 고유 ID('objectID')를 레시피의 rcpSno에 매핑
                    recipe.setRcpSno(hit.getString("objectID"));
                    recipe.setTitle(hit.optString("title", "제목 없음"));
                    recipe.setImageUrl(hit.optString("imageUrl", ""));
                    recipe.setCookingTime(hit.optString("cooking_time", "정보 없음"));

                    // Algolia에 저장된 'ingredients'(배열)를 파싱하여 'ingredientsRaw'(문자열)로 변환
                    JSONArray ingredientsArray = hit.optJSONArray("ingredients");
                    if (ingredientsArray != null) {
                        List<String> ingredientsList = new ArrayList<>();
                        for (int j = 0; j < ingredientsArray.length(); j++) {
                            ingredientsList.add(ingredientsArray.getString(j));
                        }
                        // 각 재료를 ", "로 연결하여 하나의 문자열로 만듭니다.
                        recipe.setIngredientsRaw(String.join(", ", ingredientsList));
                    } else {
                        recipe.setIngredientsRaw("정보 없음");
                    }
                    
                    recipes.add(recipe);
                }
                listener.onSuccess(recipes);
            } catch (JSONException jsonException) {
                listener.onError(jsonException.getMessage());
            }
        });
    }
}
