package com.example.food_recipe.home;

import androidx.annotation.Nullable;

import com.example.food_recipe.base.BaseContract;
import com.example.food_recipe.model.Recipe;

import java.util.List;

/**
 * [변경] HomeFragment/Presenter의 역할 변경에 따라 Contract를 최신화합니다.
 * OnFinishedListener를 사용하여 콜백을 단순화합니다.
 */
public interface HomeContract {

    // [수정] 모든 View의 기본 계약인 BaseContract.View를 상속받도록 수정합니다.
    interface View extends BaseContract.View {
        void showRecommendedRecipes(List<Recipe> recipes);
        void showPopularRecipes(List<Recipe> recipes);
        void showRecentAndFavorites(List<Recipe> recipes);
        void showEmptyRecentAndFavorites();
        void setUserName(@Nullable String username);
        void showError(String message);
    }

    // [수정] 모든 Presenter의 기본 계약인 BaseContract.Presenter를 상속받도록 수정합니다.
    interface Presenter extends BaseContract.Presenter<View> {
        void start(); // 기존의 start 메서드는 이제 사용되지 않을 수 있지만, 호환성을 위해 유지
        void onAuthStateChanged(boolean isLoggedIn); // 로그인 상태 변경을 처리할 새 메서드
    }

    interface Model {
        // [변경] 범용 콜백 리스너를 사용하여 코드를 단순화합니다.
        interface OnFinishedListener<T> {
            void onSuccess(T result);
            void onError(Exception e);
        }

        void getUserName(OnFinishedListener<String> callback);
        void getPopularRecipes(OnFinishedListener<List<Recipe>> callback);
        void getRecommendedRecipes(OnFinishedListener<List<Recipe>> callback);
        void getRecentAndFavoriteRecipes(OnFinishedListener<List<Recipe>> callback);
    }
}
