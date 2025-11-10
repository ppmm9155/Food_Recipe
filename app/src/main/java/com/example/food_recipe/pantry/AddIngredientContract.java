package com.example.food_recipe.pantry;

import com.example.food_recipe.base.BaseContract;
import java.util.Calendar;

/**
 * 재료 추가 기능(Add Ingredient)의 View와 Presenter 사이의 통신 규칙을 정의하는 계약(Contract) 인터페이스입니다.
 */
// [변경] BaseContract를 상속받도록 수정
public interface AddIngredientContract {

    /**
     * View가 구현해야 하는 메서드들의 집합입니다.
     */
    // [변경] BaseContract.View를 상속받음
    interface View extends BaseContract.View {
        void showNameEmptyError();
        void showQuantityEmptyError();
        void onSaveSuccess(String ingredientName);
        void closeBottomSheet();
        void sendSuccessResult();
    }

    /**
     * Presenter가 구현해야 하는 메서드들의 집합입니다.
     */
    // [변경] BaseContract.Presenter를 상속받음
    interface Presenter extends BaseContract.Presenter<View> {
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
