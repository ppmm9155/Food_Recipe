package com.example.food_recipe.search;

import com.example.food_recipe.model.Recipe;
import java.util.List;

/**
 * [추가] 검색 기능의 View와 Presenter 사이의 통신 규칙을 정의하는 계약(Contract) 인터페이스입니다.
 * 이 파일을 통해 View와 Presenter는 서로를 직접 참조하지 않고, 오직 이 '계약서'에 명시된 기능만을 호출하게 됩니다.
 * 이를 통해 코드의 결합도를 낮추고(느슨한 결합), 각 컴포넌트의 독립적인 테스트와 수정이 용이해집니다.
 */
public interface SearchContract {

    /**
     * [추가] View가 반드시 구현해야 하는 기능 목록을 정의합니다.
     * Presenter는 여기에 정의된 메서드만을 호출하여 View를 제어할 수 있습니다.
     */
    interface View {
        /**
         * [추가] Presenter가 데이터를 성공적으로 가져왔을 때, View에게 레시피 목록을 화면에 표시하라고 지시합니다.
         * @param recipes 화면에 표시할 레시피 데이터 리스트
         */
        void showRecipes(List<Recipe> recipes);

        /**
         * [추가] 데이터 로딩 중이나 검색 과정에서 오류가 발생했을 때, View에게 에러 메시지를 표시하라고 지시합니다.
         * (지금 당장 구현하지는 않지만, 확장을 위해 미리 정의해 둡니다.)
         * @param message 표시할 에러 메시지
         */
        void showError(String message);

        /**
         * [추가] 데이터 로딩이 시작될 때, View에게 로딩 인디케이터(예: ProgressBar)를 보여주라고 지시합니다.
         * (지금 당장 구현하지는 않지만, 확장을 위해 미리 정의해 둡니다.)
         */
        void showLoadingIndicator();

        /**
         * [추가] 데이터 로딩이 완료되면, View에게 로딩 인디케이터를 숨기라고 지시합니다.
         * (지금 당장 구현하지는 않지만, 확장을 위해 미리 정의해 둡니다.)
         */
        void hideLoadingIndicator();
    }

    /**
     * [추가] Presenter가 반드시 구현해야 하는 기능 목록을 정의합니다.
     * View는 여기에 정의된 메서드만을 호출하여 Presenter에게 작업을 요청할 수 있습니다.
     */
    interface Presenter {
        /**
         * [추가] View(SearchFragment)가 사용자의 검색어 입력을 감지했을 때 호출하는 메서드입니다.
         * Presenter에게 해당 검색어로 데이터 검색을 시작하라고 요청합니다.
         * @param query 사용자가 입력한 검색어
         */
        void search(String query);

        /**
         * [추가] Presenter가 View와 연결될 때 호출될 메서드입니다.
         * (지금 당장 사용하지는 않지만, Presenter의 생명주기 관리를 위해 미리 정의해 둡니다.)
         */
        void start();
    }
}
