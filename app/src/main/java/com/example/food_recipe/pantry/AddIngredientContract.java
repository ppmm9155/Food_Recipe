package com.example.food_recipe.pantry;

import java.util.Calendar;

/**
 * 재료 추가 기능(Add Ingredient)의 View와 Presenter 사이의 통신 규칙을 정의하는 계약(Contract) 인터페이스입니다.
 * MVP(Model-View-Presenter) 아키텍처 패턴을 따르며, View와 Presenter가 서로를 직접 참조하지 않고
 * 이 인터페이스를 통해 상호작용하도록 하여 결합도를 낮춥니다.
 */
public interface AddIngredientContract {

    /**
     * View가 구현해야 하는 메서드들의 집합입니다.
     * Presenter는 이 인터페이스를 통해 View를 제어하며, UI 업데이트를 요청합니다.
     * (예: 오류 메시지 표시, 화면 닫기 등)
     */
    interface View {
        /** 재료 이름이 입력되지 않았을 때 사용자에게 오류를 알립니다. */
        void showNameEmptyError();

        /** 재료 수량이 입력되지 않았을 때 사용자에게 오류를 알립니다. */
        void showQuantityEmptyError();

        /**
         * 재료 저장이 성공적으로 완료되었음을 View에 알립니다.
         * @param ingredientName 성공적으로 추가된 재료의 이름
         */
        void onSaveSuccess(String ingredientName);

        /**
         * BottomSheet를 닫도록 View에 요청합니다.
         */
        void closeBottomSheet();

        /**
         * 재료 추가가 성공했음을 부모 프래그먼트(PantryFragment)에 알리도록 View에 요청합니다.
         * Fragment Result API를 사용하여 결과를 전달하는 로직을 포함합니다.
         */
        void sendSuccessResult();
    }

    /**
     * Presenter가 구현해야 하는 메서드들의 집합입니다.
     * View는 이 인터페이스를 통해 사용자의 입력이나 이벤트를 Presenter에게 전달합니다.
     * (예: '저장' 버튼 클릭)
     */
    interface Presenter {
        /**
         * 사용자가 입력한 재료 정보를 받아 저장하는 로직을 처리하도록 요청합니다.
         *
         * @param name 재료 이름
         * @param quantityStr 사용자가 입력한 수량 (문자열 형태)
         * @param category 재료 카테고리
         * @param unit 재료 단위
         * @param storage 재료 보관 장소
         * @param expirationDate 재료 유통기한
         */
        void saveIngredient(
                String name,
                String quantityStr,
                String category,
                String unit,
                String storage,
                Calendar expirationDate
        );
    }
}
