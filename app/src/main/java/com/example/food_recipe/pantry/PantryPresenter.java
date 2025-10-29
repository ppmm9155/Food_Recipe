package com.example.food_recipe.pantry;

import com.example.food_recipe.model.PantryItem;
import java.util.List;

/**
 * 냉장고(Pantry) 기능의 비즈니스 로직을 처리하는 Presenter 클래스입니다.
 * MVP 패턴의 Presenter 역할을 담당하며, View(PantryFragment)로부터 사용자 이벤트를 받아
 * Model(PantryRepository)에 데이터 처리를 요청하고, 그 결과를 다시 View에 전달하여 UI를 업데이트합니다.
 * 또한, Repository로부터 데이터 로드 결과를 받기 위해 콜백 인터페이스(PantryLoadCallback)를 구현합니다.
 */
public class PantryPresenter implements PantryContract.Presenter, PantryRepository.PantryLoadCallback {

    /** Presenter가 제어할 View 인터페이스입니다. 메모리 누수 방지를 위해 null 할당이 가능해야 합니다. */
    private PantryContract.View mView;

    /** 데이터 처리를 담당할 Model(Repository) 인터페이스입니다. */
    private final PantryRepository mPantryRepository;

    /**
     * PantryPresenter의 생성자입니다.
     * 의존성 주입(Dependency Injection)을 통해 View와 Repository의 구현체를 받습니다.
     *
     * @param view Presenter와 상호작용할 View의 구현체
     * @param pantryRepository 데이터 CRUD를 담당할 Repository의 구현체
     */
    public PantryPresenter(PantryContract.View view, PantryRepository pantryRepository) {
        this.mView = view;
        this.mPantryRepository = pantryRepository;
    }

    /**
     * View로부터 재료 목록을 불러오라는 요청을 처리합니다.
     * Model(Repository)에 데이터 로드를 요청하고, View에는 로딩 상태 UI를 표시하도록 지시합니다.
     */
    @Override
    public void loadPantryItems() {
        if (mView != null) {
            mView.showLoading(); // 로딩 시작을 View에 알림
        }
        // Repository에 데이터 로드를 요청하면서, 결과(성공/실패)를 받을 콜백으로 자신(this)을 전달
        mPantryRepository.getPantryItems(this);
    }

    /**
     * View로부터 재료 아이템을 삭제하라는 요청을 처리합니다.
     * @param item 삭제할 PantryItem 객체
     */
    @Override
    public void deletePantryItem(PantryItem item) {
        // Repository에 아이템 삭제를 요청합니다.
        mPantryRepository.deletePantryItem(item);
        
        // 참고: 현재는 삭제 후 전체 목록을 다시 불러오고 있습니다. 
        // 이는 데이터 정합성을 보장하는 가장 간단한 방법입니다. 
        // 만약 UX 개선이 필요하다면, 이 부분은 삭제 성공 콜백을 받아서 처리하는 방식으로 변경할 수 있습니다.
        loadPantryItems(); 
    }

    /**
     * View가 파괴될 때 호출되어, Presenter가 가지고 있는 View 참조를 해제합니다.
     * 이는 Fragment/Activity의 생명주기에 맞춰 메모리 누수를 방지하기 위한 필수적인 과정입니다.
     */
    @Override
    public void detachView() {
        mView = null;
    }

    // ===== PantryRepository.PantryLoadCallback 구현부 =====

    /**
     * Repository가 데이터 로딩에 성공했을 때 호출되는 콜백 메서드입니다.
     * @param pantryItems Firestore에서 성공적으로 불러온 재료 아이템 리스트
     */
    @Override
    public void onPantryLoaded(List<PantryItem> pantryItems) {
        // View가 아직 유효한지(null이 아닌지) 확인합니다.
        if (mView != null) {
            mView.hideLoading(); // 로딩 종료를 View에 알림
            if (pantryItems.isEmpty()) {
                // 데이터가 비어있을 경우, View에 빈 화면을 표시하도록 지시
                mView.showEmptyView();
            } else {
                // 데이터가 있을 경우, View에 목록을 표시하도록 지시
                mView.showPantryItems(pantryItems);
            }
        }
    }

    /**
     * Repository가 데이터 로딩 중 오류가 발생했을 때 호출되는 콜백 메서드입니다.
     * @param message 발생한 오류에 대한 설명 메시지
     */
    @Override
    public void onError(String message) {
        // View가 아직 유효한지(null이 아닌지) 확인합니다.
        if (mView != null) {
            mView.hideLoading(); // 로딩 종료를 View에 알림
            mView.showError(message); // View에 오류 메시지를 표시하도록 지시
        }
    }
}
