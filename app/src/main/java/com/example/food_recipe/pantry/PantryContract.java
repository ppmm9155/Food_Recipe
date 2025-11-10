package com.example.food_recipe.pantry;

import com.example.food_recipe.base.BaseContract;
import com.example.food_recipe.model.PantryItem;
import java.util.List;

/**
 * 냉장고(Pantry) 기능의 View와 Presenter 사이의 통신 규칙을 정의하는 계약(Contract) 인터페이스입니다.
 * MVP(Model-View-Presenter) 아키텍처 패턴을 따르며, View와 Presenter가 강하게 결합되지 않고
 * 이 인터페이스를 통해 상호작용하도록 설계되었습니다.
 */
// [변경] BaseContract를 상속받도록 수정
public interface PantryContract {

    /**
     * View(PantryFragment)가 구현해야 하는 메서드들의 집합입니다.
     */
    // [변경] BaseContract.View를 상속받음
    interface View extends BaseContract.View {
        void showLoading();
        void hideLoading();
        void showPantryItems(List<PantryItem> pantryItems);
        void showEmptyView();
        void showError(String message);
    }

    /**
     * Presenter(PantryPresenter)가 구현해야 하는 메서드들의 집합입니다.
     */
    // [변경] BaseContract.Presenter를 상속받음
    interface Presenter extends BaseContract.Presenter<View> {
        void loadPantryItems();
        void deletePantryItem(PantryItem item);
        // [삭제] detachView()는 BaseContract.Presenter에 이미 정의되어 있으므로 제거
    }
}
