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
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.food_recipe.R;
import com.example.food_recipe.adapter.RecipeAdapter;
import com.example.food_recipe.model.Recipe;
import java.util.List;

/**
 * [변경] HomeContract.View 인터페이스를 구현하고, '최근 본/즐겨찾기' UI 로직을 추가합니다.
 */
public class HomeFragment extends Fragment implements HomeContract.View, RecipeAdapter.OnItemClickListener {

    private HomeContract.Presenter presenter;

    private TextView titleTextView;
    private RecyclerView recommendedRecyclerView;
    private RecyclerView popularRecyclerView;
    private RecyclerView recentFavRecyclerView; // [추가]
    private LinearLayout recentFavEmptyView; // [추가]

    private RecipeAdapter recommendedAdapter;
    private RecipeAdapter popularAdapter;
    private RecipeAdapter recentFavAdapter; // [추가]

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
        recentFavRecyclerView = view.findViewById(R.id.fmain_rv_recentfav_preview); // [추가]
        recentFavEmptyView = view.findViewById(R.id.fmain_empty_recentfav); // [추가]

        setupRecyclerViews();

        // [변경] Presenter 생성 시 Context를 전달합니다.
        presenter = new HomePresenter(this, requireContext());
        presenter.start();
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

        // [추가] '최근 본/즐겨찾기' RecyclerView 설정
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
            // [주의] 홈 화면에서는 여러 RecyclerView가 하나의 NavController를 공유하므로, action ID를 명시해야 합니다.
            // NavController가 현재 위치(home)를 기준으로 목적지를 찾을 수 있도록 action ID를 사용합니다.
            // 만약 ID가 없다면, 앱은 현재 그래프에서 해당 목적지를 찾지 못해 충돌할 수 있습니다.
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

    /**
     * [추가] Presenter로부터 받은 '최근 본/즐겨찾기' 목록을 화면에 표시합니다.
     */
    @Override
    public void showRecentAndFavorites(List<Recipe> recipes) {
        recentFavAdapter.setRecipes(recipes);
        recentFavRecyclerView.setVisibility(View.VISIBLE);
        recentFavEmptyView.setVisibility(View.GONE);
    }

    /**
     * [추가] '최근 본/즐겨찾기' 목록이 비어있을 때 "Empty State" UI를 표시합니다.
     */
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
