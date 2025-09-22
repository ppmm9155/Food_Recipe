package com.example.food_recipe.main;

import android.content.Context;

import com.example.food_recipe.main.MainModel;

public class MainPresenter implements MainContract.Presenter {

    private final MainContract.View view;
    private final Context context;

    public MainPresenter(MainContract.View view, Context context) {
        this.view = view;
        this.context = context;
    }

    @Override
    public void onLogoutClicked() {
        // 중복 클릭 방지
        view.setLogoutEnabled(false);

        // 모델에서 로그아웃 처리
        MainModel.logout(context);

        // 로그아웃 메시지 표시
        view.showLogoutMessage("로그아웃 되었습니다.");

        // 로그인 화면으로 이동
        view.navigateToLogin();
    }
}
