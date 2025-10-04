package com.example.food_recipe.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.food_recipe.R;
import com.example.food_recipe.adapter.RecipeAdapter; // [수정] import 경로를 올바르게 수정합니다.
import com.example.food_recipe.model.Recipe;

import java.util.List;

/**
 * [수정] 홈 화면의 UI를 담당하는 View입니다.
 * 이제 개인화된 환영 인사를 표시하는 기능까지 포함합니다.
 */
public class HomeFragment extends Fragment implements HomeContract.View {

    private HomeContract.Presenter presenter;

    // UI 요소
    private TextView titleTextView;
    private RecyclerView recommendedRecyclerView;
    private RecyclerView popularRecyclerView;
    private RecipeAdapter recommendedAdapter;
    private RecipeAdapter popularAdapter;
    private ProgressBar loadingIndicator;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // UI 컴포넌트를 초기화합니다.
        titleTextView = view.findViewById(R.id.fmain_home_title);
        recommendedRecyclerView = view.findViewById(R.id.fmain_rv_recommended);
        popularRecyclerView = view.findViewById(R.id.fmain_rv_popular);

        // RecyclerView를 설정합니다.
        setupRecyclerViews();

        // [수정] Presenter 생성 시, HomeModel 인스턴스를 함께 전달합니다.
        presenter = new HomePresenter(this, new HomeModel());
        presenter.start();
    }

    private void setupRecyclerViews() {
        recommendedRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recommendedAdapter = new RecipeAdapter(getContext());
        recommendedRecyclerView.setAdapter(recommendedAdapter);

        popularRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        popularAdapter = new RecipeAdapter(getContext());
        popularRecyclerView.setAdapter(popularAdapter);
    }

    // === HomeContract.View 구현 ===

    @Override
    public void showRecipes(List<Recipe> recipes) {
        recommendedAdapter.setRecipes(recipes);
        popularAdapter.setRecipes(recipes);
    }

    /**
     * [구현] 개인화된 환영 인사를 TextView에 설정합니다.
     */
    @Override
    public void showPersonalizedGreeting(@Nullable String username) {
        if (username != null && !username.isEmpty()) {
            titleTextView.setText(username + "님, 오늘 뭐 먹을까?");
        } else {
            titleTextView.setText("오늘 뭐 먹을까?");
        }
    }

    @Override
    public void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void showLoadingIndicator() {
        // TODO: 로딩 인디케이터 UI 구현
    }

    @Override
    public void hideLoadingIndicator() {
        // TODO: 로딩 인디케이터 UI 구현
    }
}
