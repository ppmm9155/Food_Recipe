package com.example.food_recipe.favorites;

import com.example.food_recipe.model.Recipe;

import java.util.List;

/**
 * [추가] 즐겨찾기 화면의 비즈니스 로직을 처리하고, View와 Model 간의 상호작용을 중재하는 Presenter 클래스입니다.
 * MVP 패턴에서 'Presenter'의 역할을 수행하며, FavoritesContract.Presenter 인터페이스를 구현합니다.
 */
public class FavoritesPresenter implements FavoritesContract.Presenter {

    private FavoritesContract.View view;
    private final FavoritesContract.Model model;

    public FavoritesPresenter(FavoritesContract.View view) {
        this.model = new FavoritesModel();
        attachView(view); // [변경] 생성자에서도 attachView를 호출하여 코드 일관성 유지
    }

    /**
     * [변경] 새로운 View가 연결되면, 참조를 업데이트합니다.
     */
    @Override
    public void attachView(FavoritesContract.View view) {
        this.view = view;
    }
    
    /**
     * [추가] View가 준비되었을 때 데이터 로딩을 시작합니다.
     */
    @Override
    public void start() {
        loadBookmarkedRecipes();
    }

    /**
     * [추가] Model에게 즐겨찾기 레시피 목록을 요청하고, 결과를 View에 전달합니다.
     */
    private void loadBookmarkedRecipes() {
        if (view != null) {
            view.showLoading();
        }

        model.getBookmarkedRecipes(new FavoritesContract.Model.OnFinishedListener<List<Recipe>>() {
            @Override
            public void onSuccess(List<Recipe> recipes) {
                if (view != null) {
                    if (recipes.isEmpty()) {
                        view.showEmptyView(); // 목록이 비었을 때 빈 화면 표시
                    } else {
                        view.showBookmarkedRecipes(recipes); // 목록이 있을 때 RecyclerView에 표시
                    }
                    view.hideLoading();
                }
            }

            @Override
            public void onError(Exception e) {
                if (view != null) {
                    view.showError("즐겨찾기 목록을 불러오는 데 실패했습니다: " + e.getMessage());
                    view.hideLoading();
                }
            }
        });
    }

    /**
     * [추가] View가 파괴될 때 View 참조를 해제하여 메모리 누수를 방지합니다.
     */
    @Override
    public void detachView() {
        this.view = null;
    }
}
