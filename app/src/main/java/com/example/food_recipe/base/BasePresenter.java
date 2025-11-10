package com.example.food_recipe.base;

// [추가] 모든 Presenter의 기반이 되는 추상 클래스
// [변경] 공통 코드 관리를 위해 base 패키지로 이동
public abstract class BasePresenter<T extends BaseContract.View> implements BaseContract.Presenter<T> {

    private T view;

    /**
     * [변경] View를 Presenter에 연결.
     * 연결된 View는 getView()를 통해 안전하게 접근 가능.
     */
    @Override
    public void attachView(T view) {
        this.view = view;
    }

    /**
     * [변경] View와의 연결을 해제.
     * 메모리 누수 방지를 위해 View 참조를 null로 설정.
     */
    @Override
    public void detachView() {
        this.view = null;
    }

    /**
     * 연결된 View 객체를 반환.
     * @return 연결된 View 객체, 연결되지 않았다면 null
     */
    protected T getView() {
        return view;
    }

    /**
     * View가 현재 Presenter에 연결되어 있는지 확인.
     * 비동기 작업 후 UI 업데이트 시 NullPointerException 방지를 위해 사용.
     * @return View의 연결 여부 (true: 연결됨, false: 연결되지 않음)
     */
    public boolean isViewAttached() {
        return view != null;
    }
}
