package com.example.food_recipe.home;

import com.example.food_recipe.base.BasePresenter;
import com.example.food_recipe.model.Recipe;
import java.util.List;

/**
 * [기존 주석 유지]
 */
public class HomePresenter extends BasePresenter<HomeContract.View> implements HomeContract.Presenter {

    private HomeContract.Model model;
    // [추가] 비동기 데이터 로딩 완료를 추적하기 위한 카운터
    private int loadCounter;
    private int totalLoadTasks;


    // [수정] 생성자에서 Context 주입을 제거하여 메모리 누수 위험을 방지합니다.
    public HomePresenter() {
        // Model 초기화는 View가 연결되는 시점에 안전하게 처리됩니다.
    }

    /**
     * [추가] 단위 테스트를 할 때 가짜(Mock) Model을 주입하기 위한 보조 생성자입니다.
     */
    public HomePresenter(HomeContract.Model model) {
        this.model = model;
    }

    /**
     * [추가] View가 연결될 때 Model을 초기화합니다.
     */
    @Override
    public void attachView(HomeContract.View view) {
        super.attachView(view);
        if (model == null) {
            model = new HomeModel(getView().getContext());
        }
    }

    @Override
    public void start() {
        // [기존 로직 유지]
    }

    /**
     * [추가] '더보기' 버튼 클릭 시 호출됩니다.
     * View에 즐겨찾기 탭으로 이동하라는 명령을 전달합니다.
     */
    @Override
    public void onMoreFavoritesClicked() {
        if (isViewAttached()) {
            getView().navigateToFavoritesTab();
        }
    }

    @Override
    public void onAuthStateChanged(boolean isLoggedIn) {
        if (!isViewAttached()) return;

        // [추가] 로딩 시작
        getView().showLoading();
        loadCounter = 0;

        if (isLoggedIn) {
            totalLoadTasks = 4; // 1.이름, 2.최근/즐찾, 3.추천, 4.인기
            loadUserName();
            loadRecentAndFavorites();
        } else {
            totalLoadTasks = 2; // 1.추천, 2.인기
            getView().setUserName(null);
            getView().showEmptyRecentAndFavorites();
        }
        loadRecommendedRecipes();
        loadPopularRecipes();
    }

    // [추가] 모든 데이터 로드가 완료되었는지 확인하고 로딩 UI를 숨기는 헬퍼 메서드
    private void checkAllDataLoaded() {
        loadCounter++;
        if (loadCounter >= totalLoadTasks && isViewAttached()) {
            getView().hideLoading();
        }
    }

    private void loadUserName() {
        model.getUserName(new HomeContract.Model.OnFinishedListener<String>() {
            @Override
            public void onSuccess(String userName) {
                if (isViewAttached()) {
                    getView().setUserName(userName);
                }
                // [추가] 작업 완료 체크
                checkAllDataLoaded();
            }
            @Override
            public void onError(Exception e) {
                if (isViewAttached()) {
                    getView().setUserName(null);
                }
                // [추가] 작업 완료 체크
                checkAllDataLoaded();
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
                // [추가] 작업 완료 체크
                checkAllDataLoaded();
            }
            @Override
            public void onError(Exception e) {
                if (isViewAttached()) {
                    getView().showError("추천 레시피 로딩 실패: " + e.getMessage());
                }
                // [추가] 작업 완료 체크
                checkAllDataLoaded();
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
                // [추가] 작업 완료 체크
                checkAllDataLoaded();
            }
            @Override
            public void onError(Exception e) {
                if (isViewAttached()) {
                    getView().showError("인기 레시피 로딩 실패: " + e.getMessage());
                }
                // [추가] 작업 완료 체크
                checkAllDataLoaded();
            }
        });
    }

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
                // [추가] 작업 완료 체크
                checkAllDataLoaded();
            }
            @Override
            public void onError(Exception e) {
                if (isViewAttached()) {
                    getView().showEmptyRecentAndFavorites();
                }
                // [추가] 작업 완료 체크
                checkAllDataLoaded();
            }
        });
    }
}
