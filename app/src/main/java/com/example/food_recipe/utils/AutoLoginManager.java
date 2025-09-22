// File: app/src/main/java/com/example/food_recipe/utils/AutoLoginManager.java
package com.example.food_recipe.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * 자동로그인 + 1회 강제 재로그인 플래그 관리
 * - 기존 public API 이름 유지: setAutoLogin(...), isAutoLoginEnabled(...), isLoggedIn(...)
 * - logout(...) 강화: Firebase signOut + 자동로그인 OFF + 다음 1회 강제 재로그인 ON
 */
public class AutoLoginManager {

    private static final String PREF_NAME = "auto_login_prefs";
    private static final String KEY_AUTO_LOGIN = "auto_login";
    private static final String KEY_FORCE_RELOGIN_ONCE = "force_relogin_once";

    private static SharedPreferences sp(Context context) {
        return context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // === 기존 API 유지 ===
    public static void setAutoLogin(Context context, boolean enabled) {
        sp(context).edit().putBoolean(KEY_AUTO_LOGIN, enabled).apply();
    }

    public static boolean isAutoLoginEnabled(Context context) {
        return sp(context).getBoolean(KEY_AUTO_LOGIN, false);
    }

    /**
     * 로그인 상태 판단:
     * - FirebaseUser != null
     * - 자동로그인 ON
     * - "강제 재로그인 1회"가 꺼져 있어야 함
     */
    public static boolean isLoggedIn(Context context) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        boolean auto = isAutoLoginEnabled(context);        // 👉 auto 변수 추가
        boolean force = isForceReLoginOnce(context);
        Log.d("AutoLoginCheck", "user=" + (user != null)
                + ", auto=" + auto
                + ", force=" + force);
        return (user != null && auto && !force);
    }

    // === 신규 보호 플래그 ===
    public static void setForceReLoginOnce(Context context, boolean on) {
        sp(context).edit().putBoolean(KEY_FORCE_RELOGIN_ONCE, on).apply();
    }

    public static boolean isForceReLoginOnce(Context context) {
        return sp(context).getBoolean(KEY_FORCE_RELOGIN_ONCE, false);
    }

    public static void clearForceReLoginOnce(Context context) {
        sp(context).edit().putBoolean(KEY_FORCE_RELOGIN_ONCE, false).apply();
    }

    /** 필요 시 전체 흔적 삭제 */
    public static void clearAll(Context context) {
        sp(context).edit().clear().apply();
    }

    /**
     * 완전 로그아웃:
     * - Firebase 세션 종료
     * - 자동로그인 OFF
     * - 다음 앱 진입은 무조건 로그인(1회)하도록 플래그 ON
     */
    public static void logout(Context context) {
        Log.d("AutoLogin", "logout() 호출됨 → Firebase signOut + auto=false + forceReLoginOnce=true");
        FirebaseAuth.getInstance().signOut();
        setAutoLogin(context, false);
        setForceReLoginOnce(context, true);
    }
}
