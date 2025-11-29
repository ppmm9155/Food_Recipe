package com.example.food_recipe.main;

import com.example.food_recipe.base.BaseContract;

/**
 * MVP 패턴의 각 컴포넌트(Model, View, Presenter)가 무엇을 해야 하는지 정의하는 '계약서'입니다.
 */
public interface MainContract {

    /**
     * View는 사용자에게 보여지는 화면(UI)을 담당합니다.
     */
    // [수정] 모든 View의 기본 계약인 BaseContract.View를 상속받도록 수정합니다.
    interface View extends BaseContract.View {
        void navigateToLogin();
    }

    /**
     * Presenter는 View와 Model 사이의 중재자 역할을 합니다.
     */
    // [수정] 모든 Presenter의 기본 계약인 BaseContract.Presenter를 상속받도록 수정합니다.
    interface Presenter extends BaseContract.Presenter<View> {
        void start();
    }

    /**
     * Model은 데이터와 관련된 모든 작업을 담당합니다.
     */
    interface Model {
        interface LoginStatusCallback {
            void onLoggedIn();
            void onLoggedOut();
        }

        void checkLoginStatus(LoginStatusCallback cb);
    }
}
