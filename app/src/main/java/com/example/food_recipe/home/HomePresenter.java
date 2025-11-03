package com.example.food_recipe.home;

import android.content.Context;
import com.example.food_recipe.model.Recipe;
import java.util.List;

/**
 * [변경] HomeContract.Presenter 인터페이스를 구현하도록 수정하고, '최근 본/즐겨찾기' 로직을 추가합니다.
 */
public class HomePresenter implements HomeContract.Presenter {

    private HomeContract.View view;
    private final HomeContract.Model model;

    public HomePresenter(HomeContract.View view, Context context) {
        this.view = view;
        this.model = new HomeModel(context);
    }

    @Override
    public void start() {
        loadUserName();
        loadRecommendedRecipes();
        loadPopularRecipes();
        loadRecentAndFavorites();
    }

    private void loadUserName() {
        model.getUserName(new HomeContract.Model.OnFinishedListener<String>() {
            @Override
            public void onSuccess(String userName) {
                if (view != null) {
                    view.setUserName(userName);
                }
            }
            @Override
            public void onError(Exception e) {
                if (view != null) {
                    view.setUserName(null); // 실패 시 기본값 처리
                }
            }
        });
    }

    private void loadRecommendedRecipes() {
        model.getRecommendedRecipes(new HomeContract.Model.OnFinishedListener<List<Recipe>>() {
            @Override
            public void onSuccess(List<Recipe> recipes) {
                if (view != null) {
                    view.showRecommendedRecipes(recipes);
                }
            }
            @Override
            public void onError(Exception e) {
                if (view != null) {
                    view.showError("추천 레시피 로딩 실패: " + e.getMessage());
                }
            }
        });
    }

    private void loadPopularRecipes() {
        model.getPopularRecipes(new HomeContract.Model.OnFinishedListener<List<Recipe>>() {
            @Override
            public void onSuccess(List<Recipe> recipes) {
                if (view != null) {
                    view.showPopularRecipes(recipes);
                }
            }
            @Override
            public void onError(Exception e) {
                if (view != null) {
                    view.showError("인기 레시피 로딩 실패: " + e.getMessage());
                }
            }
        });
    }

    /**
     * [추가] '최근 본/즐겨찾기' 목록 로드를 시작하고, 결과에 따라 View를 제어합니다.
     */
    private void loadRecentAndFavorites() {
        model.getRecentAndFavoriteRecipes(new HomeContract.Model.OnFinishedListener<List<Recipe>>() {
            @Override
            public void onSuccess(List<Recipe> recipes) {
                if (view != null) {
                    if (recipes == null || recipes.isEmpty()) {
                        view.showEmptyRecentAndFavorites();
                    } else {
                        view.showRecentAndFavorites(recipes);
                    }
                }
            }
            @Override
            public void onError(Exception e) {
                if (view != null) {
                    // 이 부분은 에러를 표시하기보단, 그냥 빈 화면을 보여주는 것이 사용자 경험에 더 좋습니다.
                    view.showEmptyRecentAndFavorites();
                }
            }
        });
    }

    @Override
    public void detachView() {
        this.view = null;
    }
}
