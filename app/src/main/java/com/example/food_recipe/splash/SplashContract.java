// 파일: app/src/main/java/com/example/food_recipe/splash/SplashContract.java
package com.example.food_recipe.splash;

/**
 * MVP용 계약(Contract)
 * - View: 화면 이동/로딩 표시/로그 출력
 * - Presenter: 생명주기 연결 및 시작 로직
 * - Model: 실제 판단 로직(Firebase 세션/자동로그인/강제재로그인1회)
 */
public interface SplashContract {

    interface View {
        void navigateToMain();     // 메인 화면으로 이동
        void navigateToLogin();    // 로그인 화면으로 이동
        void showLoading(boolean show); // 필요 시 로딩 표시(스플래시에선 로그 수준으로 처리 가능)
        void log(String msg);      // 디버그 로그(개발 단계 시 가시성 확보)
    }

    interface Presenter {
        void attach(View view);    // View 연결
        void start();              // 진입점(판단 시작)
        void detach();             // View 해제
    }

    interface Model {
        interface Callback {
            void onResult(boolean canGoMain); // true면 메인, false면 로그인
        }
        void canProceedToMain(Callback cb);   // 메인 진입 가능 여부 비동기 판정
    }
}
