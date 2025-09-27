package com.example.food_recipe.main;

import android.content.Context;

/**
 * MVP 패턴의 각 컴포넌트(Model, View, Presenter)가 무엇을 해야 하는지 정의하는 '계약서'입니다.
 * 이 파일을 보면 Main 화면의 전체적인 구조와 기능을 한눈에 파악할 수 있습니다.
 * 각 인터페이스의 이름은 "어떤 역할을 하는지"를 명확하게 나타냅니다.
 */
public interface MainContract {

    /**
     * View는 사용자에게 보여지는 화면(UI)을 담당합니다.
     * Presenter가 "이거 보여줘"라고 지시하면, View는 화면에 그림을 그리는 역할만 합니다.
     * (예: Activity, Fragment)
     */
    interface View {
        /** Presenter가 "로그아웃 메시지 보여줘"라고 호출할 메서드 */
        void showLogoutMessage(String message);

        /** Presenter가 "로그인 화면으로 이동해"라고 호출할 메서드 */
        void navigateToLogin();

        /** Presenter가 "로그아웃 버튼 활성화/비활성화 해"라고 호출할 메서드 */
        void setLogoutEnabled(boolean enabled);

        // (새로추가됨) Presenter가 Context를 요청할 때 호출될 메서드 (AutoLoginManager 사용 위함)
        Context getContext();
    }

    /**
     * Presenter는 View와 Model 사이의 중재자 역할을 합니다.
     * View로부터 사용자 입력을 받으면, 어떤 작업을 할지 판단하고 Model에게 데이터를 요청합니다.
     * Model로부터 데이터를 받으면, View에게 "이 데이터를 화면에 그려줘"라고 지시합니다.
     * 비즈니스 로직(판단, 계산 등)은 모두 Presenter가 담당합니다.
     */
    interface Presenter {
        /** Activity가 생성될 때, Presenter와 View를 연결하기 위해 호출됩니다. */
        void attach(View v);

        /** Activity가 파괴될 때, 메모리 누수를 방지하기 위해 View와의 연결을 끊습니다. */
        void detach();

        /** View가 화면에 보여지기 시작할 때, 초기 작업을 수행하기 위해 호출됩니다. (예: 로그인 상태 확인) */
        void start();

        /** View에서 "로그아웃 버튼이 클릭되었다"는 신호를 받으면 호출될 메서드 */
        void onLogoutClicked();
    }

    /**
     * Model은 데이터와 관련된 모든 작업을 담당합니다.
     * (예: 데이터베이스, 네트워크 통신, 파일 입출력 등)
     * Presenter로부터 요청을 받으면, 데이터를 처리하고 그 결과를 콜백을 통해 다시 Presenter에게 전달합니다.
     */
    interface Model {

        /**
         * Model의 작업(예: 로그아웃)이 끝났을 때, Presenter에게 결과를 알려주기 위한 콜백 인터페이스
         */
        interface LogoutCallback {
            void onSuccess(); // 작업 성공 시 호출
            void onError(Exception e); // 작업 실패 시 호출
        }

        /**
         * Presenter가 "로그아웃 시켜줘"라고 호출할 메서드 (변경된부분)
         * @param loginProvider 현재 로그인된 방식 (예: AutoLoginManager.PROVIDER_EMAIL, AutoLoginManager.PROVIDER_GOOGLE) (새로추가됨)
         * @param cb 작업 완료 후 호출될 콜백
         */
        void logout(String loginProvider, LogoutCallback cb); // (변경된부분) loginProvider 파라미터 명시적 추가

        /**
         * 로그인 상태 확인 작업이 끝났을 때, Presenter에게 결과를 알려주기 위한 콜백 인터페이스
         */
        interface LoginStatusCallback {
            void onLoggedIn(); // 로그인 상태일 때 호출
            void onLoggedOut(); // 로그아웃 상태일 때 호출
        }

        /** Presenter가 "로그인 상태 확인해줘"라고 호출할 메서드 */
        void checkLoginStatus(LoginStatusCallback cb);
    }
}
