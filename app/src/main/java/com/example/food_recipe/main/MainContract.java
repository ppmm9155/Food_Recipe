package com.example.food_recipe.main;

import android.content.Context;

/**
 * [기존 주석 유지] MVP 패턴의 각 컴포넌트(Model, View, Presenter)가 무엇을 해야 하는지 정의하는 '계약서'입니다.
 */
public interface MainContract {

    /**
     * [기존 주석 유지] View는 사용자에게 보여지는 화면(UI)을 담당합니다.
     */
    interface View {
        /** Presenter가 "로그인 화면으로 이동해"라고 호출할 메서드 */
        void navigateToLogin();

        // [삭제] 툴바 로그아웃 기능이 MyPage로 이전됨에 따라 관련 메서드 정의를 삭제합니다.

        /** [기존 주석 유지] Presenter가 Context를 요청할 때 호출될 메서드 (AutoLoginManager 사용 위함)*/
        Context getContext();
    }

    /**
     * [기존 주석 유지] Presenter는 View와 Model 사이의 중재자 역할을 합니다.
     */
    interface Presenter {
        /** [기존 주석 유지] Activity가 생성될 때, Presenter와 View를 연결하기 위해 호출됩니다. */
        void attach(View v);

        /** [기존 주석 유지] Activity가 파괴될 때, 메모리 누수를 방지하기 위해 View와의 연결을 끊습니다. */
        void detach();

        /** [기존 주석 유지] View가 화면에 보여지기 시작할 때, 초기 작업을 수행하기 위해 호출됩니다. (예: 로그인 상태 확인) */
        void start();

        // [삭제] 툴바 로그아웃 기능이 MyPage로 이전됨에 따라 관련 메서드 정의를 삭제합니다.
    }

    /**
     * [기존 주석 유지] Model은 데이터와 관련된 모든 작업을 담당합니다.
     */
    interface Model {

        // [삭제] 툴바 로그아웃 기능이 MyPage로 이전됨에 따라 관련 콜백 및 메서드 정의를 삭제합니다.

        /**
         * [기존 주석 유지] 로그인 상태 확인 작업이 끝났을 때, Presenter에게 결과를 알려주기 위한 콜백 인터페이스
         */
        interface LoginStatusCallback {
            void onLoggedIn(); // 로그인 상태일 때 호출
            void onLoggedOut(); // 로그아웃 상태일 때 호출
        }

        /** [기존 주석 유지] Presenter가 "로그인 상태 확인해줘"라고 호출할 메서드 */
        void checkLoginStatus(LoginStatusCallback cb);
    }
}
