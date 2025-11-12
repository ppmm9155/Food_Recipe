package com.example.food_recipe.base;

import android.content.Context;

/**
 * MVP 패턴의 안정성을 위해 모든 Contract가 상속받아야 할 기본 계약(Rules)입니다.
 */
public interface BaseContract {

    /**
     * 모든 View 인터페이스가 상속받아야 할 기본 View 인터페이스입니다.
     */
    interface View {
        /**
         * Presenter가 Context에 안전하게 접근할 수 있도록 모든 View 구현체는 이 메서드를 제공해야 합니다.
         * Activity나 Fragment에서는 'this' 또는 'getContext()'를 반환하면 됩니다.
         * @return Context 인턴스
         */
        Context getContext();
    }

    /**
     * 모든 Presenter 인터페이스가 상속받아야 할 기본 Presenter 인터페이스입니다.
     * @param <T> 자신이 제어할 View 인터페이스를 제네릭으로 받습니다.
     */
    interface Presenter<T extends View> {
        /**
         * Presenter와 View를 연결합니다. View의 생명주기 시작 시(e.g., onResume) 호출됩니다.
         * @param view 연결할 View 객체
         */
        void attachView(T view);

        /**
         * Presenter와 View의 연결을 해제합니다. View의 생명주기 종료 시(e.g., onDestroy) 호출됩니다.
         * 메모리 누수 방지를 위해 필수적으로 호출해야 합니다.
         */
        void detachView();
    }
}
