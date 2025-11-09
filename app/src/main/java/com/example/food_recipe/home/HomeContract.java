package com.example.food_recipe.home;

import androidx.annotation.Nullable;
import com.example.food_recipe.model.Recipe;
import java.util.List;

/**
 * [추가] 홈 화면의 View, Presenter, Model 사이의 통신 규칙을 정의하는 계약(Contract) 인터페이스입니다.
 */
public interface HomeContract {

    interface View {
        void showRecommendedRecipes(List<Recipe> recipes);
        void showPopularRecipes(List<Recipe> recipes);
        void showRecentAndFavorites(List<Recipe> recipes);
        void showEmptyRecentAndFavorites();
        void setUserName(@Nullable String userName); // [변경] Nullable 어노테이션 추가
        void showError(String message);
    }

    interface Presenter {
        void start();
        void detachView();
        void onAuthStateChanged(boolean isLoggedIn); // [추가]
    }

    interface Model {
        interface OnFinishedListener<T> {
            void onSuccess(T result);
            void onError(Exception e);
        }
        void getUserName(OnFinishedListener<String> callback);
        void getRecommendedRecipes(OnFinishedListener<List<Recipe>> callback);
        void getPopularRecipes(OnFinishedListener<List<Recipe>> callback);
        void getRecentAndFavoriteRecipes(OnFinishedListener<List<Recipe>> callback);
    }
}
