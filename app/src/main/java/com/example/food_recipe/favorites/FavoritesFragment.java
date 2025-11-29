package com.example.food_recipe.favorites;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import java.util.ArrayList;

/**
 * [변경] 중앙 인증 관리(AuthViewModel) 시스템을 사용하도록 리팩토링합니다.
 */
public class FavoritesFragment extends Fragment implements FavoritesContract.View {

    private static final String TAG = "FavoritesFragment";

    private FavoritesContract.Presenter presenter;
    private RecyclerView recyclerView;
    private RecipeAdapter adapter;
    private TextView emptyView;
    private View progressBar;

    private AuthViewModel authViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // [변경] Presenter 생성 시 View를 넘기지 않음
        presenter = new FavoritesPresenter();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favorites, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // [변경] Presenter에 View를 연결
        presenter.attachView(this);
        
        recyclerView = view.findViewById(R.id.rvFavorites);
        emptyView = view.findViewById(R.id.favorites_tv_empty);
        progressBar = view.findViewById(R.id.favorites_progress_bar);

        setupRecyclerView();

        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);
        observeAuthState();
    }
    
    private void observeAuthState() {
        authViewModel.user.observe(getViewLifecycleOwner(), firebaseUser -> {
            if (firebaseUser != null) {
                presenter.start();
            } else {
                hideLoading();
                showEmptyView();
                if(adapter != null) {
                    adapter.setRecipes(new ArrayList<>());
                }
            }
        });
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new RecipeAdapter(getContext());
        adapter.setOnItemClickListener(recipe -> {
            if (recipe != null && recipe.getRcpSno() != null && !recipe.getRcpSno().isEmpty()) {
                try {
                    Bundle bundle = new Bundle();
                    bundle.putString("rcpSno", recipe.getRcpSno());
                    NavHostFragment.findNavController(this).navigate(R.id.action_favoritesFragment_to_recipeDetailFragment, bundle);
                } catch (Exception e) {
                    Log.e(TAG, "Navigation failed, even with valid data.", e);
                    Toast.makeText(getContext(), "화면 이동에 실패했습니다. 경로를 확인해주세요.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.e(TAG, "Navigation failed due to invalid recipe data.");
                Toast.makeText(getContext(), "레시피 정보가 올바르지 않아 이동할 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
        });
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void showBookmarkedRecipes(List<Recipe> recipes) {
        adapter.setRecipes(recipes);
        recyclerView.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);
    }

    @Override
    public void showLoading() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);
    }

    @Override
    public void hideLoading() {
        if (progressBar != null) progressBar.setVisibility(View.GONE);
    }

    @Override
    public void showEmptyView() {
        recyclerView.setVisibility(View.GONE);
        emptyView.setVisibility(View.VISIBLE);
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
        // [변경] Presenter와의 연결을 끊어 메모리 누수를 방지
        presenter.detachView();
    }
}
