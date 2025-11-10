package com.example.food_recipe.recipedetail;

import android.content.Context;
import com.example.food_recipe.base.BasePresenter;
import com.example.food_recipe.model.Recipe;
import com.example.food_recipe.utils.RecentRecipeManager;

/**
 * [변경] 중앙 인증 시스템에 반응하도록 로직을 수정합니다.
 */
// [변경] BasePresenter를 상속받아 View 생명주기를 안전하게 관리
public class RecipeDetailPresenter extends BasePresenter<RecipeDetailContract.View> implements RecipeDetailContract.Presenter {

    private final RecipeDetailContract.Model model;
    private Recipe currentRecipe;
    private final Context context;

    // [변경] 생성자에서 View를 받지 않음
    public RecipeDetailPresenter(Context context) {
        this.model = new RecipeDetailModel();
        this.context = context;
    }

    @Override
    public void loadRecipe(String rcpSno, boolean isLoggedIn) {
        if (isViewAttached()) {
            getView().showLoading();
        }

        model.getRecipeDetails(rcpSno, new RecipeDetailContract.Model.OnFinishedListener<Recipe>() {
            @Override
            public void onSuccess(Recipe recipe) {
                currentRecipe = recipe;
                if (isViewAttached()) {
                    getView().showRecipe(recipe);

                    if (isLoggedIn) {
                        checkBookmarkStatus(recipe.getId());
                    } else {
                        getView().setBookmarkState(false);
                        getView().hideLoading();
                    }

                    RecentRecipeManager.addRecentRecipe(context, recipe.getId());
                }
            }

            @Override
            public void onError(Exception e) {
                if (isViewAttached()) {
                    getView().showError(e.getMessage());
                    getView().hideLoading();
                }
            }
        });
    }

    private void checkBookmarkStatus(String recipeId) {
        model.checkBookmarkState(recipeId, new RecipeDetailContract.Model.OnFinishedListener<Boolean>() {
            @Override
            public void onSuccess(Boolean isBookmarked) {
                if (isViewAttached()) {
                    getView().setBookmarkState(isBookmarked);
                    getView().hideLoading();
                }
            }

            @Override
            public void onError(Exception e) {
                if (isViewAttached()) {
                    getView().showError("즐겨찾기 정보를 불러오는데 실패했습니다: " + e.getMessage());
                    getView().hideLoading();
                }
            }
        });
    }

    @Override
    public void onBookmarkClicked() {
        if (currentRecipe == null || currentRecipe.getId() == null) {
            if(isViewAttached()) {
                getView().showError("레시피 정보가 아직 로드되지 않았거나, 문서 ID가 없습니다.");
            }
            return;
        }

        model.toggleBookmark(currentRecipe.getId(), new RecipeDetailContract.Model.OnFinishedListener<Boolean>() {
            @Override
            public void onSuccess(Boolean isBookmarked) {
                if (isViewAttached()) {
                    getView().setBookmarkState(isBookmarked);
                    if (isBookmarked) {
                        getView().showBookmarkResult("즐겨찾기에 추가되었습니다.");
                    } else {
                        getView().showBookmarkResult("즐겨찾기에서 해제되었습니다.");
                    }
                }
            }

            @Override
            public void onError(Exception e) {
                if (isViewAttached()) {
                    getView().showError("작업에 실패했습니다: " + e.getMessage());
                }
            }
        });
    }

    // [삭제] detachView는 BasePresenter에 이미 정의되어 있으므로 제거
}
