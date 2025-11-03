package com.example.food_recipe.search;

import com.example.food_recipe.model.Recipe;
import java.util.ArrayList;
import java.util.List;

public interface SearchContract {

    // --- [변경] Model 인터페이스에 냉장고 재료 로딩 기능 추가 ---
    interface Model {
        // [기존 유지] 레시피 검색 결과 콜백
        interface OnRecipesFetchedListener {
            void onSuccess(List<Recipe> recipes);
            void onError(String message);
        }

        // [추가] 냉장고 재료 로딩 결과 콜백
        interface OnPantryItemsFetchedListener {
            void onSuccess(List<String> items);
            void onError(String message);
        }

        // [기존 유지] 레시피 검색 기능
        void fetchInitialRecipes(OnRecipesFetchedListener listener);
        void searchRecipes(String query, OnRecipesFetchedListener listener);

        // [추가] 냉장고 재료 목록을 가져오는 기능
        void fetchPantryItems(OnPantryItemsFetchedListener listener);
    }

    interface View {
        void showRecipes(List<Recipe> recipes);
        void showError(String message);
        void showLoadingIndicator();
        void hideLoadingIndicator();
        void addChipToGroup(String text);
        void clearSearchViewText();
        // --- [변경] 바텀 시트를 보여줄 때, 전체 재료 목록도 함께 전달 ---
        void showPantryImportBottomSheet(ArrayList<String> pantryItems, ArrayList<String> currentChips);
    }

    interface Presenter {
        void start();
        void search(String query);
        void onSearchQuerySubmitted(String query);
        void onPantryImportButtonClicked();
        void onPantryIngredientsSelected(ArrayList<String> ingredients);
        void onChipClosed(String chipText);
    }
}
