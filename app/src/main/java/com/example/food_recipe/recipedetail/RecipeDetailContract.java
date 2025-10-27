package com.example.food_recipe.recipedetail;

import com.example.food_recipe.model.Recipe;

/**
 * 레시피 상세 화면의 View와 Presenter 사이의 통신 규칙을 정의하는 계약(Contract) 인터페이스입니다.
 * 이 인터페이스는 View와 Presenter 간의 결합도를 낮추어, 각 컴포넌트의 독립적인 개발과 테스트를 용이하게 합니다.
 */
public interface RecipeDetailContract {

    /**
     * View가 반드시 구현해야 하는 기능 목록을 정의합니다.
     * Presenter는 이 인터페이스에 정의된 메소드만을 호출하여 View를 제어합니다.
     */
    interface View {
        /**
         * Presenter가 성공적으로 레시피 데이터를 가져왔을 때, View에게 데이터를 화면에 표시하라고 지시합니다.
         *
         * @param recipe 화면에 표시할 상세 레시피 데이터 객체.
         */
        void showRecipe(Recipe recipe);

        /**
         * 데이터 로딩이 시작될 때, View에게 로딩 인디케이터(예: ProgressBar)를 보여주라고 지시합니다.
         */
        void showLoading();

        /**
         * 데이터 로딩이 완료되면, View에게 로딩 인디케이터를 숨기라고 지시합니다.
         */
        void hideLoading();

        /**
         * 데이터 로딩 과정에서 오류가 발생했을 때, View에게 에러 메시지를 표시하라고 지시합니다.
         *
         * @param message 표시할 에러 메시지 문자열.
         */
        void showError(String message);
    }

    /**
     * Presenter가 반드시 구현해야 하는 기능 목록을 정의합니다.
     * View는 이 인터페이스에 정의된 메소드만을 호출하여 Presenter에게 작업을 요청합니다.
     */
    interface Presenter {
        /**
         * View가 특정 레시피의 상세 정보를 요청할 때 호출하는 메소드입니다.
         * Presenter는 이 요청을 받아 Model에게 데이터 로드를 지시합니다.
         *
         * @param rcpSno 불러올 레시피의 고유 식별 번호.
         */
        void loadRecipe(String rcpSno);

        /**
         * View가 파괴될 때(예: 화면 전환) 호출됩니다.
         * Presenter가 View에 대한 참조를 안전하게 해제하여 메모리 누수를 방지하기 위해 사용됩니다.
         */
        void detachView();
    }
}
