package com.example.food_recipe.home;

import androidx.annotation.Nullable;
import com.example.food_recipe.base.BaseContract;
import com.example.food_recipe.model.Recipe;
import java.util.List;

/**
 * [추가] 홈 화면의 View, Presenter, Model 사이의 통신 규칙을 정의하는 계약(Contract) 인터페이스입니다.
 */
// [변경] BaseContract를 상속받도록 수정
public interface HomeContract {

    // [변경] BaseContract.View를 상속받음
    interface View extends BaseContract.View {
        void showRecommendedRecipes(List<Recipe> recipes);
        void showPopularRecipes(List<Recipe> recipes);
        void showRecentAndFavorites(List<Recipe> recipes);
        void showEmptyRecentAndFavorites();
        void setUserName(@Nullable String userName);
        void showError(String message);
    }

    // [변경] BaseContract.Presenter를 상속받음
    interface Presenter extends BaseContract.Presenter<View> {
        void start();
        void onAuthStateChanged(boolean isLoggedIn);
        // [삭제] detachView()는 BaseContract.Presenter에 이미 정의되어 있으므로 제거
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
