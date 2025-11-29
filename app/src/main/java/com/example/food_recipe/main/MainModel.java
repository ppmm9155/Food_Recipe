package com.example.food_recipe.main;

import android.content.Context;
import android.util.Log;

// [삭제] 불필요해진 Google 로그아웃 관련 import를 정리합니다.

import com.example.food_recipe.utils.AutoLoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * [기존 주석 유지] 데이터와 관련된 모든 실제 작업을 담당하는 Model 입니다.
 */
public class MainModel implements MainContract.Model {

    private static final String TAG = "MainModel";
    private final Context appContext;

    public MainModel(Context context) {
        this.appContext = context.getApplicationContext();
    }

    // [삭제] 툴바 로그아웃 기능이 MyPage로 이전됨에 따라 관련 메서드를 완전히 삭제합니다.

    @Override
    public void checkLoginStatus(LoginStatusCallback cb) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            cb.onLoggedIn();
        } else {
            cb.onLoggedOut();
        }
    }
}
