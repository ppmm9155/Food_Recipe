package com.example.food_recipe.search;

import com.example.food_recipe.model.Recipe;
import java.util.List;

public interface SearchContract {

    // --- [추가] Model이 따라야 할 규칙 ---
    interface Model {
        // [추가] 데이터 로딩이 완료되었을 때 Presenter에게 결과를 전달하는 콜백 인터페이스
        interface OnRecipesFetchedListener {
            void onSuccess(List<Recipe> recipes);
            void onError(String message);
        }

        // [추가] 초기 추천 레시피 목록을 가져오는 기능
        void fetchInitialRecipes(OnRecipesFetchedListener listener);

        // [추가] 검색어를 기반으로 레시피를 검색하는 기능
        void searchRecipes(String query, OnRecipesFetchedListener listener);
    }

    interface View {
        void showRecipes(List<Recipe> recipes);
        void showError(String message);
        void showLoadingIndicator();
        void hideLoadingIndicator();
    }

    interface Presenter {
        void search(String query);
        void start();
    }
}
