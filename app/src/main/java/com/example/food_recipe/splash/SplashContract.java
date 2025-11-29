// 파일: app/src/main/java/com/example/food_recipe/splash/SplashContract.java
package com.example.food_recipe.splash;

import com.example.food_recipe.base.BaseContract;

/**
 * MVP용 계약(Contract)
 */
// [변경] BaseContract를 상속받도록 수정
public interface SplashContract {

    // [변경] BaseContract.View를 상속받음
    interface View extends BaseContract.View {
        void navigateToMain();
        void navigateToLogin();
        void showLoading(boolean show);
        void log(String msg);
    }

    // [변경] BaseContract.Presenter를 상속받음
    interface Presenter extends BaseContract.Presenter<View> {
        void start();
        // [삭제] attach, detach는 BaseContract.Presenter에 이미 정의되어 있으므로 제거
    }

    interface Model {
        interface Callback {
            void onResult(boolean canGoMain);
        }
        void canProceedToMain(Callback cb);
    }
}
