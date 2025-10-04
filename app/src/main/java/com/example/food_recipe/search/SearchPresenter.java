package com.example.food_recipe.search;

import com.algolia.search.saas.Client;
import com.algolia.search.saas.Index;
import com.algolia.search.saas.Query;
import com.algolia.search.saas.CompletionHandler;
import com.example.food_recipe.model.Recipe;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * [수정] 데이터 원본('파이어베이스 레시피 컬렉션.txt')에 명시된 실제 필드명으로 파싱하는 최종 Presenter입니다.
 */
public class SearchPresenter implements SearchContract.Presenter {

    private final SearchContract.View view;
    private final Index index;

    public SearchPresenter(SearchContract.View view) {
        this.view = view;
        Client client = new Client("6XOCPVQLP5", "ae27028a184d74d2fa885250257ce2dd");
        this.index = client.getIndex("recipes");
    }

    @Override
    public void search(String query) {
        view.showLoadingIndicator();

        CompletionHandler completionHandler = (content, error) -> {
            view.hideLoadingIndicator();
            if (error != null) {
                view.showError("검색 중 오류가 발생했습니다: " + error.getMessage());
                return;
            }
            try {
                List<Recipe> recipes = parseResults(content);
                view.showRecipes(recipes);
            } catch (Exception e) {
                view.showError("데이터를 처리하는 중 오류가 발생했습니다: " + e.getMessage());
            }
        };

        index.searchAsync(new Query(query), completionHandler);
    }

    /**
     * [수정] 데이터 원본에 명시된 실제 필드명("title", "cooking_time" 등)을 사용하여 JSON을 파싱합니다.
     */
    private List<Recipe> parseResults(JSONObject json) throws JSONException {
        List<Recipe> recipes = new ArrayList<>();
        JSONArray hits = json.getJSONArray("hits");
        for (int i = 0; i < hits.length(); i++) {
            JSONObject hit = hits.getJSONObject(i);
            String objectID = hit.optString("objectID");

            // [수정] 데이터 원본 필드명 사용
            String title = hit.optString("title", "제목 없음");
            String imageUrl = hit.optString("imageUrl", null);
            String cookingTime = hit.optString("cooking_time", "정보 없음"); // snake_case 유지
            long viewCount = hit.optLong("view_count", 0); // snake_case 유지
            long recommendCount = hit.optLong("recommend_count", 0); // snake_case 유지

            JSONArray ingredientsArray = hit.optJSONArray("ingredients");
            String ingredientsString = "";
            if (ingredientsArray != null) {
                List<String> ingredientsList = new ArrayList<>();
                for (int j = 0; j < ingredientsArray.length(); j++) {
                    ingredientsList.add(ingredientsArray.getString(j));
                }
                ingredientsString = String.join(", ", ingredientsList);
            }

            recipes.add(new Recipe(objectID, title, ingredientsString, imageUrl, cookingTime, viewCount, recommendCount));
        }
        return recipes;
    }

    @Override
    public void start() {
        search("");
    }
}
