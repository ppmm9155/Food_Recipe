package com.example.food_recipe.recipedetail;

import com.example.food_recipe.model.Recipe;

/**
 * [변경] Model과의 상호작용 시, RCP_SNO 대신 실제 문서 ID를 사용하도록 수정합니다.
 */
public class RecipeDetailPresenter implements RecipeDetailContract.Presenter {

    private RecipeDetailContract.View view;
    private final RecipeDetailContract.Model model;
    private Recipe currentRecipe;

    public RecipeDetailPresenter(RecipeDetailContract.View view) {
        this.view = view;
        this.model = new RecipeDetailModel();
    }

    /**
     * [변경] 레시피 로드 성공 후, 즐겨찾기 상태를 확인할 때 실제 문서 ID를 사용하도록 수정합니다.
     */
    @Override
    public void loadRecipe(String rcpSno) {
        if (view != null) {
            view.showLoading();
        }

        model.getRecipeDetails(rcpSno, new RecipeDetailContract.Model.OnFinishedListener<Recipe>() {
            @Override
            public void onSuccess(Recipe recipe) {
                currentRecipe = recipe;
                if (view != null) {
                    view.showRecipe(recipe);
                    // [변경] rcpSno 대신 실제 문서 ID(recipe.getId())를 사용하여 즐겨찾기 상태를 체크합니다.
                    checkBookmarkStatus(recipe.getId());
                }
            }

            @Override
            public void onError(Exception e) {
                if (view != null) {
                    view.showError(e.getMessage());
                    view.hideLoading();
                }
            }
        });
    }

    /**
     * [변경] 메소드 시그니처는 동일하지만, 내부 로직에서 넘어온 recipeId가 실제 문서 ID임을 인지하고 사용합니다.
     */
    private void checkBookmarkStatus(String recipeId) {
        model.checkBookmarkState(recipeId, new RecipeDetailContract.Model.OnFinishedListener<Boolean>() {
            @Override
            public void onSuccess(Boolean isBookmarked) {
                if (view != null) {
                    view.setBookmarkState(isBookmarked);
                    view.hideLoading();
                }
            }

            @Override
            public void onError(Exception e) {
                if (view != null) {
                    view.showError("즐겨찾기 정보를 불러오는데 실패했습니다: " + e.getMessage());
                    view.hideLoading();
                }
            }
        });
    }

    /**
     * [변경] 즐겨찾기 상태를 토글할 때, RCP_SNO 대신 실제 문서 ID를 Model에 전달합니다.
     */
    @Override
    public void onBookmarkClicked() {
        if (currentRecipe == null || currentRecipe.getId() == null) {
            if(view != null) {
                view.showError("레시피 정보가 아직 로드되지 않았거나, 문서 ID가 없습니다.");
            }
            return;
        }

        // [변경] currentRecipe.getRcpSno() 대신 currentRecipe.getId()를 사용합니다.
        model.toggleBookmark(currentRecipe.getId(), new RecipeDetailContract.Model.OnFinishedListener<Boolean>() {
            @Override
            public void onSuccess(Boolean isBookmarked) {
                if (view != null) {
                    view.setBookmarkState(isBookmarked);
                    if (isBookmarked) {
                        view.showBookmarkResult("즐겨찾기에 추가되었습니다.");
                    } else {
                        view.showBookmarkResult("즐겨찾기에서 해제되었습니다.");
                    }
                }
            }

            @Override
            public void onError(Exception e) {
                if (view != null) {
                    view.showError("작업에 실패했습니다: " + e.getMessage());
                }
            }
        });
    }

    @Override
    public void detachView() {
        this.view = null;
    }
}
