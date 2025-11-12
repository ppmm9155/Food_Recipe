package com.example.food_recipe.main;

import android.content.Context;
import com.example.food_recipe.utils.AutoLoginManager; // AutoLoginManager 사용을 위해 import

import java.lang.ref.WeakReference;

/**
 * [기존 주석 유지] 메인 화면의 비즈니스 로직을 처리하는 Presenter 입니다.
 */
public class MainPresenter implements MainContract.Presenter {

    private final MainContract.Model model;
    private WeakReference<MainContract.View> viewRef;

    public MainPresenter(Context context) {
        this.model = new MainModel(context.getApplicationContext());
    }

    /**
     * [기존 주석 유지] 단위 테스트를 할 때 가짜(Mock) Model을 주입하기 위한 보조 생성자입니다.
     */
    public MainPresenter(MainContract.Model model) {
        this.model = model;
    }

    @Override public void attach(MainContract.View v) { viewRef = new WeakReference<>(v); }
    @Override public void detach() { viewRef = null; }

    @Override
    public void start() {
        model.checkLoginStatus(new MainContract.Model.LoginStatusCallback() {
            @Override
            public void onLoggedIn() {
                // [삭제] 툴바 메뉴가 삭제됨에 따라 관련 코드를 삭제합니다.
            }

            @Override
            public void onLoggedOut() {
                MainContract.View v = getView();
                if (v != null) v.navigateToLogin();
            }
        });
    }

    // [삭제] 툴바 로그아웃 기능이 MyPage로 이전됨에 따라 관련 메서드를 삭제합니다.

    /**
     * [기존 주석 유지] WeakReference로 감싸진 View 객체를 안전하게 가져오기 위한 도우미 메서드입니다.
     */
    private MainContract.View getView() {
        return viewRef != null ? viewRef.get() : null;
    }
}
