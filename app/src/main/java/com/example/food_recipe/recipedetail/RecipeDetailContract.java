package com.example.food_recipe.recipedetail;

import com.example.food_recipe.base.BaseContract;
import com.example.food_recipe.model.Recipe;

/**
 * [변경] 즐겨찾기 기능 관련 인터페이스를 추가하여 계약을 확장합니다.
 */
// [변경] BaseContract를 상속받도록 수정
public interface RecipeDetailContract {

    /**
     * View가 반드시 구현해야 하는 기능 목록을 정의합니다.
     */
    // [변경] BaseContract.View를 상속받음
    interface View extends BaseContract.View {
        void showRecipe(Recipe recipe);
        void showLoading();
        void hideLoading();
        void showError(String message);
        void setBookmarkState(boolean isBookmarked);
        void showBookmarkResult(String message);
    }

    /**
     * Presenter가 반드시 구현해야 하는 기능 목록을 정의합니다.
     */
    // [변경] BaseContract.Presenter를 상속받음
    interface Presenter extends BaseContract.Presenter<View> {
        void loadRecipe(String rcpSno, boolean isLoggedIn);
        void onBookmarkClicked();
        // [삭제] detachView()는 BaseContract.Presenter에 이미 정의되어 있으므로 제거
    }

    /**
     * Model이 반드시 구현해야 하는 기능 목록을 정의합니다.
     */
    interface Model {
        void getRecipeDetails(String rcpSno, OnFinishedListener<Recipe> callback);
        void checkBookmarkState(String recipeId, OnFinishedListener<Boolean> callback);
        void toggleBookmark(String recipeId, OnFinishedListener<Boolean> callback);

        interface OnFinishedListener<T> {
            void onSuccess(T result);
            void onError(Exception e);
        }
    }
}
