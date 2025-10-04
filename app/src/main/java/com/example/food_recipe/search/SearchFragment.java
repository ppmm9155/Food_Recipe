// [기존 구조 유지] search 패키지 내에 SearchFragment 클래스를 유지합니다.
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.food_recipe.R;
import com.example.food_recipe.adapter.RecipeAdapter;
import com.example.food_recipe.model.Recipe;

import java.util.List;

/**
 * [변경 없음] 검색 화면의 UI를 담당하는 View 클래스입니다.
 * MVP 패턴에서 'View'의 역할을 수행하며, SearchContract.View 인터페이스를 구현합니다.
 */
public class SearchFragment extends Fragment implements SearchContract.View {

    // [기존 코드 유지]
    private RecyclerView recyclerView;
    private RecipeAdapter recipeAdapter;
    private SearchView searchView;
    private SearchContract.Presenter presenter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // [기존 코드 유지]
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // [기존 코드 유지]
        recyclerView = view.findViewById(R.id.recycler_view_recipes);
        searchView = view.findViewById(R.id.search_view);
        recipeAdapter = new RecipeAdapter(requireContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(recipeAdapter);

        // [기존 코드 유지] Presenter 객체를 생성하고 View를 연결합니다.
        presenter = new SearchPresenter(this);

        // [추가] Presenter에게 시작 신호를 보내 초기 데이터 로드를 요청합니다.
        // 이 한 줄이 '시동을 거는' 역할을 하여, 초기 추천 레시피를 불러오게 합니다.
        presenter.start();

        // [기존 코드 유지] SearchView에 검색 리스너를 설정합니다.
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            /**
             * [추가] 사용자가 검색 버튼을 눌렀을 때 호출되는 메서드입니다.
             */
            @Override
            public boolean onQueryTextSubmit(String query) {
                // [추가] View는 어떤 로직도 처리하지 않고, 즉시 Presenter에게 검색을 요청합니다.
                presenter.search(query);
                // [추가] 검색창의 포커스를 해제하여 키보드가 자동으로 내려가도록 합니다.
                searchView.clearFocus();
                return true; // [추가] 이벤트가 처리되었음을 시스템에 알립니다.
            }

            /**
             * [추가] 검색창의 텍스트가 변경될 때마다 호출되는 메서드입니다.
             * (실시간 검색 기능이 필요 없으므로, 지금은 아무것도 하지 않습니다.)
             */
            @Override
            public boolean onQueryTextChange(String newText) {
                return false; // [추가] 이벤트가 처리되지 않았음을 시스템에 알립니다.
            }
        });
    }

    /**
     * [기존 코드 유지]
     */
    @Override
    public void showRecipes(List<Recipe> recipes) {
        recipeAdapter.setRecipes(recipes);
    }

    /**
     * [기존 코드 유지]
     */
    @Override
    public void showError(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    /**
     * [기존 코드 유지]
     */
    @Override
    public void showLoadingIndicator() {
        // TODO: 로딩 인디케이터를 보여주는 UI 로직 구현
    }

    /**
     * [기존 코드 유지]
     */
    @Override
    public void hideLoadingIndicator() {
        // TODO: 로딩 인디케이터를 숨기는 UI 로직 구현
    }
}