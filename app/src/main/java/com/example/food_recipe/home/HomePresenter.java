package com.example.food_recipe.home;

import androidx.annotation.Nullable;

import com.example.food_recipe.model.Recipe;

import java.util.List;

/**
 * [수정] ARCHITECTURE_SUMMARY.md 설계도에 맞춘 최종 Presenter입니다.
 * 이제 Algolia와 직접 통신하지 않으며, 모든 데이터 요청을 Model에게 위임합니다.
 */
public class HomePresenter implements HomeContract.Presenter {

    private final HomeContract.View view;
    private final HomeContract.Model model;

    /**
     * [수정] 생성자에서 Algolia 관련 코드를 모두 제거합니다.
     */
    public HomePresenter(HomeContract.View view, HomeContract.Model model) {
        this.view = view;
        this.model = model;
    }

    /**
     * [수정] View가 준비되면, Model에게 데이터 요청을 시작합니다.
     */
    @Override
    public void start() {
        fetchUsername();
        fetchPopularRecipes(); // 메서드 이름 변경
    }

    /**
     * 사용자 이름을 가져옵니다. (기존 유지)
     */
    private void fetchUsername() {
        model.fetchUsername(new HomeContract.Model.UsernameCallback() {
            @Override
            public void onSuccess(@Nullable String username) {
                view.showPersonalizedGreeting(username);
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
                view.showPersonalizedGreeting(null); // 실패 시 기본 문구 표시
            }
        });
    }

    /**
     * [수정] Model을 통해 인기 레시피 목록을 가져오도록 로직을 완전히 변경합니다.
     */
    private void fetchPopularRecipes() {
        view.showLoadingIndicator();
        model.fetchPopularRecipes(new HomeContract.Model.RecipesCallback() {
            @Override
            public void onSuccess(List<Recipe> recipes) {
                view.hideLoadingIndicator();
                view.showRecipes(recipes);
            }

            @Override
            public void onError(Exception e) {
                view.hideLoadingIndicator();
                view.showError("레시피를 불러오는 데 실패했습니다: " + e.getMessage());
            }
        });
    }
}
