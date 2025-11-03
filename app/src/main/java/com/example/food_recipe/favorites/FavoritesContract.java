package com.example.food_recipe.favorites;

import com.example.food_recipe.model.Recipe;
import java.util.List;

/**
 * [추가] 즐겨찾기 화면의 View, Presenter, Model 사이의 통신 규칙을 정의하는 계약(Contract) 인터페이스입니다.
 */
public interface FavoritesContract {

    /**
     * View가 반드시 구현해야 하는 기능 목록을 정의합니다.
     */
    interface View {
        void showBookmarkedRecipes(List<Recipe> recipes);
        void showLoading();
        void hideLoading();
        void showEmptyView();
        void showError(String message);
    }

    /**
     * Presenter가 반드시 구현해야 하는 기능 목록을 정의합니다.
     */
    interface Presenter {
        /**
         * [변경] 화면(View)이 다시 생성되었을 때, Presenter에게 새로운 View를 연결합니다.
         * @param view 새로 생성된 View 인스턴스
         */
        void attachView(View view);
        
        /**
         * View가 생성되고 준비되었을 때, 즐겨찾기 목록 로딩을 시작하라고 지시합니다.
         */
        void start();

        /**
         * View가 파괴될 때 Presenter가 View에 대한 참조를 안전하게 해제하여 메모리 누수를 방지합니다.
         */
        void detachView();
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
