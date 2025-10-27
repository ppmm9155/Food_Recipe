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
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.food_recipe.R;
import com.example.food_recipe.adapter.CookingStepAdapter;
import com.example.food_recipe.model.Recipe;

/**
 * 레시피의 상세 정보를 보여주는 UI 컨트롤러 클래스(Fragment)입니다.
 * MVP 패턴에서 'View'의 역할을 수행하며, {@link RecipeDetailContract.View} 인터페이스를 구현합니다.
 */
public class RecipeDetailFragment extends Fragment implements RecipeDetailContract.View {

    private RecipeDetailContract.Presenter presenter;
    private String rcpSno;

    // UI 컴포넌트 참조 변수
    private NestedScrollView scrollView;
    private ProgressBar progressBar;
    private ImageView ivRecipeImage;
    private TextView tvTitle;
    private TextView tvServings;
    private TextView tvCookingTime;
    private TextView tvDifficulty;
    private TextView tvIngredientsRaw;
    private RecyclerView rvCookingSteps;
    private CookingStepAdapter cookingStepAdapter;

    /**
     * Fragment가 생성될 때 호출됩니다.
     * 여기서는 이전 화면으로부터 전달받은 레시피 ID(rcpSno)를 추출하고 Presenter를 초기화합니다.
     *
     * @param savedInstanceState Fragment 상태 복원을 위한 Bundle 객체.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            rcpSno = getArguments().getString("rcpSno");
        }
        presenter = new RecipeDetailPresenter(this);
    }

    /**
     * Fragment의 UI가 처음 생성될 때 호출됩니다.
     * `fragment_recipe_detail.xml` 레이아웃을 인플레이트하여 View를 생성합니다.
     *
     * @param inflater           레이아웃을 인플레이트하기 위한 LayoutInflater 객체.
     * @param container          Fragment의 UI가 들어갈 부모 ViewGroup.
     * @param savedInstanceState Fragment 상태 복원을 위한 Bundle 객체.
     * @return 생성된 Fragment의 루트 View.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recipe_detail, container, false);
    }

    /**
     * Fragment의 View가 완전히 생성되었을 때 호출됩니다.
     * UI 컴포넌트 초기화, RecyclerView 설정, Presenter를 통한 데이터 로딩 요청을 수행합니다.
     *
     * @param view               `onCreateView`에서 반환된 View 객체.
     * @param savedInstanceState Fragment 상태 복원을 위한 Bundle 객체.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // UI 컴포넌트 참조 초기화
        scrollView = view.findViewById(R.id.fdetail_scrollview);
        progressBar = view.findViewById(R.id.fdetail_progress_bar);
        ivRecipeImage = view.findViewById(R.id.fdetail_iv_image);
        tvTitle = view.findViewById(R.id.fdetail_tv_title);
        tvServings = view.findViewById(R.id.fdetail_tv_servings);
        tvCookingTime = view.findViewById(R.id.fdetail_tv_cooking_time);
        tvDifficulty = view.findViewById(R.id.fdetail_tv_difficulty);
        tvIngredientsRaw = view.findViewById(R.id.fdetail_tv_ingredients_raw);
        rvCookingSteps = view.findViewById(R.id.fdetail_rv_cooking_steps);

        setupRecyclerView();

        // 레시피 ID가 유효하면 Presenter에게 데이터 로딩을 요청
        if (rcpSno != null) {
            presenter.loadRecipe(rcpSno);
        }
    }

    /**
     * 요리 단계 목록을 표시할 RecyclerView를 초기화하고 설정합니다.
     */
    private void setupRecyclerView() {
        rvCookingSteps.setLayoutManager(new LinearLayoutManager(getContext()));
        cookingStepAdapter = new CookingStepAdapter(getContext());
        rvCookingSteps.setAdapter(cookingStepAdapter);
    }

    /**
     * Presenter로부터 받은 레시피 상세 정보를 UI에 바인딩합니다.
     *
     * @param recipe 화면에 표시할 {@link Recipe} 객체.
     */
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

    /**
     * 표시할 텍스트가 유효한지 확인하고, 유효하지 않을 경우 대체 텍스트를 반환하는 헬퍼 메소드입니다.
     *
     * @param text     표시할 원본 텍스트.
     * @param fallback 원본 텍스트가 유효하지 않을 경우 사용할 대체 텍스트.
     * @return 표시할 최종 텍스트.
     */
    private String getDisplayText(String text, String fallback) {
        return (text != null && !text.isEmpty() && !"null".equalsIgnoreCase(text)) ? text : fallback;
    }

    /**
     * 데이터 로딩 시작 시 로딩 인디케이터(ProgressBar)를 보여주고, 메인 콘텐츠는 숨깁니다.
     */
    @Override
    public void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        scrollView.setVisibility(View.GONE);
    }

    /**
     * 데이터 로딩 완료 시 로딩 인디케이터를 숨기고, 메인 콘텐츠를 보여줍니다.
     */
    @Override
    public void hideLoading() {
        progressBar.setVisibility(View.GONE);
        scrollView.setVisibility(View.VISIBLE);
    }

    /**
     * Presenter로부터 받은 에러 메시지를 사용자에게 Toast 메시지로 보여줍니다.
     *
     * @param message 표시할 에러 메시지.
     */
    @Override
    public void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), "Error: " + message, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Fragment의 View가 파괴될 때 호출됩니다.
     * Presenter에게 View가 detach되었음을 알려 메모리 누수를 방지합니다.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        presenter.detachView();
    }
}
