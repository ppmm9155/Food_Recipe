package com.example.food_recipe.recipedetail;

import android.content.Context;
import com.example.food_recipe.model.Recipe;
import com.example.food_recipe.utils.RecentRecipeManager;

/**
 * [변경] 중앙 인증 시스템에 반응하도록 로직을 수정합니다.
 */
public class RecipeDetailPresenter implements RecipeDetailContract.Presenter {

    private RecipeDetailContract.View view;
    private final RecipeDetailContract.Model model;
    private Recipe currentRecipe;
    private final Context context;

    public RecipeDetailPresenter(RecipeDetailContract.View view, Context context) {
        this.view = view;
        this.model = new RecipeDetailModel();
        this.context = context;
    }

    /**
     * [변경] 로그인 상태를 전달받아, 로그인된 경우에만 즐겨찾기 상태를 확인하도록 로직을 변경합니다.
     */
    @Override
    public void loadRecipe(String rcpSno, boolean isLoggedIn) {
        if (view != null) {
            view.showLoading();
        }

        model.getRecipeDetails(rcpSno, new RecipeDetailContract.Model.OnFinishedListener<Recipe>() {
            @Override
            public void onSuccess(Recipe recipe) {
                currentRecipe = recipe;
                if (view != null) {
                    view.showRecipe(recipe);

                    // [추가] 로그인 상태일 때만 즐겨찾기 여부를 확인합니다.
                    if (isLoggedIn) {
                        checkBookmarkStatus(recipe.getId());
                    } else {
                        // 로그아웃 상태이면, 즐겨찾기 안된 상태(빈 하트)로 UI를 설정하고 로딩을 숨깁니다.
                        view.setBookmarkState(false);
                        view.hideLoading();
                    }

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
