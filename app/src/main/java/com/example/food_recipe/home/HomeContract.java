package com.example.food_recipe.home;

import androidx.annotation.Nullable;

import com.example.food_recipe.model.Recipe;

import java.util.List;

/**
 * [수정] ARCHITECTURE_SUMMARY.md 설계도에 맞춰 Model의 역할을 재정의합니다.
 * Model이 Firebase로부터 레시피를 직접 가져오는 책임을 갖게 됩니다.
 */
public interface HomeContract {

    interface View {
        void showPopularRecipes(List<Recipe> recipes);
        void showRecommendedRecipes(List<Recipe> recipes);
        void showPersonalizedGreeting(@Nullable String username);
        void showError(String message);
        void showLoadingIndicator();
        void hideLoadingIndicator();
    }

    interface Presenter {
        void start();
    }

    /**
     * [수정] Model의 역할이 Firebase와의 통신으로 명확해졌습니다.
     */
    interface Model {
        /**
         * 사용자 이름을 가져옵니다. (기존 유지)
         */
        void fetchUsername(UsernameCallback cb);

        /**
         * [추가] Firebase로부터 인기 레시피 목록을 가져오는 임무를 정의합니다.
         */
        void fetchPopularRecipes(RecipesCallback cb); //오늘의 추천 레시피

        /**
         * [추가] Firebase로부터 추천 레시피 목록을 가져오는 임무를 정의합니다.
         */
        void fetchRecommendedRecipes(RecipesCallback cb); // 지금 인기 있는 레시피


        // --- 콜백 인터페이스들 ---

        interface UsernameCallback {
            void onSuccess(@Nullable String username);
            void onError(Exception e);
        }

        /**
         * [추가] 레시피 목록 조회 결과를 처리하기 위한 콜백 인터페이스입니다.
         */
        interface RecipesCallback {
            void onSuccess(List<Recipe> recipes);
            void onError(Exception e);
        }
    }
}
