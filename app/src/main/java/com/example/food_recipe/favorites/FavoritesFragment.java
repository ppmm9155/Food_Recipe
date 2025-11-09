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
import androidx.lifecycle.ViewModelProvider; // [추가]
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.food_recipe.R;
import com.example.food_recipe.adapter.RecipeAdapter;
import com.example.food_recipe.main.AuthViewModel; // [추가]
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

    // [추가] 공유 ViewModel
    private AuthViewModel authViewModel;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // [변경] Presenter는 Fragment의 생명주기 전체에 걸쳐 한 번만 생성됩니다.
        // 초기 View 연결은 attachView를 통해 이루어지므로 생성자에서는 this를 넘기지 않습니다.
        presenter = new FavoritesPresenter(this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favorites, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // [변경] 화면(View)이 생성될 때마다 Presenter와 다시 연결합니다.
        presenter.attachView(this);
        
        recyclerView = view.findViewById(R.id.rvFavorites);
        emptyView = view.findViewById(R.id.favorites_tv_empty);
        progressBar = view.findViewById(R.id.favorites_progress_bar);

        setupRecyclerView();

        // [추가] Activity 범위의 AuthViewModel 인스턴스를 가져옵니다.
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        // [추가] AuthViewModel의 사용자 상태 변화를 관찰합니다.
        observeAuthState();
        
        // [삭제] View가 준비된 후 데이터를 로드하도록 요청합니다.
        // presenter.start();
    }
    
    /**
     * [추가] AuthViewModel의 LiveData를 관찰하여 로그인 상태 변화에 따라 UI를 업데이트합니다.
     */
    private void observeAuthState() {
        authViewModel.user.observe(getViewLifecycleOwner(), firebaseUser -> {
            if (firebaseUser != null) {
                // 사용자가 로그인 상태이면, 즐겨찾기 목록을 불러옵니다.
                presenter.start();
            } else {
                // 사용자가 로그아웃 상태이면, 빈 화면을 표시하고 관련 UI를 처리합니다.
                hideLoading();
                showEmptyView();
                adapter.setRecipes(new ArrayList<>()); // [추가] 어댑터의 데이터도 비워줍니다.
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

    /**
     * [변경] 화면(View)이 파괴될 때 Presenter와의 연결을 끊습니다.
     * Presenter 자체는 Fragment가 완전히 파괴될 때까지 살아있습니다.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        presenter.detachView();
    }
}
