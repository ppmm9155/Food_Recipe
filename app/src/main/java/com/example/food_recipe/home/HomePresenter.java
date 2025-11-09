package com.example.food_recipe.home;

import android.content.Context;
import com.example.food_recipe.model.Recipe;
import java.util.List;

/**
 * [변경] HomeContract.Presenter 인터페이스를 구현하도록 수정하고, '최근 본/즐겨찾기' 로직을 추가합니다.
 * 또한, 중앙 인증 시스템에 반응하도록 onAuthStateChanged 로직을 구현합니다.
 */
public class HomePresenter implements HomeContract.Presenter {

    private HomeContract.View view;
    private final HomeContract.Model model;

    public HomePresenter(HomeContract.View view, Context context) {
        this.view = view;
        this.model = new HomeModel(context);
    }

    /**
     * [변경] 이 메소드는 HomeFragment가 AuthViewModel을 사용하게 되면서 더 이상 직접 호출되지 않습니다.
     * 모든 로직은 onAuthStateChanged로 이전되었습니다.
     */
    @Override
    public void start() {
        // 이 메소드의 내용은 onAuthStateChanged로 이전되었습니다.
    }

    /**
     * [추가] HomeFragment의 AuthViewModel 관찰자로부터 호출됩니다.
     * 로그인 상태에 따라 UI와 불러올 데이터를 결정하는 새로운 진입점입니다.
     * @param isLoggedIn 사용자의 로그인 여부
     */
    @Override
    public void onAuthStateChanged(boolean isLoggedIn) {
        if (isLoggedIn) {
            // 사용자가 로그인 상태일 경우, 개인화된 데이터를 로드합니다.
            loadUserName();
            loadRecentAndFavorites();
        } else {
            // 사용자가 로그아웃 상태일 경우, 개인화된 UI를 초기화합니다.
            if (view != null) {
                view.setUserName(null);
                view.showEmptyRecentAndFavorites();
            }
        }

        // 추천 및 인기 레시피는 로그인 여부와 관계없이 항상 로드합니다.
        loadRecommendedRecipes();
        loadPopularRecipes();
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
