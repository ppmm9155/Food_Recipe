package com.example.food_recipe.pantry;

import android.text.TextUtils;
import androidx.annotation.NonNull;
import com.example.food_recipe.base.BasePresenter;
import com.example.food_recipe.model.PantryItem;
import java.util.Calendar;
import java.util.UUID;

/**
 * [기존 주석 유지] 재료 추가 기능의 비즈니스 로직을 처리하는 Presenter 클래스입니다.
 * [변경] 데이터 처리 결과를 콜백으로 받아 View를 제어하도록 수정되었습니다.
 */
public class AddIngredientPresenter extends BasePresenter<AddIngredientContract.View> implements AddIngredientContract.Presenter {

    private final PantryRepository mPantryRepository;

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
                expirationDate != null ? expirationDate.getTime() : null
        );

        // [변경] Repository에 콜백을 전달하여 작업 완료 후의 로직을 정의합니다.
        mPantryRepository.addPantryItem(newItem, new PantryRepository.PantryWriteCallback() {
            @Override
            public void onWriteSuccess() {
                if (isViewAttached()) {
                    getView().onSaveSuccess(name);
                    getView().sendSuccessResult();
                    getView().closeBottomSheet();
                }
            }

            @Override
            public void onWriteFailure(String message) {
                if (isViewAttached()) {
                    // 간단한 에러 토스트를 보여주는 기능이 View에 필요하다면 추가할 수 있습니다.
                    // 여기서는 onSaveSuccess를 실패 메시지와 함께 재활용합니다.
                    getView().onSaveSuccess("저장 실패: " + message);
                }
            }
        });
    }

    @Override
    public void updateIngredient(PantryItem item) {
        if (!isViewAttached()) return;

        if (TextUtils.isEmpty(item.getName())) {
            getView().showNameEmptyError();
            return;
        }
        if (item.getQuantity() <= 0) {
            getView().showQuantityEmptyError();
            return;
        }

        // [변경] Repository에 콜백을 전달하여 작업 완료 후의 로직을 정의합니다.
        mPantryRepository.updatePantryItem(item, new PantryRepository.PantryWriteCallback() {
            @Override
            public void onWriteSuccess() {
                if (isViewAttached()) {
                    getView().onSaveSuccess(item.getName());
                    getView().sendSuccessResult();
                    getView().closeBottomSheet();
                }
            }

            @Override
            public void onWriteFailure(String message) {
                if (isViewAttached()) {
                    getView().onSaveSuccess("수정 실패: " + message);
                }
            }
        });
    }
}
