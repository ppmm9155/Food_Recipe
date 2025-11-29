package com.example.food_recipe.recipedetail;

import com.example.food_recipe.base.BasePresenter;
import com.example.food_recipe.model.Recipe;
import com.example.food_recipe.utils.RecentRecipeManager;

/**
 * [기존 주석 유지]
 */
public class RecipeDetailPresenter extends BasePresenter<RecipeDetailContract.View> implements RecipeDetailContract.Presenter {

    private RecipeDetailContract.Model model;
    private Recipe currentRecipe;
    // [수정] Context 멤버 변수를 제거하여 메모리 누수 위험을 방지합니다.

    /**
     * [수정] 생성자에서 Context 주입을 제거합니다.
     */
    public RecipeDetailPresenter() {
        this.model = new RecipeDetailModel();
    }
    
    /**
     * [추가] 단위 테스트를 할 때 가짜(Mock) Model을 주입하기 위한 보조 생성자입니다.
     */
    public RecipeDetailPresenter(RecipeDetailContract.Model model) {
        this.model = model;
    }

    @Override
    public void loadRecipe(String rcpSno, boolean isLoggedIn) {
        if (!isViewAttached()) return;
        getView().showLoading();

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

                    // [수정] View가 살아있을 때만 안전하게 Context를 가져와 사용합니다.
                    RecentRecipeManager.addRecentRecipe(getView().getContext(), recipe.getId());
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
}
