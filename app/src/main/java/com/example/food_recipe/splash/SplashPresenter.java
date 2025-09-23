// 파일: app/src/main/java/com/example/food_recipe/splash/SplashPresenter.java
package com.example.food_recipe.splash;

import android.util.Log;

/**
 * SplashPresenter
 * - View 생명주기에 맞춰 Model에 판단 요청
 * - 결과에 따라 View에 화면 전환 지시
 */
public class SplashPresenter implements SplashContract.Presenter {

    private final SplashContract.Model model;
    private SplashContract.View view; // onDestroy에서 detach()

    public SplashPresenter(SplashContract.Model model) {
        this.model = model;
    }

    @Override
    public void attach(SplashContract.View view) {
        this.view = view;
        if (view != null) view.log("Presenter attached");
    }

    @Override
    public void start() {
        if (view == null) return;
        view.showLoading(true);
        view.log("판단 시작");

        model.canProceedToMain(canGoMain -> {
            if (view == null) return;
            view.showLoading(false);
            view.log("판단 결과 canGoMain=" + canGoMain);
            if (canGoMain) {
                view.navigateToMain();
            } else {
                view.navigateToLogin();
            }
        });
    }

    @Override
    public void detach() {
        if (view != null) {
            view.log("Presenter detached");
        }
        view = null;
        Log.d("SplashPresenter", "detach() done");
    }
}
