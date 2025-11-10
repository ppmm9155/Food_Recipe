package com.example.food_recipe.home;

import android.content.Context;
import com.example.food_recipe.base.BasePresenter;
import com.example.food_recipe.model.Recipe;
import java.util.List;

/**
 * [변경] HomeContract.Presenter 인터페이스를 구현하도록 수정하고, '최근 본/즐겨찾기' 로직을 추가합니다.
 * 또한, 중앙 인증 시스템에 반응하도록 onAuthStateChanged 로직을 구현합니다.
 */
// [변경] BasePresenter를 상속받아 View 생명주기를 안전하게 관리
public class HomePresenter extends BasePresenter<HomeContract.View> implements HomeContract.Presenter {

    // [삭제] view 멤버 변수. BasePresenter가 관리하므로 제거.
    private final HomeContract.Model model;

    // [변경] 생성자에서 View를 받지 않음.
    public HomePresenter(Context context) {
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
        if (!isViewAttached()) return;
        if (isLoggedIn) {
            // 사용자가 로그인 상태일 경우, 개인화된 데이터를 로드합니다.
            loadUserName();
            loadRecentAndFavorites();
        } else {
            // 사용자가 로그아웃 상태일 경우, 개인화된 UI를 초기화합니다.
            getView().setUserName(null);
            getView().showEmptyRecentAndFavorites();
        }

        // 추천 및 인기 레시피는 로그인 여부와 관계없이 항상 로드합니다.
        loadRecommendedRecipes();
        loadPopularRecipes();
    }


    private void loadUserName() {
        model.getUserName(new HomeContract.Model.OnFinishedListener<String>() {
            @Override
            public void onSuccess(String userName) {
                if (isViewAttached()) {
                    getView().setUserName(userName);
                }
            }
            @Override
            public void onError(Exception e) {
                if (isViewAttached()) {
                    getView().setUserName(null); // 실패 시 기본값 처리
                }
            }
        });
    }

    private void loadRecommendedRecipes() {
        model.getRecommendedRecipes(new HomeContract.Model.OnFinishedListener<List<Recipe>>() {
            @Override
            public void onSuccess(List<Recipe> recipes) {
                if (isViewAttached()) {
                    getView().showRecommendedRecipes(recipes);
                }
            }
            @Override
            public void onError(Exception e) {
                if (isViewAttached()) {
                    getView().showError("추천 레시피 로딩 실패: " + e.getMessage());
                }
            }
        });
    }

    private void loadPopularRecipes() {
        model.getPopularRecipes(new HomeContract.Model.OnFinishedListener<List<Recipe>>() {
            @Override
            public void onSuccess(List<Recipe> recipes) {
                if (isViewAttached()) {
                    getView().showPopularRecipes(recipes);
                }
            }
            @Override
            public void onError(Exception e) {
                if (isViewAttached()) {
                    getView().showError("인기 레시피 로딩 실패: " + e.getMessage());
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
                if (isViewAttached()) {
                    if (recipes == null || recipes.isEmpty()) {
                        getView().showEmptyRecentAndFavorites();
                    } else {
                        getView().showRecentAndFavorites(recipes);
                    }
                }
            }
            @Override
            public void onError(Exception e) {
                if (isViewAttached()) {
                    // 이 부분은 에러를 표시하기보단, 그냥 빈 화면을 보여주는 것이 사용자 경험에 더 좋습니다.
                    getView().showEmptyRecentAndFavorites();
                }
            }
        });
    }

    // [삭제] detachView()는 BasePresenter에 구현되어 있으므로 제거
}
