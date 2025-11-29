package com.example.food_recipe.search;

import android.util.Log;
import com.algolia.search.saas.Client;
import com.algolia.search.saas.Index;
import com.algolia.search.saas.Query;
import com.example.food_recipe.BuildConfig;
import com.example.food_recipe.model.Recipe;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
        Client client = new Client(BuildConfig.ALGOLIA_APP_ID, BuildConfig.ALGOLIA_API_KEY);
        index = client.getIndex("recipes");
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    // [추가] Algolia 검색 결과를 Recipe 객체 리스트로 파싱하는 공통 로직을 별도 메소드로 분리합니다.
    private List<Recipe> parseRecipes(JSONObject json) throws JSONException {
        List<Recipe> recipes = new ArrayList<>();
        JSONArray hits = json.getJSONArray("hits");
        for (int i = 0; i < hits.length(); i++) {
            JSONObject hit = hits.getJSONObject(i);
            Recipe recipe = new Recipe();

            JSONObject highlightResult = hit.optJSONObject("_highlightResult");

            String title;
            if (highlightResult != null && highlightResult.has("title")) {
                title = highlightResult.getJSONObject("title").getString("value");
            } else {
                title = hit.optString("title", "제목 없음");
            }

            String ingredientsRaw;
            if (highlightResult != null && highlightResult.has("ingredients")) {
                JSONArray highlightedIngredients = highlightResult.getJSONArray("ingredients");
                List<String> ingredientsList = new ArrayList<>();
                for (int j = 0; j < highlightedIngredients.length(); j++) {
                    ingredientsList.add(highlightedIngredients.getJSONObject(j).getString("value"));
                }
                ingredientsRaw = String.join(", ", ingredientsList);
            } else {
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
        return recipes;
    }

    @Override
    public void searchRecipes(String query, OnRecipesFetchedListener listener) {
        // [변경] 검색 쿼리를 생성하는 부분을 유지합니다.
        Query algoliaQuery = new Query(query)
                .setAttributesToHighlight("title", "ingredients")
                .setHighlightPreTag("<b>")
                .setHighlightPostTag("</b>");

        index.searchAsync(algoliaQuery, (json, e) -> {
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
                // [변경] 공통 파싱 메소드를 호출하여 중복을 제거합니다.
                List<Recipe> recipes = parseRecipes(json);
                listener.onSuccess(recipes);
            } catch (JSONException jsonException) {
                listener.onError("검색 결과를 파싱하는데 실패했습니다: " + jsonException.getMessage());
            }
        });
    }

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
                                    Object nameObj = ingredientMap.get("name");
                                    if (nameObj != null) {
                                        items.add(nameObj.toString());
                                    }
                                 }
                            } else if (item instanceof String) {
                                items.add((String) item);
                            } else if (item != null) {
                                Log.w("SearchModel", "Unexpected data type in myIngredients: " + item.getClass().getName());
                            }
                        }
                    }

                    if (items.isEmpty() && myIngredientsObj != null) {
                        Log.w("SearchModel", "myIngredients field exists but failed to parse any items. Data: " + myIngredientsObj.toString());
                    }
                    
                    listener.onSuccess(items);
                })
                .addOnFailureListener(e -> listener.onError(e.getMessage()));
    }

    @Override
    public void fetchInitialRecipes(OnRecipesFetchedListener listener) {
        // [수정] 검색어가 없는 빈 Query 객체를 생성합니다. Algolia는 검색어가 없으면 설정된 랭킹 순으로 결과를 반환합니다.
        Query algoliaQuery = new Query("");

        index.searchAsync(algoliaQuery, (json, e) -> {
            Log.d("AlgoliaSearch", "Initial fetch");
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
                // [수정] 일반 검색과 동일하게 공통 파싱 메소드를 호출하여 결과를 처리합니다.
                List<Recipe> recipes = parseRecipes(json);
                listener.onSuccess(recipes);
            } catch (JSONException jsonException) {
                listener.onError("초기 레시피를 파싱하는데 실패했습니다: " + jsonException.getMessage());
            }
        });
    }
}
