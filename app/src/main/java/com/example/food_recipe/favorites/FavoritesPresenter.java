package com.example.food_recipe.favorites;

import com.example.food_recipe.base.BasePresenter;
import com.example.food_recipe.model.Recipe;

import java.util.List;

/**
 * [추가] 즐겨찾기 화면의 비즈니스 로직을 처리하고, View와 Model 간의 상호작용을 중재하는 Presenter 클래스입니다.
 */
// [변경] BasePresenter를 상속받아 View 생명주기를 안전하게 관리
public class FavoritesPresenter extends BasePresenter<FavoritesContract.View> implements FavoritesContract.Presenter {

    private final FavoritesContract.Model model;

    // [변경] 생성자에서 View를 받지 않음
    public FavoritesPresenter() {
        this.model = new FavoritesModel();
    }

    // [삭제] attachView는 BasePresenter에 이미 정의되어 있으므로 제거

    @Override
    public void start() {
        loadBookmarkedRecipes();
    }

    private void loadBookmarkedRecipes() {
        if (isViewAttached()) {
            getView().showLoading();
        }

        model.getBookmarkedRecipes(new FavoritesContract.Model.OnFinishedListener<List<Recipe>>() {
            @Override
            public void onSuccess(List<Recipe> recipes) {
                if (isViewAttached()) {
                    if (recipes.isEmpty()) {
                        getView().showEmptyView();
                    } else {
                        getView().showBookmarkedRecipes(recipes);
                    }
                    getView().hideLoading();
                }
            }

            @Override
            public void onError(Exception e) {
                if (isViewAttached()) {
                    getView().showError("즐겨찾기 목록을 불러오는 데 실패했습니다: " + e.getMessage());
                    getView().hideLoading();
                }
            }
        });
    }

    // [삭제] detachView는 BasePresenter에 이미 정의되어 있으므로 제거
}
