package com.example.food_recipe.search;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.food_recipe.R;
import com.example.food_recipe.adapter.RecipeAdapter;
import com.example.food_recipe.model.Recipe;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import java.util.ArrayList;
import java.util.List;

/**
 * [변경] ViewModel과 함께 동작하도록 Fragment 로직을 수정합니다.
 * 이제 Fragment는 Presenter에게 직접 UI 업데이트를 요청받지 않고,
 * ViewModel의 상태 변화를 관찰하여 스스로 UI를 갱신합니다.
 */
public class SearchFragment extends Fragment implements SearchContract.View, RecipeAdapter.OnItemClickListener {

    private static final String TAG = "SearchFragment";

    private ChipGroup searchChipGroup;
    private MaterialButton searchBtnPantryImport;
    private RecyclerView recyclerView;
    private TextView emptyTextView;
    private RecipeAdapter recipeAdapter;
    private SearchView searchView;
    private SearchContract.Presenter presenter;
    private SearchViewModel viewModel; // [추가] ViewModel 참조

    public static final String REQUEST_KEY_PANTRY_IMPORT = "pantry_import_request";
    public static final String BUNDLE_KEY_SELECTED_INGREDIENTS = "selected_ingredients";
    public static final String BUNDLE_KEY_PANTRY_ITEMS = "pantry_items";
    public static final String BUNDLE_KEY_CURRENT_CHIPS = "current_chips";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // [추가] ViewModel을 초기화합니다.
        viewModel = new ViewModelProvider(this).get(SearchViewModel.class);

        // [변경] Presenter를 생성할 때 ViewModel을 전달합니다.
        presenter = new SearchPresenter(this, viewModel);
        
        setupViews(view);
        setupRecyclerView();
        setupListeners();
        setupFragmentResultListener();
        observeViewModel(); // [추가] ViewModel 관찰 시작

        presenter.start();
    }

    private void setupViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_view_recipes);
        emptyTextView = view.findViewById(R.id.text_view_empty);
        searchView = view.findViewById(R.id.search_view);
        searchChipGroup = view.findViewById(R.id.search_chip_group);
        searchBtnPantryImport = view.findViewById(R.id.search_btn_pantry_import);
    }

    private void setupRecyclerView() {
        recipeAdapter = new RecipeAdapter(requireContext());
        recipeAdapter.setOnItemClickListener(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(recipeAdapter);
    }

    /**
     * [추가] ViewModel의 LiveData를 관찰하고, 데이터 변경 시 UI를 업데이트하는 메소드입니다.
     */
    private void observeViewModel() {
        viewModel.searchResult.observe(getViewLifecycleOwner(), recipes -> {
            updateRecipeListUI(recipes);
        });

        viewModel.searchChips.observe(getViewLifecycleOwner(), chips -> {
            updateChipsUI(chips);
        });
    }
    
    /**
     * [추가] 레시피 목록 LiveData 변경에 따라 UI를 갱신합니다.
     */
    private void updateRecipeListUI(List<Recipe> recipes) {
        recipeAdapter.setRecipes(recipes);
        if (recipes == null || recipes.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyTextView.setVisibility(View.VISIBLE);
            emptyTextView.setText("해당하는 레시피가 없습니다.");
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyTextView.setVisibility(View.GONE);
        }
    }

    /**
     * [추가] 검색어 칩 LiveData 변경에 따라 ChipGroup UI를 갱신합니다.
     */
    private void updateChipsUI(List<String> chips) {
        searchChipGroup.removeAllViews();
        for (String chipText : chips) {
            Chip chip = (Chip) getLayoutInflater().inflate(R.layout.item_search_chip, searchChipGroup, false);
            chip.setText(chipText);
            chip.setOnCloseIconClickListener(v -> presenter.onChipClosed(chipText));
            searchChipGroup.addView(chip);
        }
    }

    private void setupListeners() {
        searchBtnPantryImport.setOnClickListener(v -> presenter.onPantryImportButtonClicked());
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                presenter.onSearchQuerySubmitted(query);
                searchView.clearFocus();
                return true;
            }
            @Override
            public boolean onQueryTextChange(String newText) { return false; }
        });
    }

    private void setupFragmentResultListener() {
        getParentFragmentManager().setFragmentResultListener(REQUEST_KEY_PANTRY_IMPORT, this, (requestKey, result) -> {
            ArrayList<String> selectedIngredients = result.getStringArrayList(BUNDLE_KEY_SELECTED_INGREDIENTS);
            if (selectedIngredients != null) {
                presenter.onPantryIngredientsSelected(selectedIngredients);
            }
        });
    }

    @Override
    public void onItemClick(Recipe recipe) {
        if (recipe != null && recipe.getRcpSno() != null && !recipe.getRcpSno().isEmpty()) {
            Bundle bundle = new Bundle();
            bundle.putString("rcpSno", recipe.getRcpSno());
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_searchFragment_to_recipeDetailFragment, bundle);
        } else {
            Toast.makeText(getContext(), "레시피 정보가 없어 이동할 수 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }
    
    // [변경] 아래 메소드들은 이제 Presenter가 직접 호출하지 않으므로, View 인터페이스에서만 구현합니다.
    @Override
    public void showRecipes(List<Recipe> recipes) {
        // 이 메소드는 이제 observeViewModel의 updateRecipeListUI에 의해 처리됩니다.
    }

    @Override
    public void addChipToGroup(String text) {
        // 이 메소드는 이제 observeViewModel의 updateChipsUI에 의해 처리됩니다.
    }

    @Override
    public void showError(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        Log.e(TAG, "An error occurred: " + message);
        recyclerView.setVisibility(View.GONE);
        emptyTextView.setVisibility(View.VISIBLE);
        emptyTextView.setText("오류가 발생했습니다: " + message);
    }

    @Override
    public void showLoadingIndicator() { /* TODO: 로딩 인디케이터 표시 구현 */ }

    @Override
    public void hideLoadingIndicator() { /* TODO: 로딩 인디케이터 숨기기 구현 */ }

    @Override
    public void clearSearchViewText() {
        searchView.setQuery("", false);
    }

    @Override
    public void showPantryImportBottomSheet(ArrayList<String> pantryItems, ArrayList<String> currentChips) {
        PantryImportBottomSheetFragment bottomSheet = new PantryImportBottomSheetFragment();
        Bundle args = new Bundle();
        args.putStringArrayList(BUNDLE_KEY_PANTRY_ITEMS, pantryItems);
        args.putStringArrayList(BUNDLE_KEY_CURRENT_CHIPS, currentChips);
        bottomSheet.setArguments(args);
        bottomSheet.show(getParentFragmentManager(), bottomSheet.getTag());
    }
}
