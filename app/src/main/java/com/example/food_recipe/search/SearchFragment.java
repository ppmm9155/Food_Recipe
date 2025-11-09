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
import com.example.food_recipe.main.AuthViewModel; // [추가]
import com.example.food_recipe.model.Recipe;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import java.util.ArrayList;
import java.util.List;

/**
 * [변경] 중앙 인증 관리(AuthViewModel) 시스템을 사용하도록 리팩토링합니다.
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
    private SearchViewModel viewModel;
    private AuthViewModel authViewModel; // [추가]

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

        viewModel = new ViewModelProvider(this).get(SearchViewModel.class);
        // [추가] Activity 범위의 AuthViewModel 인스턴스를 가져옵니다.
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        presenter = new SearchPresenter(this, viewModel);
        
        setupViews(view);
        setupRecyclerView();
        setupListeners();
        setupFragmentResultListener();
        observeViewModel();

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

    private void observeViewModel() {
        viewModel.searchResult.observe(getViewLifecycleOwner(), recipes -> {
            updateRecipeListUI(recipes);
        });

        viewModel.searchChips.observe(getViewLifecycleOwner(), chips -> {
            updateChipsUI(chips);
        });
    }
    
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
        // [변경] '냉장고 재료 가져오기' 버튼 클릭 시 로그인 상태를 먼저 확인합니다.
        searchBtnPantryImport.setOnClickListener(v -> {
            if (authViewModel.user.getValue() != null) {
                // 로그인 상태일 경우, Presenter에 작업을 요청합니다.
                presenter.onPantryImportButtonClicked();
            } else {
                // 로그아웃 상태일 경우, 사용자에게 안내 메시지를 표시합니다.
                Toast.makeText(getContext(), "로그인이 필요한 기능입니다.", Toast.LENGTH_SHORT).show();
            }
        });

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
    
    @Override
    public void showRecipes(List<Recipe> recipes) {
        // This method is now handled by updateRecipeListUI in observeViewModel.
    }

    @Override
    public void addChipToGroup(String text) {
        // This method is now handled by updateChipsUI in observeViewModel.
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
    public void showLoadingIndicator() { /* TODO: Implement loading indicator */ }

    @Override
    public void hideLoadingIndicator() { /* TODO: Implement loading indicator */ }

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
