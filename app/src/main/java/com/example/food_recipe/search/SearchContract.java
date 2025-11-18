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
         * [수정] '검색 결과 없음'과 같은 지속적인 빈 화면 상태를 표시합니다.
         * 이 메소드는 스낵바 알림과는 명확히 구분됩니다.
         * @param message 화면에 표시할 메시지 (e.g., "검색 결과가 없습니다.")
         */
        void showEmptyView(String message);

        /**
         * [수정] 사용자가 검색하기 전, 초기 안내 문구를 보여주는 화면을 표시합니다.
         */
        void showInitialView();

        /**
         * [추가] 냉장고에 재료가 없을 때, 재료 추가를 유도하는 스낵바를 표시합니다.
         * 이 메소드는 일회성 알림이며, 화면의 다른 부분은 초기 상태로 돌아갈 수 있습니다.
         */
        void showEmptyPantrySnackbar();
    }

    interface Presenter extends BaseContract.Presenter<View> {
        void start();
        void search(String query);
        void onSearchQuerySubmitted(String query);
        void onPantryImportButtonClicked();
        void onPantryIngredientsSelected(ArrayList<String> ingredients);
        void onChipClosed(String chipText);
        void onPantrySelectionCancelled();
    }
}
