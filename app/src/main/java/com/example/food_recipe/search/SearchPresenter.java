package com.example.food_recipe.search;

import com.example.food_recipe.model.Recipe;

import java.util.List;

/**
 * 검색 화면의 비즈니스 로직을 처리하고, View와 Model 간의 상호작용을 중재하는 Presenter 클래스입니다.
 * MVP 패턴에서 'Presenter'의 역할을 수행하며, {@link SearchContract.Presenter} 인터페이스를 구현합니다.
 */
public class SearchPresenter implements SearchContract.Presenter {

    /**
     * Presenter가 제어할 View에 대한 참조입니다.
     */
    private final SearchContract.View view;

    /**
     * 데이터를 실제로 가져오는 Model에 대한 참조입니다.
     * Presenter는 Model의 구체적인 구현(Algolia, Firestore 등)을 알지 못하고, 오직 인터페이스에만 의존합니다.
     */
    private final SearchContract.Model model;

    /**
     * SearchPresenter의 생성자입니다.
     * View를 전달받아 멤버 변수에 할당하고, Model 인스턴스를 생성합니다.
     *
     * @param view 이 Presenter와 연결될 View의 인스턴스.
     */
    public SearchPresenter(SearchContract.View view) {
        this.view = view;
        this.model = new SearchModel(); // 데이터 처리를 담당할 Model을 생성
    }

    /**
     * View로부터 검색 요청을 받으면, 직접 처리하지 않고 Model에게 검색을 위임(delegate)합니다.
     * Model로부터 결과를 비동기적으로 받아 View에 업데이트하도록 지시합니다.
     *
     * @param query 사용자가 입력한 검색어.
     */
    @Override
    public void search(String query) {
        view.showLoadingIndicator(); // 데이터 로딩 시작을 View에 알림
        model.searchRecipes(query, new SearchContract.Model.OnRecipesFetchedListener() {
            /**
             * Model이 데이터 조회를 성공했을 때 호출되는 콜백 메소드.
             * @param recipes 조회된 레시피 목록.
             */
            @Override
            public void onSuccess(List<Recipe> recipes) {
                view.hideLoadingIndicator(); // 로딩 완료를 View에 알림
                view.showRecipes(recipes);   // 조회된 데이터를 View에 전달하여 표시
            }

            /**
             * Model이 데이터 조회 중 오류를 발생시켰을 때 호출되는 콜백 메소드.
             * @param message 발생한 오류 메시지.
             */
            @Override
            public void onError(String message) {
                view.hideLoadingIndicator(); // 로딩 완료(실패)를 View에 알림
                view.showError(message);     // 오류 메시지를 View에 전달하여 표시
            }
        });
    }

    /**
     * View가 처음 생성되었을 때(또는 준비되었을 때) 호출됩니다.
     * 초기 화면 구성을 위해 빈 검색어로 검색을 요청하여, Algolia에 설정된 랭킹 기반의
     * 인기/추천 레시피 목록을 가져오도록 합니다.
     */
    @Override
    public void start() {
        // model.fetchInitialRecipes() 대신, search("")를 호출하여 초기 화면에도 Algolia 랭킹을 사용합니다.
        search("");
    }
}
