package com.example.food_recipe.search;

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
        index.searchAsync(new Query(query), (json, e) -> {
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
                    recipe.setRcpSno(hit.getString("objectID"));
                    recipe.setTitle(hit.optString("title", "제목 없음"));
                    recipe.setImageUrl(hit.optString("imageUrl", ""));
                    recipe.setCookingTime(hit.optString("cooking_time", "정보 없음"));
                    JSONArray ingredientsArray = hit.optJSONArray("ingredients");
                    if (ingredientsArray != null && ingredientsArray.length() > 0) {
                        List<String> ingredientsList = new ArrayList<>();
                        for (int j = 0; j < ingredientsArray.length(); j++) {
                            ingredientsList.add(ingredientsArray.getString(j));
                        }
                        recipe.setIngredientsRaw(String.join(", ", ingredientsList));
                    } else {
                        recipe.setIngredientsRaw("재료 정보 없음");
                    }
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
        // [변경] 하위 컬렉션이 아닌, 사용자의 단일 문서를 가져옵니다.
        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        listener.onError("사용자 정보를 찾을 수 없습니다.");
                        return;
                    }

                    List<String> items = new ArrayList<>();
                    // [변경] 문서에서 'myIngredients' 필드(배열)를 가져옵니다.
                    Object myIngredientsObj = documentSnapshot.get("myIngredients");

                    if (myIngredientsObj instanceof List) {
                        // Firestore는 배열을 List<Map<String, Object>> 형태로 반환합니다.
                        List<?> rawList = (List<?>) myIngredientsObj;
                        for (Object item : rawList) {
                            if (item instanceof Map) {
                                Map<String, Object> ingredientMap = (Map<String, Object>) item;
                                // 각 맵에서 'name' 키의 값을 문자열로 추출합니다.
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
