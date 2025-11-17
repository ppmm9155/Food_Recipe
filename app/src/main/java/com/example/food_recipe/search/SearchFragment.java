package com.example.food_recipe.search;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
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
import com.example.food_recipe.main.AuthViewModel;
import com.example.food_recipe.model.Recipe;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;
import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment implements SearchContract.View, RecipeAdapter.OnItemClickListener {

    private static final String TAG = "SearchFragment";

    private ChipGroup searchChipGroup;
    private MaterialButton searchBtnPantryImport;
    private RecyclerView recyclerView;
    private TextView emptyTextView;
    // [추가] 로딩 인디케이터
    private ProgressBar progressBar;
    private RecipeAdapter recipeAdapter;
    private SearchView searchView;
    private SearchContract.Presenter presenter;
    private SearchViewModel viewModel;
    private AuthViewModel authViewModel;

    public static final String REQUEST_KEY_PANTRY_IMPORT = "pantry_import_request";
    public static final String BUNDLE_KEY_SELECTED_INGREDIENTS = "selected_ingredients";
    public static final String BUNDLE_KEY_PANTRY_ITEMS = "pantry_items";
    public static final String BUNDLE_KEY_CURRENT_CHIPS = "current_chips";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(SearchViewModel.class);
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);
        presenter = new SearchPresenter(viewModel);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        presenter.attachView(this);

        setupViews(view);
        setupRecyclerView();
        setupListeners();
        setupFragmentResultListener();
        observeViewModel();

        presenter.start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        presenter.detachView();
    }

    private void setupViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_view_recipes);
        emptyTextView = view.findViewById(R.id.text_view_empty);
        searchView = view.findViewById(R.id.search_view);
        searchChipGroup = view.findViewById(R.id.search_chip_group);
        searchBtnPantryImport = view.findViewById(R.id.search_btn_pantry_import);
        // [추가] ProgressBar 초기화
        progressBar = view.findViewById(R.id.search_progress_bar);
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
        searchBtnPantryImport.setOnClickListener(v -> {
            if (authViewModel.user.getValue() != null) {
                presenter.onPantryImportButtonClicked();
            } else {
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
        // Handled by ViewModel
    }

    @Override
    public void addChipToGroup(String text) {
        // Handled by ViewModel
    }

    @Override
    public void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
            Log.e(TAG, "An error occurred: " + message);
        }
        recyclerView.setVisibility(View.GONE);
        emptyTextView.setVisibility(View.VISIBLE);
        emptyTextView.setText("오류가 발생했습니다: " + message);
    }

    /**
     * [수정] 검색 시작 시 로딩 인디케이터를 보여줍니다.
     */
    @Override
    public void showLoadingIndicator() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        emptyTextView.setVisibility(View.GONE);
    }

    /**
     * [수정] 검색 완료 시 로딩 인디케이터를 숨깁니다.
     */
    @Override
    public void hideLoadingIndicator() {
        progressBar.setVisibility(View.GONE);
        // [주석] RecyclerView와 EmptyTextView의 가시성은
        // 데이터 수신 후 updateRecipeListUI에서 처리하므로 여기서는 ProgressBar만 숨깁니다.
    }

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

    @Override
    public void showPantryEmptyActionSnackbar() {
        if (getView() == null) return;
        Snackbar.make(getView(), "냉장고에 재료가 없습니다.", Snackbar.LENGTH_LONG)
                .setAction("추가하기", v -> {
                    if (getActivity() != null) {
                        BottomNavigationView bottomNav = getActivity().findViewById(R.id.main_bottom_nav);
                        if (bottomNav != null) {
                            bottomNav.setSelectedItemId(R.id.nav_pantry);
                        }
                    }
                })
                .show();
    }
}
