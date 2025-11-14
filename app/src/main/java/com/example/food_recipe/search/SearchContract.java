package com.example.food_recipe.search;

import com.example.food_recipe.base.BaseContract;
import com.example.food_recipe.model.Recipe;
import java.util.ArrayList;
import java.util.List;

public interface SearchContract {

    interface Model {
        interface OnRecipesFetchedListener {
            void onSuccess(List<Recipe> recipes);
            void onError(String message);
        }

        interface OnPantryItemsFetchedListener {
            void onSuccess(List<String> items);
            void onError(String message);
        }

        void fetchInitialRecipes(OnRecipesFetchedListener listener);
        void searchRecipes(String query, OnRecipesFetchedListener listener);
        void fetchPantryItems(OnPantryItemsFetchedListener listener);
    }

    interface View extends BaseContract.View {
        void showRecipes(List<Recipe> recipes);
        void showError(String message);
        void showLoadingIndicator();
        void hideLoadingIndicator();
        void addChipToGroup(String text);
        void clearSearchViewText();
        void showPantryImportBottomSheet(ArrayList<String> pantryItems, ArrayList<String> currentChips);

        /**
         * [수정] 냉장고가 비어있을 때, '추가하기' 액션 버튼이 포함된 스낵바를 표시하라는 지시입니다.
         */
        void showPantryEmptyActionSnackbar();
    }

    interface Presenter extends BaseContract.Presenter<View> {
        void start();
        void search(String query);
        void onSearchQuerySubmitted(String query);
        void onPantryImportButtonClicked();
        void onPantryIngredientsSelected(ArrayList<String> ingredients);
        void onChipClosed(String chipText);
    }
}
