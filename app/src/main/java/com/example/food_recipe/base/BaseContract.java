package com.example.food_recipe.base;

// [추가] MVP 패턴의 안정성 강화를 위한 기본 Contract 인터페이스
// [변경] 공통 코드 관리를 위해 base 패키지로 이동
public interface BaseContract {

    /**
     * 모든 View 인터페이스가 상속받아야 할 기본 View 인터페이스
     */
    interface View {
    }

    /**
     * 모든 Presenter 인터페이스가 상속받아야 할 기본 Presenter 인터페이스
     * @param <T> 제네릭 타입으로, 자신이 제어할 View 인터페이스를 받음
     */
    interface Presenter<T extends View> {

        /**
         * Presenter와 View를 연결. View의 생명주기 시작 시(e.g., onResume) 호출
         * @param view 연결할 View 객체
         */
        void attachView(T view);

        /**
         * Presenter와 View의 연결을 해제. View의 생명주기 종료 시(e.g., onDestroy) 호출
         * 메모리 누수 방지를 위해 필수적으로 호출해야 함
         */
        void detachView();
    }
}
