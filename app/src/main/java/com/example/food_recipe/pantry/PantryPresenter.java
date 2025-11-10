package com.example.food_recipe.pantry;

import com.example.food_recipe.base.BasePresenter;
import com.example.food_recipe.model.PantryItem;
import java.util.List;

/**
 * 냉장고(Pantry) 기능의 비즈니스 로직을 처리하는 Presenter 클래스입니다.
 */
// [변경] BasePresenter를 상속받고, 생성자 및 View 참조 방식을 수정
public class PantryPresenter extends BasePresenter<PantryContract.View> implements PantryContract.Presenter, PantryRepository.PantryLoadCallback {

    /** 데이터 처리를 담당할 Model(Repository) 인터페이스입니다. */
    private final PantryRepository mPantryRepository;

    /**
     * [변경] 생성자에서 View를 받지 않고 Repository만 주입받음
     */
    public PantryPresenter(PantryRepository pantryRepository) {
        this.mPantryRepository = pantryRepository;
    }

    @Override
    public void loadPantryItems() {
        if (isViewAttached()) {
            getView().showLoading();
        }
        mPantryRepository.getPantryItems(this);
    }

    @Override
    public void deletePantryItem(PantryItem item) {
        mPantryRepository.deletePantryItem(item);
        loadPantryItems();
    }

    // [삭제] detachView()는 BasePresenter에 구현되어 있으므로 제거

    // ===== PantryRepository.PantryLoadCallback 구현부 =====

    @Override
    public void onPantryLoaded(List<PantryItem> pantryItems) {
        if (isViewAttached()) {
            getView().hideLoading();
            if (pantryItems.isEmpty()) {
                getView().showEmptyView();
            } else {
                getView().showPantryItems(pantryItems);
            }
        }
    }

    @Override
    public void onError(String message) {
        if (isViewAttached()) {
            getView().hideLoading();
            getView().showError(message);
        }
    }
}
