package com.example.food_recipe.search;

import com.example.food_recipe.base.BaseContract;
import com.example.food_recipe.model.Recipe;
import java.util.ArrayList;
import java.util.List;

// [변경] BaseContract를 상속받도록 수정
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

    // [변경] BaseContract.View를 상속받음
    interface View extends BaseContract.View {
        void showRecipes(List<Recipe> recipes);
        void showError(String message);
        void showLoadingIndicator();
        void hideLoadingIndicator();
        void addChipToGroup(String text);
        void clearSearchViewText();
        void showPantryImportBottomSheet(ArrayList<String> pantryItems, ArrayList<String> currentChips);
    }

    // [변경] BaseContract.Presenter를 상속받음
    interface Presenter extends BaseContract.Presenter<View> {
        void start();
        void search(String query);
        void onSearchQuerySubmitted(String query);
        void onPantryImportButtonClicked();
        void onPantryIngredientsSelected(ArrayList<String> ingredients);
        void onChipClosed(String chipText);
    }
}
