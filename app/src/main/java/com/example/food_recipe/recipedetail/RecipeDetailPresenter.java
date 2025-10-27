package com.example.food_recipe.recipedetail;

import com.example.food_recipe.model.Recipe;

/**
 * 레시피 상세 화면의 비즈니스 로직을 처리하고, View와 Model 간의 상호작용을 중재하는 Presenter 클래스입니다.
 * MVP 패턴에서 'Presenter'의 역할을 수행하며, {@link RecipeDetailContract.Presenter} 인터페이스를 구현합니다.
 */
public class RecipeDetailPresenter implements RecipeDetailContract.Presenter {

    /**
     * Presenter가 제어할 View에 대한 참조입니다.
     * 메모리 누수 방지를 위해 View가 파괴될 때 null로 설정될 수 있습니다.
     */
    private RecipeDetailContract.View view;

    /**
     * 데이터를 실제로 가져오는 Model에 대한 참조입니다.
     */
    private RecipeDetailModel model;

    /**
     * RecipeDetailPresenter의 생성자입니다.
     * View와 Model을 초기화하고 연결합니다.
     *
     * @param view 이 Presenter와 연결될 View의 인스턴스.
     */
    public RecipeDetailPresenter(RecipeDetailContract.View view) {
        this.view = view;
        this.model = new RecipeDetailModel(); // Model 인스턴스 생성
    }

    /**
     * View로부터 레시피 데이터 로드 요청을 받아 처리합니다.
     * Model에게 데이터 조회를 요청하고, 결과를 콜백으로 받아 View에 전달합니다.
     *
     * @param rcpSno 불러올 레시피의 고유 식별 번호.
     */
    @Override
    public void loadRecipe(String rcpSno) {
        if (view != null) {
            view.showLoading(); // 데이터 로딩 시작을 View에 알림
        }

        // Model에게 데이터 조회를 요청하고, 콜백 리스너를 등록합니다.
        model.getRecipe(rcpSno, new RecipeDetailModel.OnRecipeListener() {
            /**
             * Model로부터 데이터를 성공적으로 받았을 때 호출됩니다.
             * @param recipe 조회된 레시피 객체.
             */
            @Override
            public void onSuccess(Recipe recipe) {
                if (view != null) {
                    view.showRecipe(recipe); // 조회된 데이터를 View에 전달하여 표시
                    view.hideLoading();      // 데이터 로딩 완료를 View에 알림
                }
            }

            /**
             * Model로부터 오류 메시지를 받았을 때 호출됩니다.
             * @param message 발생한 오류 메시지.
             */
            @Override
            public void onError(String message) {
                if (view != null) {
                    view.showError(message); // 오류 메시지를 View에 전달하여 표시
                    view.hideLoading();      // 데이터 로딩 완료(실패)를 View에 알림
                }
            }
        });
    }

    /**
     * View가 파괴될 때 호출되어, Presenter가 가지고 있던 View의 참조를 해제합니다.
     * 이는 Activity나 Fragment가 메모리에서 해제될 때, Presenter가 이를 계속 붙잡고 있어 발생하는
     * 메모리 누수(Memory Leak)를 방지하는 중요한 역할을 합니다.
     */
    @Override
    public void detachView() {
        this.view = null;
    }
}
