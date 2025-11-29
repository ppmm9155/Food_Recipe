package com.example.food_recipe.pantry;

import com.example.food_recipe.base.BaseContract;
import com.example.food_recipe.model.PantryItem;
import java.util.Calendar;

/**
 * [기존 주석 유지] 재료 추가 기능(Add Ingredient)의 View와 Presenter 사이의 통신 규칙을 정의하는 계약(Contract) 인터페이스입니다.
 * [변경] 재료 수정 기능이 추가되었습니다.
 */
public interface AddIngredientContract {

    /**
     * [기존 주석 유지] View가 구현해야 하는 메서드들의 집합입니다.
     */
    interface View extends BaseContract.View {
        void showNameEmptyError();
        void showQuantityEmptyError();
        void onSaveSuccess(String ingredientName);
        void closeBottomSheet();
        void sendSuccessResult();
    }

    /**
     * [기존 주석 유지] Presenter가 구현해야 하는 메서드들의 집합입니다.
     * [변경] 재료 수정 메서드가 추가되었습니다.
     */
    interface Presenter extends BaseContract.Presenter<View> {
        void saveIngredient(
                String name,
                String quantityStr,
                String category,
                String unit,
                String storage,
                Calendar expirationDate
        );

        /**
         * [추가] 기존 재료 정보를 수정합니다.
         * @param item 수정할 정보가 담긴 PantryItem 객체
         */
        void updateIngredient(PantryItem item);
    }
}
