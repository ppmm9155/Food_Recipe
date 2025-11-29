package com.example.food_recipe.favorites;

import com.example.food_recipe.base.BaseContract;
import com.example.food_recipe.model.Recipe;
import java.util.List;

/**
 * [추가] 즐겨찾기 화면의 View, Presenter, Model 사이의 통신 규칙을 정의하는 계약(Contract) 인터페이스입니다.
 */
// [변경] BaseContract를 상속받도록 수정
public interface FavoritesContract {

    /**
     * View가 반드시 구현해야 하는 기능 목록을 정의합니다.
     */
    // [변경] BaseContract.View를 상속받음
    interface View extends BaseContract.View {
        void showBookmarkedRecipes(List<Recipe> recipes);
        void showLoading();
        void hideLoading();
        void showEmptyView();
        void showError(String message);
    }

    /**
     * Presenter가 반드시 구현해야 하는 기능 목록을 정의합니다.
     */
    // [변경] BaseContract.Presenter를 상속받음
    interface Presenter extends BaseContract.Presenter<View> {
        void start();
        // [삭제] attachView, detachView는 BaseContract.Presenter에 이미 정의되어 있으므로 제거
    }

    /**
     * Model이 반드시 구현해야 하는 기능 목록을 정의합니다.
     */
    interface Model {
        void getBookmarkedRecipes(OnFinishedListener<List<Recipe>> callback);

        interface OnFinishedListener<T> {
            void onSuccess(T result);
            void onError(Exception e);
        }
    }
}
