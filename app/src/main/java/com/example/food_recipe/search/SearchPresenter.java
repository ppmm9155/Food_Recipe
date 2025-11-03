package com.example.food_recipe.search;

import android.os.Handler;
import android.os.Looper;
import com.example.food_recipe.model.Recipe;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class SearchPresenter implements SearchContract.Presenter {

    private final SearchContract.View view;
    private final SearchContract.Model model;
    private final Set<String> currentSearchChips = new LinkedHashSet<>();
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;
    private static final long SEARCH_DELAY_MS = 300;

    public SearchPresenter(SearchContract.View view) {
        this.view = view;
        this.model = new SearchModel();
    }

    @Override
    public void start() {
        performSearch();
    }

    @Override
    public void search(String query) {
        view.showLoadingIndicator();
        model.searchRecipes(query, new SearchContract.Model.OnRecipesFetchedListener() {
            @Override
            public void onSuccess(List<Recipe> recipes) {
                view.hideLoadingIndicator();
                view.showRecipes(recipes);
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
        if (!normalizedQuery.isEmpty() && currentSearchChips.add(normalizedQuery)) {
            view.addChipToGroup(normalizedQuery);
            triggerDebouncedSearch();
        }
        view.clearSearchViewText();
    }

    /**
     * [변경] '내 냉장고 재료 불러오기' 버튼 클릭 시, Model로부터 데이터를 먼저 가져온 후 View를 호출합니다.
     */
    @Override
    public void onPantryImportButtonClicked() {
        view.showLoadingIndicator(); // 데이터 로딩 시작을 알림
        model.fetchPantryItems(new SearchContract.Model.OnPantryItemsFetchedListener() {
            @Override
            public void onSuccess(List<String> items) {
                view.hideLoadingIndicator(); // 로딩 완료
                // Model로부터 받은 전체 재료 목록과, 현재 화면의 Chip 목록을 함께 View에 전달
                view.showPantryImportBottomSheet(new ArrayList<>(items), new ArrayList<>(currentSearchChips));
            }

            @Override
            public void onError(String message) {
                view.hideLoadingIndicator(); // 로딩 실패
                view.showError(message);     // 에러 메시지 표시
            }
        });
    }

    @Override
    public void onPantryIngredientsSelected(ArrayList<String> ingredients) {
        boolean isChanged = false;
        for (String ingredient : ingredients) {
            if (currentSearchChips.add(ingredient)) {
                view.addChipToGroup(ingredient);
                isChanged = true;
            }
        }
        if (isChanged) {
            triggerDebouncedSearch();
        }
    }

    @Override
    public void onChipClosed(String chipText) {
        if (currentSearchChips.remove(chipText)) {
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
        String finalQuery = String.join(" ", currentSearchChips);
        search(finalQuery);
    }
}
