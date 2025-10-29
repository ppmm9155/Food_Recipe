package com.example.food_recipe.pantry;

import com.example.food_recipe.model.PantryItem;
import java.util.List;

/**
 * 냉장고(Pantry) 기능의 View와 Presenter 사이의 통신 규칙을 정의하는 계약(Contract) 인터페이스입니다.
 * MVP(Model-View-Presenter) 아키텍처 패턴을 따르며, View와 Presenter가 강하게 결합되지 않고
 * 이 인터페이스를 통해 상호작용하도록 설계되었습니다.
 */
public interface PantryContract {

    /**
     * View(PantryFragment)가 구현해야 하는 메서드들의 집합입니다.
     * Presenter는 이 인터페이스를 통해 View의 UI 상태를 제어합니다.
     * (예: 로딩 아이콘 표시, 데이터 목록 표시, 오류 메시지 표시 등)
     */
    interface View {
        /** 데이터 로딩이 시작되었음을 사용자에게 알리기 위해 로딩 인디케이터를 표시합니다. */
        void showLoading();

        /** 데이터 로딩이 완료되었을 때 로딩 인디케이터를 숨깁니다. */
        void hideLoading();

        /**
         * 성공적으로 불러온 재료 목록을 화면의 RecyclerView에 표시하도록 요청합니다.
         * @param pantryItems 표시할 재료 아이템의 리스트
         */
        void showPantryItems(List<PantryItem> pantryItems);

        /**
         * 불러온 재료 목록이 비어있을 경우, "냉장고가 비었어요"와 같은 화면을 표시하도록 요청합니다.
         */
        void showEmptyView();

        /**
         * 데이터 처리 중 오류가 발생했을 때 사용자에게 오류 메시지를 표시합니다.
         * @param message 표시할 오류 메시지 문자열
         */
        void showError(String message);
    }

    /**
     * Presenter(PantryPresenter)가 구현해야 하는 메서드들의 집합입니다.
     * View는 이 인터페이스를 통해 사용자의 액션이나 생명주기 이벤트를 Presenter에게 전달합니다.
     * (예: 화면 진입 시 데이터 로드 요청, 아이템 삭제 요청 등)
     */
    interface Presenter {
        /**
         * View가 화면에 표시할 재료 목록을 불러오도록 Presenter에게 요청합니다.
         * 이 메서드는 주로 View가 생성되거나 데이터 새로고침이 필요할 때 호출됩니다.
         */
        void loadPantryItems();

        /**
         * 사용자가 스와이프하여 재료 아이템을 삭제하도록 Presenter에게 요청합니다.
         * @param item 삭제할 PantryItem 객체
         */
        void deletePantryItem(PantryItem item);

        /**
         * View가 파괴되기 직전에 호출되어, Presenter가 View에 대한 참조를 안전하게 해제하도록 합니다.
         * 이는 메모리 누수를 방지하는 데 매우 중요합니다.
         */
        void detachView();
    }
}
