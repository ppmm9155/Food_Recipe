package com.example.food_recipe.search;

import android.os.Handler;
import android.os.Looper;
import com.example.food_recipe.model.Recipe;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * [변경] ViewModel과 함께 동작하도록 Presenter 로직을 수정합니다.
 * 이제 Presenter는 View에 직접 데이터를 전달하는 대신, ViewModel의 상태를 업데이트합니다.
 */
public class SearchPresenter implements SearchContract.Presenter {

    private final SearchContract.View view;
    private final SearchContract.Model model;
    private final SearchViewModel viewModel; // [추가] ViewModel 참조
    
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;
    private static final long SEARCH_DELAY_MS = 300;

    public SearchPresenter(SearchContract.View view, SearchViewModel viewModel) {
        this.view = view;
        this.model = new SearchModel();
        this.viewModel = viewModel; // [추가] ViewModel 초기화
    }

    /**
     * [변경] 시작 시 ViewModel에 상태가 있는지 먼저 확인합니다.
     */
    @Override
    public void start() {
        // ViewModel에 이미 결과가 있다면 (예: 뒤로가기로 돌아온 경우),
        // 새로운 검색을 수행하지 않고 기존 상태를 유지합니다.
        if (viewModel.searchResult.getValue() == null) {
            performSearch();
        }
    }

    @Override
    public void search(String query) {
        view.showLoadingIndicator();
        model.searchRecipes(query, new SearchContract.Model.OnRecipesFetchedListener() {
            @Override
            public void onSuccess(List<Recipe> recipes) {
                view.hideLoadingIndicator();
                viewModel.setSearchResult(recipes); // [변경] View 대신 ViewModel 업데이트
            }
            @Override
            public void onError(String message) {
                view.hideLoadingIndicator();
                view.showError(message);
            }
        });
    }

    @Override
    public void onSearchQuerySubmitted(String query) {
        String normalizedQuery = query.trim();
        if (!normalizedQuery.isEmpty()) {
            List<String> currentChips = new ArrayList<>(viewModel.searchChips.getValue());
            if (!currentChips.contains(normalizedQuery)) {
                currentChips.add(normalizedQuery);
                viewModel.setSearchChips(currentChips); // [변경] View 대신 ViewModel 업데이트
                triggerDebouncedSearch();
            }
        }
        view.clearSearchViewText();
    }

    @Override
    public void onPantryImportButtonClicked() {
        view.showLoadingIndicator();
        model.fetchPantryItems(new SearchContract.Model.OnPantryItemsFetchedListener() {
            @Override
            public void onSuccess(List<String> items) {
                view.hideLoadingIndicator();
                // [변경] 현재 칩 목록을 ViewModel에서 가져와서 View에 전달합니다.
                view.showPantryImportBottomSheet(new ArrayList<>(items), new ArrayList<>(viewModel.searchChips.getValue()));
            }
            @Override
            public void onError(String message) {
                view.hideLoadingIndicator();
                view.showError(message);
            }
        });
    }

    @Override
    public void onPantryIngredientsSelected(ArrayList<String> ingredients) {
        List<String> currentChips = new ArrayList<>(viewModel.searchChips.getValue());
        boolean isChanged = false;
        for (String ingredient : ingredients) {
            if (!currentChips.contains(ingredient)) {
                currentChips.add(ingredient);
                isChanged = true;
            }
        }
        if (isChanged) {
            viewModel.setSearchChips(currentChips); // [변경] View 대신 ViewModel 업데이트
            triggerDebouncedSearch();
        }
    }



    @Override
    public void onChipClosed(String chipText) {
        List<String> currentChips = new ArrayList<>(viewModel.searchChips.getValue());
        if (currentChips.remove(chipText)) {
            viewModel.setSearchChips(currentChips); // [변경] View 대신 ViewModel 업데이트
            triggerDebouncedSearch();
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
        // [변경] 현재 칩 목록을 ViewModel에서 가져와서 검색 쿼리를 생성합니다.
        String finalQuery = String.join(" ", viewModel.searchChips.getValue());
        search(finalQuery);
    }
}
