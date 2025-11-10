// 파일: app/src/main/java/com/example/food_recipe/splash/SplashPresenter.java
package com.example.food_recipe.splash;

import android.util.Log;
import com.example.food_recipe.base.BasePresenter;

/**
 * SplashPresenter
 */
// [변경] BasePresenter를 상속받아 View 생명주기를 안전하게 관리
public class SplashPresenter extends BasePresenter<SplashContract.View> implements SplashContract.Presenter {

    private final SplashContract.Model model;

    // [변경] 생성자에서 View를 받지 않음
    public SplashPresenter(SplashContract.Model model) {
        this.model = model;
    }

    // [삭제] attach는 BasePresenter에 이미 정의되어 있으므로 제거
    // [변경] attachView 오버라이드 후 로그 추가 (기존 기능 유지)
    @Override
    public void attachView(SplashContract.View view) {
        super.attachView(view);
        if (isViewAttached()) {
            getView().log("Presenter attached");
        }
    }

    @Override
    public void start() {
        if (!isViewAttached()) return;
        getView().showLoading(true);
        getView().log("판단 시작");

        model.canProceedToMain(canGoMain -> {
            if (!isViewAttached()) return;
            getView().showLoading(false);
            getView().log("판단 결과 canGoMain=" + canGoMain);
            if (canGoMain) {
                getView().navigateToMain();
            } else {
                getView().navigateToLogin();
            }
        });
    }

    // [삭제] detach는 BasePresenter에 이미 정의되어 있으므로 제거
    // [변경] detachView 오버라이드 후 로그 추가 (기존 기능 유지)
    @Override
    public void detachView() {
        if (isViewAttached()) {
            getView().log("Presenter detached");
        }
        super.detachView();
        Log.d("SplashPresenter", "detach() done");
    }
}
