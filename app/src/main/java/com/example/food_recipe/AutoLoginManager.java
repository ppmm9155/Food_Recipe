package com.example.food_recipe;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AutoLoginManager {
    private static final String PREF_NAME = "auto_login_prefs";
    private static final String KEY_AUTO_LOGIN = "auto_login_enabled";

    // 자동 로그인 여부 저장
    public static void setAutoLogin(Context context, boolean enabled) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putBoolean(KEY_AUTO_LOGIN, enabled)
                .apply();
    }

    // 자동 로그인 여부 가져오기
    public static boolean isAutoLoginEnabled(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_AUTO_LOGIN, false);
    }

    // Firebase 현재 유저가 존재하는지 + 자동 로그인 설정 확인
    public static boolean isLoggedIn(Context context) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return (user != null && isAutoLoginEnabled(context));
    }

    // 로그아웃 처리
    public static void logout(Context context) {
        FirebaseAuth.getInstance().signOut();
        setAutoLogin(context, false);
    }
}
