package com.example.food_recipe.search;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.example.food_recipe.base.BasePresenter;
import com.example.food_recipe.model.Recipe;
import com.example.food_recipe.utils.StringUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SearchPresenter extends BasePresenter<SearchContract.View> implements SearchContract.Presenter {

    private final SearchContract.Model model;
    private final SearchViewModel viewModel;

    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;
    private static final long SEARCH_DELAY_MS = 300;

    public SearchPresenter(SearchViewModel viewModel) {
        this.model = new SearchModel();
        this.viewModel = viewModel;
    }

    @Override
    public void start() {
        if (viewModel.searchResult.getValue() == null || viewModel.searchResult.getValue().isEmpty()) {
            loadInitialRecipes();
        }
    }

    private void loadInitialRecipes() {
        if (!isViewAttached()) return;
        getView().showLoadingIndicator();
        model.fetchInitialRecipes(new SearchContract.Model.OnRecipesFetchedListener() {
            @Override
            public void onSuccess(List<Recipe> recipes) {
                if (!isViewAttached()) return;
                getView().hideLoadingIndicator();
                if (recipes == null || recipes.isEmpty()) {
                    getView().showInitialView();
                } else {
                    viewModel.setSearchResult(recipes);
                }
            }

            @Override
            public void onError(String message) {
                if (!isViewAttached()) return;
                getView().hideLoadingIndicator();
                getView().showError(message);
            }
        });
    }

    @Override
    public void search(String query) {
        if (!isViewAttached()) return;

        if (query == null || query.trim().isEmpty()) {
            loadInitialRecipes();
            return;
        }

        getView().showLoadingIndicator();
        model.searchRecipes(query, new SearchContract.Model.OnRecipesFetchedListener() {
            @Override
            public void onSuccess(List<Recipe> recipes) {
                if (!isViewAttached()) return;
                getView().hideLoadingIndicator();
                if (recipes == null || recipes.isEmpty()) {
                    getView().showEmptyView("검색 결과가 없습니다.");
                } else {
                    viewModel.setSearchResult(recipes);
                }
            }
            @Override
            public void onError(String message) {
                if (!isViewAttached()) return;
                getView().hideLoadingIndicator();
                getView().showError(message);
            }
        });
    }

    @Override
    public void onSearchQuerySubmitted(String query) {
        if (!isViewAttached()) return;
        CompletableFuture.supplyAsync(() -> StringUtils.extractNouns(query))
                .thenAccept(extractedNouns -> {
                    searchHandler.post(() -> {
                        if (!isViewAttached()) return;
                        if (extractedNouns != null && !extractedNouns.isEmpty()) {
                            List<String> currentChips = new ArrayList<>(viewModel.searchChips.getValue());
                            boolean isChanged = false;
                            String[] nouns = extractedNouns.split(" ");
                            for (String noun : nouns) {
                                if (!currentChips.contains(noun)) {
                                    currentChips.add(noun);
                                    isChanged = true;
                                }
                            }
                            if (isChanged) {
                                viewModel.setSearchChips(currentChips);
                                triggerDebouncedSearch();
                            }
                        }
                    });
                })
                .exceptionally(ex -> {
                    searchHandler.post(() -> {
                        if (isViewAttached()) {
                            getView().showError("검색어 분석 중 오류가 발생했습니다.");
                        }
                        Log.e("SearchPresenter", "Error extracting nouns", ex);
                    });
                    return null;
                });

        getView().clearSearchViewText();
    }

    @Override
    public void onPantryImportButtonClicked() {
        if (!isViewAttached()) return;
        getView().showLoadingIndicator();
        model.fetchPantryItems(new SearchContract.Model.OnPantryItemsFetchedListener() {
            @Override
            public void onSuccess(List<String> items) {
                if (!isViewAttached()) return;
                getView().hideLoadingIndicator();

                if (items == null || items.isEmpty()) {
                    // [수정] 모호한 문자열 메시지를 보내는 대신, 명확한 메소드를 직접 호출합니다.
                    getView().showEmptyPantrySnackbar();
                } else {
                    getView().showPantryImportBottomSheet(new ArrayList<>(items), new ArrayList<>(viewModel.searchChips.getValue()));
                }
            }
            @Override
            public void onError(String message) {
                if (!isViewAttached()) return;
                getView().hideLoadingIndicator();
                getView().showError(message);
            }
        });
    }

    @Override
    public void onPantryIngredientsSelected(ArrayList<String> ingredients) {
        List<String> currentChips = new ArrayList<>(viewModel.searchChips.getValue());
        boolean isChanged = false;
        for (String ingredient : ingredients) {
            String normalizedIngredient = StringUtils.normalizeIngredientName(ingredient);
            if (!currentChips.contains(normalizedIngredient)) {
                currentChips.add(normalizedIngredient);
                isChanged = true;
            }
        }
        if (isChanged) {
            viewModel.setSearchChips(currentChips);
            triggerDebouncedSearch();
        }
    }

    @Override
    public void onChipClosed(String chipText) {
        List<String> currentChips = new ArrayList<>(viewModel.searchChips.getValue());
        if (currentChips.remove(chipText)) {
            viewModel.setSearchChips(currentChips);
            triggerDebouncedSearch();
        }
    }

    @Override
    public void onPantrySelectionCancelled() {
        if (!isViewAttached()) return;
        List<Recipe> currentRecipes = viewModel.searchResult.getValue();

        if (currentRecipes == null || currentRecipes.isEmpty()) {
            loadInitialRecipes();
        } else {
            viewModel.setSearchResult(currentRecipes);
        }
    }

    private void triggerDebouncedSearch() {
        if (searchRunnable != null) {
            searchHandler.removeCallbacks(searchRunnable);
        }
        searchRunnable = this::performSearch;
        searchHandler.postDelayed(searchRunnable, SEARCH_DELAY_MS);
    }

    private void performSearch() {
        String finalQuery = String.join(" ", viewModel.searchChips.getValue());
        search(finalQuery);
    }
}
