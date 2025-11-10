package com.example.food_recipe.pantry;

import android.text.TextUtils;
import com.example.food_recipe.base.BasePresenter;
import com.example.food_recipe.model.PantryItem;
import java.util.Calendar;
import java.util.UUID;

/**
 * 재료 추가 기능의 비즈니스 로직을 처리하는 Presenter 클래스입니다.
 */
// [변경] BasePresenter를 상속받고, 생성자 및 View 참조 방식을 수정
public class AddIngredientPresenter extends BasePresenter<AddIngredientContract.View> implements AddIngredientContract.Presenter {

    /** 데이터 처리를 담당할 Model(Repository) 인터페이스입니다. */
    private final PantryRepository mPantryRepository;

    /**
     * [변경] 생성자에서 View를 받지 않고 Repository만 주입받음
     */
    public AddIngredientPresenter(PantryRepository pantryRepository) {
        this.mPantryRepository = pantryRepository;
    }

    @Override
    public void saveIngredient(String name, String quantityStr, String category, String unit, String storage, Calendar expirationDate) {
        if (!isViewAttached()) return;

        if (TextUtils.isEmpty(name)) {
            getView().showNameEmptyError();
            return;
        }
        if (TextUtils.isEmpty(quantityStr)) {
            getView().showQuantityEmptyError();
            return;
        }

        double quantity = Double.parseDouble(quantityStr);
        String id = UUID.randomUUID().toString();

        PantryItem newItem = new PantryItem(
                id,
                name,
                category,
                quantity,
                unit,
                storage,
                expirationDate.getTime()
        );

        mPantryRepository.addPantryItem(newItem);

        getView().onSaveSuccess(name);
        getView().sendSuccessResult();
        getView().closeBottomSheet();
    }
}
