package com.example.food_recipe.search;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.food_recipe.R;
import com.example.food_recipe.adapter.RecipeAdapter;
import com.example.food_recipe.model.Recipe;

import java.util.List;

/**
 * 레시피 검색 화면의 UI를 담당하는 컨트롤러 클래스(Fragment)입니다.
 * MVP 패턴에서 'View'의 역할을 수행하며, {@link SearchContract.View} 인터페이스를 구현합니다.
 * 또한, 검색 결과 목록의 클릭 이벤트를 직접 처리하기 위해 {@link RecipeAdapter.OnItemClickListener}를 구현합니다.
 */
public class SearchFragment extends Fragment implements SearchContract.View, RecipeAdapter.OnItemClickListener {

    private RecyclerView recyclerView;
    private RecipeAdapter recipeAdapter;
    private SearchView searchView;
    private SearchContract.Presenter presenter;

    /**
     * Fragment의 UI가 처음 생성될 때 호출됩니다.
     * `fragment_search.xml` 레이아웃을 인플레이트하여 View를 생성합니다.
     *
     * @param inflater           레이아웃을 인플레이트하기 위한 LayoutInflater 객체.
     * @param container          Fragment의 UI가 들어갈 부모 ViewGroup.
     * @param savedInstanceState Fragment 상태 복원을 위한 Bundle 객체.
     * @return 생성된 Fragment의 루트 View.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    /**
     * Fragment의 View가 완전히 생성되었을 때 호출됩니다.
     * UI 컴포넌트 초기화, RecyclerView 설정, Presenter 생성 및 초기 데이터 로딩 시작 등의 작업을 수행합니다.
     *
     * @param view               `onCreateView`에서 반환된 View 객체.
     * @param savedInstanceState Fragment 상태 복원을 위한 Bundle 객체.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // UI 컴포넌트 참조 초기화
        recyclerView = view.findViewById(R.id.recycler_view_recipes);
        searchView = view.findViewById(R.id.search_view);
        
        // RecyclerView와 Adapter 설정
        recipeAdapter = new RecipeAdapter(requireContext());
        recipeAdapter.setOnItemClickListener(this); // 프래그먼트가 클릭 이벤트를 직접 처리하도록 설정
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(recipeAdapter);

        // Presenter를 생성하고, 초기 데이터 로딩을 시작하도록 요청
        presenter = new SearchPresenter(this);
        presenter.start();

        // SearchView에 대한 쿼리 리스너 설정
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            /**
             * 사용자가 키보드의 검색 버튼을 누르거나 검색 아이콘을 클릭했을 때 호출됩니다.
             * @param query 사용자가 입력한 검색어.
             * @return 이벤트가 처리되었으면 true.
             */
            @Override
            public boolean onQueryTextSubmit(String query) {
                presenter.search(query); // Presenter에게 검색 요청
                searchView.clearFocus();   // 검색 후 키보드를 숨김
                return true;
            }

            /**
             * 검색창의 텍스트가 변경될 때마다 호출됩니다. (실시간 검색 기능에 사용)
             * @param newText 변경된 텍스트.
             * @return 이벤트가 처리되었으면 true.
             */
            @Override
            public boolean onQueryTextChange(String newText) {
                // 현재 앱에서는 실시간 검색을 사용하지 않으므로 false 반환
                return false;
            }
        });
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
                    .navigate(R.id.action_searchFragment_to_recipeDetailFragment, bundle);
        } else {
            // 만약의 경우를 대비한 방어 코드
            Toast.makeText(getContext(), "레시피 정보가 없어 이동할 수 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Presenter로부터 받은 레시피 목록을 RecyclerView에 표시합니다.
     *
     * @param recipes 화면에 표시할 레시피 데이터 리스트.
     */
    @Override
    public void showRecipes(List<Recipe> recipes) {
        recipeAdapter.setRecipes(recipes);
    }

    /**
     * Presenter로부터 받은 에러 메시지를 사용자에게 Toast 메시지로 보여줍니다.
     *
     * @param message 표시할 에러 메시지.
     */
    @Override
    public void showError(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
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
