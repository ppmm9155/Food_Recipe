package com.example.food_recipe.pantry;

import android.text.TextUtils;
import com.example.food_recipe.model.PantryItem;
import java.util.Calendar;
import java.util.UUID;

/**
 * 재료 추가 기능의 비즈니스 로직을 처리하는 Presenter 클래스입니다.
 * MVP 패턴의 Presenter 역할을 담당하며, View(AddIngredientBottomSheetFragment)로부터 사용자 입력을 받아
 * 데이터 유효성을 검사하고, Model(PantryRepository)에 데이터 저장을 요청합니다.
 * 모든 로직 처리 후에는 그 결과를 다시 View에 전달하여 UI를 업데이트하도록 합니다.
 */
public class AddIngredientPresenter implements AddIngredientContract.Presenter {

    /** Presenter가 제어할 View 인터페이스입니다. */
    private final AddIngredientContract.View mView;

    /** 데이터 처리를 담당할 Model(Repository) 인터페이스입니다. */
    private final PantryRepository mPantryRepository;

    /**
     * AddIngredientPresenter의 생성자입니다.
     * 의존성 주입(Dependency Injection)을 통해 View와 Repository의 구현체를 받습니다.
     *
     * @param view Presenter와 상호작용할 View의 구현체
     * @param pantryRepository 데이터 CRUD를 담당할 Repository의 구현체
     */
    public AddIngredientPresenter(AddIngredientContract.View view, PantryRepository pantryRepository) {
        this.mView = view;
        this.mPantryRepository = pantryRepository;
    }

    /**
     * View로부터 전달받은 재료 정보를 저장하는 핵심 로직을 수행합니다.
     *
     * @param name 재료 이름
     * @param quantityStr 사용자가 입력한 수량 (문자열 형태)
     * @param category 재료 카테고리
     * @param unit 재료 단위
     * @param storage 재료 보관 장소
     * @param expirationDate 재료 유통기한
     */
    @Override
    public void saveIngredient(String name, String quantityStr, String category, String unit, String storage, Calendar expirationDate) {
        // 1. 입력 값 유효성 검사
        if (TextUtils.isEmpty(name)) {
            mView.showNameEmptyError(); // View에 이름 입력 오류 알림
            return;
        }
        if (TextUtils.isEmpty(quantityStr)) {
            mView.showQuantityEmptyError(); // View에 수량 입력 오류 알림
            return;
        }

        // 2. 데이터 가공 및 모델 객체 생성
        String id = UUID.randomUUID().toString(); // 새 재료를 위한 고유 ID 생성
        double quantity = Double.parseDouble(quantityStr);

        PantryItem newItem = new PantryItem(
                id,
                name,
                category,
                quantity,
                unit,
                storage,
                expirationDate.getTime() // Calendar 객체를 Date 객체로 변환
        );

        // 3. Model(Repository)에 데이터 추가 요청
        mPantryRepository.addPantryItem(newItem);

        // 4. 처리 결과를 View에 전달
        mView.onSaveSuccess(name);      // View에 저장 성공 토스트 메시지 표시 요청
        mView.sendSuccessResult();      // View에 부모 프래그먼트로 성공 결과 전달 요청
        mView.closeBottomSheet();       // View에 BottomSheet 닫기 요청
    }
}
