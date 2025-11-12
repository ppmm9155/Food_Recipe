package com.example.food_recipe.home;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.food_recipe.R;
import com.example.food_recipe.adapter.RecipeAdapter;
import com.example.food_recipe.main.AuthViewModel;
import com.example.food_recipe.model.Recipe;
import java.util.List;

/**
 * [기존 주석 유지]
 */
public class HomeFragment extends Fragment implements HomeContract.View, RecipeAdapter.OnItemClickListener {

    private HomeContract.Presenter presenter;
    private AuthViewModel authViewModel;

    private TextView titleTextView;
    private RecyclerView recommendedRecyclerView;
    private RecyclerView popularRecyclerView;
    private RecyclerView recentFavRecyclerView;
    private LinearLayout recentFavEmptyView;

    private RecipeAdapter recommendedAdapter;
    private RecipeAdapter popularAdapter;
    private RecipeAdapter recentFavAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        presenter = new HomePresenter();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        presenter.attachView(this);

        titleTextView = view.findViewById(R.id.fmain_home_title);
        recommendedRecyclerView = view.findViewById(R.id.fmain_rv_recommended);
        popularRecyclerView = view.findViewById(R.id.fmain_rv_popular);
        recentFavRecyclerView = view.findViewById(R.id.fmain_rv_recentfav_preview);
        recentFavEmptyView = view.findViewById(R.id.fmain_empty_recentfav);

        setupRecyclerViews();

        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);
        observeAuthState();
    }

    private void observeAuthState() {
        authViewModel.user.observe(getViewLifecycleOwner(), firebaseUser -> {
            presenter.onAuthStateChanged(firebaseUser != null);
        });
    }

    private void setupRecyclerViews() {
        recommendedRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recommendedAdapter = new RecipeAdapter(getContext());
        recommendedAdapter.setOnItemClickListener(this);
        recommendedRecyclerView.setAdapter(recommendedAdapter);

        popularRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        popularAdapter = new RecipeAdapter(getContext());
        popularAdapter.setOnItemClickListener(this);
        popularRecyclerView.setAdapter(popularAdapter);

        recentFavRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recentFavAdapter = new RecipeAdapter(getContext());
        recentFavAdapter.setOnItemClickListener(this);
        recentFavRecyclerView.setAdapter(recentFavAdapter);
    }

    @Override
    public void onItemClick(Recipe recipe) {
        if (recipe != null && recipe.getRcpSno() != null && !recipe.getRcpSno().isEmpty()) {
            Bundle bundle = new Bundle();
            bundle.putString("rcpSno", recipe.getRcpSno());
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_home_fragment_to_recipeDetailFragment, bundle);
        } else {
            Toast.makeText(getContext(), "레시피 정보가 부족하여 이동할 수 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void showRecommendedRecipes(List<Recipe> recipes) {
        recommendedAdapter.setRecipes(recipes);
    }
    
    @Override
    public void showPopularRecipes(List<Recipe> recipes) {
        popularAdapter.setRecipes(recipes);
    }

    @Override
    public void showRecentAndFavorites(List<Recipe> recipes) {
        recentFavAdapter.setRecipes(recipes);
        recentFavRecyclerView.setVisibility(View.VISIBLE);
        recentFavEmptyView.setVisibility(View.GONE);
    }

    @Override
    public void showEmptyRecentAndFavorites() {
        recentFavRecyclerView.setVisibility(View.GONE);
        recentFavEmptyView.setVisibility(View.VISIBLE);
    }

    @Override
    public void setUserName(@Nullable String username) {
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
    public void onDestroyView() {
        super.onDestroyView();
        presenter.detachView();
    }
    
    /**
     * [수정] 무한 재귀 호출을 방지하기 위해 super.getContext()를 사용합니다.
     */
    @Override
    public Context getContext() {
        return super.getContext();
    }
}
