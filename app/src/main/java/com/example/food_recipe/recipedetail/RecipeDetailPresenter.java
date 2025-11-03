package com.example.food_recipe.recipedetail;

import android.content.Context;
import com.example.food_recipe.model.Recipe;
import com.example.food_recipe.utils.RecentRecipeManager;

/**
 * [변경] Model과의 상호작용 시, RCP_SNO 대신 실제 문서 ID를 사용하도록 수정합니다.
 */
public class RecipeDetailPresenter implements RecipeDetailContract.Presenter {

    private RecipeDetailContract.View view;
    private final RecipeDetailContract.Model model;
    private Recipe currentRecipe;
    private final Context context; // [추가] Context 참조

    public RecipeDetailPresenter(RecipeDetailContract.View view, Context context) {
        this.view = view;
        this.model = new RecipeDetailModel();
        this.context = context; // [추가] Context 초기화
    }

    /**
     * [변경] 레시피 로드 성공 후, '최근 본 레시피' 목록에 현재 레시피를 추가합니다.
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
                    checkBookmarkStatus(recipe.getId());

                    // [추가] 최근 본 레시피 목록에 추가
                    RecentRecipeManager.addRecentRecipe(context, recipe.getId());
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

    @Override
    public void onBookmarkClicked() {
        if (currentRecipe == null || currentRecipe.getId() == null) {
            if(view != null) {
                view.showError("레시피 정보가 아직 로드되지 않았거나, 문서 ID가 없습니다.");
            }
            return;
        }

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
