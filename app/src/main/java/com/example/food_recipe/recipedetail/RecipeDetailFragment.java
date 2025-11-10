package com.example.food_recipe.recipedetail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.food_recipe.R;
import com.example.food_recipe.adapter.CookingStepAdapter;
import com.example.food_recipe.adapter.IngredientAdapter;
import com.example.food_recipe.main.AuthViewModel;
import com.example.food_recipe.model.Ingredient;
import com.example.food_recipe.model.Recipe;

import java.util.ArrayList;
import java.util.List;

/**
 * [변경] 중앙 인증 관리(AuthViewModel) 시스템을 사용하도록 리팩토링합니다.
 */
public class RecipeDetailFragment extends Fragment implements RecipeDetailContract.View {

    private RecipeDetailContract.Presenter presenter;
    private String rcpSno;
    private AuthViewModel authViewModel;

    private NestedScrollView scrollView;
    private ProgressBar progressBar;
    private ImageView ivRecipeImage;
    private TextView tvTitle;
    private ImageView ivBookmark;
    private TextView tvServings;
    private TextView tvCookingTime;
    private TextView tvDifficulty;
    private RecyclerView rvIngredients;
    private IngredientAdapter ingredientAdapter;
    private RecyclerView rvCookingSteps;
    private CookingStepAdapter cookingStepAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            rcpSno = getArguments().getString("rcpSno");
        }
        // [변경] Presenter 생성 시 View를 넘기지 않음
        presenter = new RecipeDetailPresenter(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recipe_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // [추가] Presenter에 View를 연결
        presenter.attachView(this);

        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        scrollView = view.findViewById(R.id.fdetail_scrollview);
        progressBar = view.findViewById(R.id.fdetail_progress_bar);
        ivRecipeImage = view.findViewById(R.id.fdetail_iv_image);
        tvTitle = view.findViewById(R.id.fdetail_tv_title);
        ivBookmark = view.findViewById(R.id.fdetail_iv_bookmark);
        tvServings = view.findViewById(R.id.fdetail_tv_servings);
        tvCookingTime = view.findViewById(R.id.fdetail_tv_cooking_time);
        tvDifficulty = view.findViewById(R.id.fdetail_tv_difficulty);
        rvIngredients = view.findViewById(R.id.fdetail_rv_ingredients);
        rvCookingSteps = view.findViewById(R.id.fdetail_rv_cooking_steps);

        setupAdapters();
        setupBookmarkClickListener();
        observeAuthState();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // [변경] Presenter와의 연결을 끊어 메모리 누수를 방지
        presenter.detachView();
    }
    
    private void observeAuthState() {
        authViewModel.user.observe(getViewLifecycleOwner(), firebaseUser -> {
            if (rcpSno != null) {
                presenter.loadRecipe(rcpSno, firebaseUser != null);
            }
        });
    }

    private void setupBookmarkClickListener() {
        ivBookmark.setOnClickListener(v -> {
            if (authViewModel.user.getValue() != null) {
                presenter.onBookmarkClicked();
            } else {
                showBookmarkResult("로그인이 필요한 기능입니다.");
            }
        });
    }


    private void setupAdapters() {
        rvCookingSteps.setLayoutManager(new LinearLayoutManager(getContext()));
        cookingStepAdapter = new CookingStepAdapter(getContext());
        rvCookingSteps.setAdapter(cookingStepAdapter);
        setupIngredientsRecyclerView();
    }

    private void setupIngredientsRecyclerView() {
        rvIngredients.setLayoutManager(new GridLayoutManager(getContext(), 2));
        ingredientAdapter = new IngredientAdapter();
        rvIngredients.setAdapter(ingredientAdapter);
    }

    @Override
    public void showRecipe(Recipe recipe) {
        if (getContext() == null || recipe == null) return;
        Glide.with(getContext()).load(recipe.getImageUrl()).into(ivRecipeImage);
        tvTitle.setText(getDisplayText(recipe.getTitle(), "제목 없음"));
        tvServings.setText(getDisplayText(recipe.getServings(), "정보 없음"));
        tvCookingTime.setText(getDisplayText(recipe.getCookingTime(), "정보 없음"));
        tvDifficulty.setText(getDisplayText(recipe.getDifficulty(), "정보 없음"));
        updateIngredients(recipe.getIngredientsRaw());
        cookingStepAdapter.setSteps(recipe.getCookingSteps());
    }

    private void updateIngredients(String ingredientsRaw) {
        String ingredientsText = getDisplayText(ingredientsRaw, "재료 정보가 없습니다.");
        List<Ingredient> ingredientList = new ArrayList<>();

        if ("재료 정보가 없습니다.".equals(ingredientsText)) {
            ingredientList.add(new Ingredient(ingredientsText, ""));
        } else {
            String[] ingredients = ingredientsText.split("\\|");
            for (String chunk : ingredients) {
                String trimmedChunk = chunk.trim();
                if (trimmedChunk.isEmpty()) {
                    continue;
                }
                int lastSpaceIndex = trimmedChunk.lastIndexOf(' ');
                String name;
                String quantity;
                if (lastSpaceIndex == -1) {
                    name = trimmedChunk;
                    quantity = "";
                } else {
                    name = trimmedChunk.substring(0, lastSpaceIndex).trim();
                    quantity = trimmedChunk.substring(lastSpaceIndex + 1).trim();
                }
                ingredientList.add(new Ingredient(name, quantity));
            }
        }
        ingredientAdapter.setIngredients(ingredientList);
    }

    private String getDisplayText(String text, String fallback) {
        return (text != null && !text.isEmpty() && !"null".equalsIgnoreCase(text)) ? text : fallback;
    }

    @Override
    public void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        scrollView.setVisibility(View.GONE);
    }

    @Override
    public void hideLoading() {
        progressBar.setVisibility(View.GONE);
        scrollView.setVisibility(View.VISIBLE);
    }

    @Override
    public void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), "Error: " + message, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void setBookmarkState(boolean isBookmarked) {
        if (getContext() == null) return;
        if (isBookmarked) {
            ivBookmark.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_favorite_filled));
        } else {
            ivBookmark.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.outline_favorite_24));
        }
    }

    @Override
    public void showBookmarkResult(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
}
