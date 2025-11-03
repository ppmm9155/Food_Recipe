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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.food_recipe.R;
import com.example.food_recipe.adapter.CookingStepAdapter;
import com.example.food_recipe.model.Recipe;

/**
 * [변경] 즐겨찾기 관련 UI 이벤트 처리 및 업데이트 로직을 추가합니다.
 */
public class RecipeDetailFragment extends Fragment implements RecipeDetailContract.View {

    private RecipeDetailContract.Presenter presenter;
    private String rcpSno;

    private NestedScrollView scrollView;
    private ProgressBar progressBar;
    private ImageView ivRecipeImage;
    private TextView tvTitle;
    private ImageView ivBookmark;
    private TextView tvServings;
    private TextView tvCookingTime;
    private TextView tvDifficulty;
    private TextView tvIngredientsRaw;
    private RecyclerView rvCookingSteps;
    private CookingStepAdapter cookingStepAdapter;

    /**
     * [변경] Presenter 생성 시, SharedPreferences 접근에 필요한 Context를 전달합니다.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            rcpSno = getArguments().getString("rcpSno");
        }
        // Presenter에게 Context를 전달하여 RecentRecipeManager를 사용할 수 있도록 합니다.
        presenter = new RecipeDetailPresenter(this, requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recipe_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        scrollView = view.findViewById(R.id.fdetail_scrollview);
        progressBar = view.findViewById(R.id.fdetail_progress_bar);
        ivRecipeImage = view.findViewById(R.id.fdetail_iv_image);
        tvTitle = view.findViewById(R.id.fdetail_tv_title);
        ivBookmark = view.findViewById(R.id.fdetail_iv_bookmark);
        tvServings = view.findViewById(R.id.fdetail_tv_servings);
        tvCookingTime = view.findViewById(R.id.fdetail_tv_cooking_time);
        tvDifficulty = view.findViewById(R.id.fdetail_tv_difficulty);
        tvIngredientsRaw = view.findViewById(R.id.fdetail_tv_ingredients_raw);
        rvCookingSteps = view.findViewById(R.id.fdetail_rv_cooking_steps);

        setupRecyclerView();
        
        ivBookmark.setOnClickListener(v -> presenter.onBookmarkClicked());

        if (rcpSno != null) {
            presenter.loadRecipe(rcpSno);
        }
    }

    private void setupRecyclerView() {
        rvCookingSteps.setLayoutManager(new LinearLayoutManager(getContext()));
        cookingStepAdapter = new CookingStepAdapter(getContext());
        rvCookingSteps.setAdapter(cookingStepAdapter);
    }

    @Override
    public void showRecipe(Recipe recipe) {
        if (getContext() == null || recipe == null) return;
        Glide.with(getContext()).load(recipe.getImageUrl()).into(ivRecipeImage);
        tvTitle.setText(getDisplayText(recipe.getTitle(), "제목 없음"));
        tvServings.setText(getDisplayText(recipe.getServings(), "정보 없음"));
        tvCookingTime.setText(getDisplayText(recipe.getCookingTime(), "정보 없음"));
        tvDifficulty.setText(getDisplayText(recipe.getDifficulty(), "정보 없음"));
        tvIngredientsRaw.setText(getDisplayText(recipe.getIngredientsRaw(), "재료 정보가 없습니다."));
        cookingStepAdapter.setSteps(recipe.getCookingSteps());
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        presenter.detachView();
    }
}
