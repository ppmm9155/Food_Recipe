package com.example.food_recipe.search;

import android.os.Handler;
import android.os.Looper;
import android.util.Log; // [추가] 예외 로깅을 위해 import 합니다.
import com.example.food_recipe.model.Recipe;
import com.example.food_recipe.utils.StringUtils;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture; // [추가] 비동기 처리를 위해 import 합니다.

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
        // [변경] CompletableFuture를 사용하여 명사 추출을 비동기로 처리합니다.
        CompletableFuture.supplyAsync(() -> StringUtils.extractNouns(query))
                .thenAccept(extractedNouns -> {
                    // [변경] UI 업데이트는 메인 스레드에서 수행해야 합니다.
                    searchHandler.post(() -> {
                        if (extractedNouns != null && !extractedNouns.isEmpty()) {
                            List<String> currentChips = new ArrayList<>(viewModel.searchChips.getValue());
                            boolean isChanged = false;
                            // [변경] 추출된 모든 명사를 칩으로 추가합니다.
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
                    // [추가] 비동기 작업 중 발생한 예외를 처리합니다.
                    searchHandler.post(() -> {
                        // 사용자에게 오류를 알리고 로그를 남깁니다.
                        view.showError("검색어 분석 중 오류가 발생했습니다.");
                        Log.e("SearchPresenter", "Error extracting nouns", ex);
                    });
                    return null; // exceptionally 블록은 null을 반환해야 합니다.
                });

        // 검색어 입력창은 비동기 작업 시작과 동시에 바로 비웁니다.
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
            // [변경] 냉장고에서 가져온 재료도 정규화하여 중복을 방지하고 검색 쿼리의 일관성을 유지합니다.
            String normalizedIngredient = StringUtils.normalizeIngredientName(ingredient);
            if (!currentChips.contains(normalizedIngredient)) {
                currentChips.add(normalizedIngredient);
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
