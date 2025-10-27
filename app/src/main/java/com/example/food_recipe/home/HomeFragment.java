package com.example.food_recipe.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
 * 앱의 메인 화면(홈)을 담당하는 UI 컨트롤러 클래스(Fragment)입니다.
 * MVP 패턴에서 'View'의 역할을 수행하며, {@link HomeContract.View} 인터페이스를 구현합니다.
 * 사용자에게 인기 레시피와 추천 레시피 목록을 보여주는 역할을 합니다.
 */
public class HomeFragment extends Fragment implements HomeContract.View, RecipeAdapter.OnItemClickListener {

    /**
     * 이 View와 상호작용하는 Presenter에 대한 참조입니다.
     * View는 Presenter를 통해 비즈니스 로직 처리를 요청합니다.
     */
    private HomeContract.Presenter presenter;

    private TextView titleTextView;
    private RecyclerView recommendedRecyclerView;
    private RecyclerView popularRecyclerView;
    private RecipeAdapter recommendedAdapter;
    private RecipeAdapter popularAdapter;

    /**
     * Fragment의 UI가 처음 생성될 때 호출됩니다.
     * 여기서는 `fragment_main.xml` 레이아웃을 인플레이트하여 Fragment의 View를 생성합니다.
     *
     * @param inflater           레이아웃을 인플레이트하기 위한 LayoutInflater 객체.
     * @param container          Fragment의 UI가 들어갈 부모 ViewGroup.
     * @param savedInstanceState Fragment 상태 복원을 위한 Bundle 객체.
     * @return 생성된 Fragment의 루트 View.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    /**
     * `onCreateView`가 완료된 후, Fragment의 View가 완전히 생성되었을 때 호출됩니다.
     * 이 메소드에서 UI 컴포넌트 초기화, RecyclerView 설정, Presenter 생성 및 데이터 로딩 시작 등의 작업을 수행합니다.
     *
     * @param view               `onCreateView`에서 반환된 View 객체.
     * @param savedInstanceState Fragment 상태 복원을 위한 Bundle 객체.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // UI 컴포넌트 참조 초기화
        titleTextView = view.findViewById(R.id.fmain_home_title);
        recommendedRecyclerView = view.findViewById(R.id.fmain_rv_recommended);
        popularRecyclerView = view.findViewById(R.id.fmain_rv_popular);

        // RecyclerView 설정
        setupRecyclerViews();

        // Presenter를 생성하고, 데이터 로딩을 시작하도록 요청
        presenter = new HomePresenter(this, new HomeModel());
        presenter.start();
    }

    /**
     * 추천 및 인기 레시피 목록을 표시할 RecyclerView들을 초기화하고 설정합니다.
     * 각 RecyclerView에 LayoutManager와 Adapter를 설정하고, 아이템 클릭 리스너를 연결합니다.
     */
    private void setupRecyclerViews() {
        // 추천 레시피 RecyclerView 설정
        recommendedRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recommendedAdapter = new RecipeAdapter(getContext());
        recommendedAdapter.setOnItemClickListener(this); // 프래그먼트가 클릭 이벤트를 직접 처리하도록 설정
        recommendedRecyclerView.setAdapter(recommendedAdapter);

        // 인기 레시피 RecyclerView 설정
        popularRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        popularAdapter = new RecipeAdapter(getContext());
        popularAdapter.setOnItemClickListener(this); // 프래그먼트가 클릭 이벤트를 직접 처리하도록 설정
        popularRecyclerView.setAdapter(popularAdapter);
    }

    /**
     * {@link RecipeAdapter.OnItemClickListener} 인터페이스의 구현부입니다.
     * RecyclerView의 레시피 아이템이 클릭되었을 때 호출됩니다.
     *
     * @param recipe 클릭된 {@link Recipe} 객체.
     */
    @Override
    public void onItemClick(Recipe recipe) {
        // 레시피 객체와 ID가 유효한지 확인하여 NullPointerException을 방지
        if (recipe != null && recipe.getRcpSno() != null && !recipe.getRcpSno().isEmpty()) {
            Bundle bundle = new Bundle();
            bundle.putString("rcpSno", recipe.getRcpSno());

            // Navigation Component를 사용하여 레시피 상세 화면으로 이동
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_home_fragment_to_recipeDetailFragment, bundle);
        } else {
            // 만약의 경우를 대비한 방어 코드
            Toast.makeText(getContext(), "레시피 정보가 부족하여 이동할 수 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Presenter로부터 받은 인기 레시피 목록을 화면에 표시합니다.
     *
     * @param recipes 화면에 표시할 인기 레시피 데이터 리스트.
     */
    @Override
    public void showPopularRecipes(List<Recipe> recipes) {
        popularAdapter.setRecipes(recipes);
    }

    /**
     * Presenter로부터 받은 추천 레시피 목록을 화면에 표시합니다.
     *
     * @param recipes 화면에 표시할 추천 레시피 데이터 리스트.
     */
    @Override
    public void showRecommendedRecipes(List<Recipe> recipes) {
        recommendedAdapter.setRecipes(recipes);
    }

    /**
     * Presenter로부터 받은 사용자 이름으로 맞춤형 인사말을 화면에 표시합니다.
     *
     * @param username 로그인한 사용자의 이름. null이거나 비어있을 수 있습니다.
     */
    @Override
    public void showPersonalizedGreeting(@Nullable String username) {
        if (username != null && !username.isEmpty()) {
            titleTextView.setText(username + "님, 오늘 뭐 먹을까?");
        } else {
            titleTextView.setText("오늘 뭐 먹을까?");
        }
    }

    /**
     * Presenter로부터 받은 에러 메시지를 사용자에게 Toast 메시지로 보여줍니다.
     *
     * @param message 표시할 에러 메시지.
     */
    @Override
    public void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 데이터 로딩 시작 시 로딩 인디케이터를 화면에 표시합니다. (현재는 비어 있음)
     */
    @Override
    public void showLoadingIndicator() {
        // TODO: 로딩 인디케이터 UI 구현
    }

    /**
     * 데이터 로딩 완료 시 로딩 인디케이터를 화면에서 숨깁니다. (현재는 비어 있음)
     */
    @Override
    public void hideLoadingIndicator() {
        // TODO: 로딩 인디케이터 UI 구현
    }
}
