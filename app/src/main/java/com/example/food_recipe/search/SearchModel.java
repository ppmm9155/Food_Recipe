package com.example.food_recipe.search;

import android.util.Log;
import com.algolia.search.saas.Client;
import com.algolia.search.saas.Index;
import com.algolia.search.saas.Query;
import com.example.food_recipe.model.Recipe;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SearchModel implements SearchContract.Model {

    private final Index index;
    private final FirebaseFirestore db;
    private final FirebaseAuth mAuth;

    public SearchModel() {
        Client client = new Client("6XOCPVQLP5", "ae27028a184d74d2fa885250257ce2dd");
        index = client.getIndex("recipes");
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void searchRecipes(String query, OnRecipesFetchedListener listener) {
        // [추가] 검색 결과 하이라이팅을 위한 쿼리 옵션을 설정합니다.
        Query algoliaQuery = new Query(query)
                .setAttributesToHighlight("title", "ingredients")
                .setHighlightPreTag("<b>")
                .setHighlightPostTag("</b>");

        index.searchAsync(algoliaQuery, (json, e) -> {
            // [추가] Algolia 응답과 에러를 직접 확인하기 위한 디버깅 로그
            Log.d("AlgoliaSearch", "Query: " + query);
            if (e != null) {
                Log.e("AlgoliaSearch", "Error: ", e);
            }
            if (json != null) {
                Log.d("AlgoliaSearch", "Result: " + json.toString());
            }

            if (e != null) {
                listener.onError(e.getMessage());
                return;
            }
            try {
                List<Recipe> recipes = new ArrayList<>();
                JSONArray hits = json.getJSONArray("hits");
                for (int i = 0; i < hits.length(); i++) {
                    JSONObject hit = hits.getJSONObject(i);
                    Recipe recipe = new Recipe();

                    // [변경] highlightResult가 null일 수 있으므로 optJSONObject로 안전하게 접근합니다.
                    JSONObject highlightResult = hit.optJSONObject("_highlightResult");

                    String title;
                    // [변경] 하이라이팅 결과가 있을 때만 해당 값을 사용하고, 없으면 원본을 사용합니다.
                    if (highlightResult != null && highlightResult.has("title")) {
                        title = highlightResult.getJSONObject("title").getString("value");
                    } else {
                        title = hit.optString("title", "제목 없음");
                    }

                    String ingredientsRaw;
                    // [변경] 하이라이팅된 재료가 배열(JSONArray)일 것을 가정하고 파싱 로직을 전면 수정합니다.
                    if (highlightResult != null && highlightResult.has("ingredients")) {
                        JSONArray highlightedIngredients = highlightResult.getJSONArray("ingredients");
                        List<String> ingredientsList = new ArrayList<>();
                        for (int j = 0; j < highlightedIngredients.length(); j++) {
                            // 각 배열 요소는 {"value": "하이라이팅된 텍스트"} 형태의 객체입니다.
                            ingredientsList.add(highlightedIngredients.getJSONObject(j).getString("value"));
                        }
                        ingredientsRaw = String.join(", ", ingredientsList);
                    } else {
                        // 하이라이팅 결과가 없을 경우, 기존처럼 원본 데이터에서 재료 정보를 가져옵니다.
                        JSONArray ingredientsArray = hit.optJSONArray("ingredients");
                        if (ingredientsArray != null && ingredientsArray.length() > 0) {
                            List<String> ingredientsList = new ArrayList<>();
                            for (int j = 0; j < ingredientsArray.length(); j++) {
                                ingredientsList.add(ingredientsArray.getString(j));
                            }
                            ingredientsRaw = String.join(", ", ingredientsList);
                        } else {
                            ingredientsRaw = "재료 정보 없음";
                        }
                    }

                    recipe.setRcpSno(hit.getString("objectID"));
                    recipe.setTitle(title);
                    recipe.setImageUrl(hit.optString("imageUrl", ""));
                    recipe.setCookingTime(hit.optString("cooking_time", "정보 없음"));
                    recipe.setIngredientsRaw(ingredientsRaw);

                    recipes.add(recipe);
                }
                listener.onSuccess(recipes);
            } catch (JSONException jsonException) {
                listener.onError("검색 결과를 파싱하는데 실패했습니다: " + jsonException.getMessage());
            }
        });
    }

    /**
     * [최종 수정] users 문서의 myIngredients 배열 필드에서 재료 이름을 직접 파싱합니다.
     * @param listener 데이터 조회 완료 후 결과를 전달받을 콜백 객체.
     */
    @Override
    public void fetchPantryItems(OnPantryItemsFetchedListener listener) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            listener.onError("로그인이 필요합니다.");
            return;
        }

        String uid = currentUser.getUid();
        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        listener.onError("사용자 정보를 찾을 수 없습니다.");
                        return;
                    }

                    List<String> items = new ArrayList<>();
                    Object myIngredientsObj = documentSnapshot.get("myIngredients");

                    if (myIngredientsObj instanceof List) {
                        List<?> rawList = (List<?>) myIngredientsObj;
                        for (Object item : rawList) {
                            if (item instanceof Map) {
                                Map<String, Object> ingredientMap = (Map<String, Object>) item;
                                if (ingredientMap.containsKey("name")) {
                                    items.add(String.valueOf(ingredientMap.get("name")));
                                }
                            }
                        }
                    }
                    listener.onSuccess(items);
                })
                .addOnFailureListener(e -> listener.onError(e.getMessage()));
    }

    @Override
    public void fetchInitialRecipes(OnRecipesFetchedListener listener) {
        listener.onSuccess(new ArrayList<>());
    }
}
