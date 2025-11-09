package com.example.food_recipe.home;

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
import androidx.lifecycle.ViewModelProvider; // [추가]
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.food_recipe.R;
import com.example.food_recipe.adapter.RecipeAdapter;
import com.example.food_recipe.main.AuthViewModel; // [추가]
import com.example.food_recipe.model.Recipe;
import java.util.List;

/**
 * [변경] 중앙 인증 관리(AuthViewModel) 시스템을 사용하도록 리팩토링합니다.
 */
public class HomeFragment extends Fragment implements HomeContract.View, RecipeAdapter.OnItemClickListener {

    private HomeContract.Presenter presenter;
    private AuthViewModel authViewModel; // [추가]

    private TextView titleTextView;
    private RecyclerView recommendedRecyclerView;
    private RecyclerView popularRecyclerView;
    private RecyclerView recentFavRecyclerView;
    private LinearLayout recentFavEmptyView;

    private RecipeAdapter recommendedAdapter;
    private RecipeAdapter popularAdapter;
    private RecipeAdapter recentFavAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // UI 컴포넌트 참조 초기화
        titleTextView = view.findViewById(R.id.fmain_home_title);
        recommendedRecyclerView = view.findViewById(R.id.fmain_rv_recommended);
        popularRecyclerView = view.findViewById(R.id.fmain_rv_popular);
        recentFavRecyclerView = view.findViewById(R.id.fmain_rv_recentfav_preview);
        recentFavEmptyView = view.findViewById(R.id.fmain_empty_recentfav);

        setupRecyclerViews();

        presenter = new HomePresenter(this, requireContext());

        // [추가] Activity 범위의 AuthViewModel 인스턴스를 가져옵니다.
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        // [추가] AuthViewModel의 사용자 상태 변화를 관찰합니다.
        observeAuthState();
        
        // [삭제] presenter.start();
    }
    
    /**
     * [추가] AuthViewModel의 LiveData를 관찰하여 로그인 상태 변화에 따라 UI를 업데이트합니다.
     */
    private void observeAuthState() {
        authViewModel.user.observe(getViewLifecycleOwner(), firebaseUser -> {
            // Presenter에게 로그인 상태가 변경되었음을 알립니다.
            // Presenter가 로그인된 사용자에 맞는 데이터를 로드하거나,
            // 로그아웃 상태에 맞게 UI를 초기화하는 등의 작업을 처리합니다.
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
}
