// File: app/src/main/java/com/example/food_recipe/utils/AutoLoginManager.java
package com.example.food_recipe.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils; // (새로추가됨) TextUtils.isEmpty 사용을 위해
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * 자동로그인 + 1회 강제 재로그인 플래그 + 현재 로그인 방식 관리 (변경된부분)
 * - 기존 public API 이름 유지: setAutoLogin(...), isAutoLoginEnabled(...), isLoggedIn(...)
 * - logout(...) 강화: Firebase signOut + 자동로그인 OFF + 다음 1회 강제 재로그인 ON + 로그인 방식 초기화 (변경된부분)
 */
public class AutoLoginManager {

    private static final String PREF_NAME = "auto_login_prefs";
    private static final String KEY_AUTO_LOGIN = "auto_login";
    private static final String KEY_FORCE_RELOGIN_ONCE = "force_relogin_once";
    // (새로추가됨) 로그인 제공자 정보를 저장하기 위한 SharedPreferences 키
    private static final String KEY_LOGIN_PROVIDER = "login_provider";

    // (새로추가됨) 로그인 제공자 유형을 나타내는 상수
    public static final String PROVIDER_EMAIL = "EMAIL";
    public static final String PROVIDER_GOOGLE = "GOOGLE";
    public static final String PROVIDER_GUEST = "GUEST";
    public static final String PROVIDER_UNKNOWN = "UNKNOWN"; // (새로추가됨) 기본 또는 알 수 없는 제공자

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
        boolean auto = isAutoLoginEnabled(context);
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

    // (새로추가됨) 현재 로그인 제공자 정보 저장
    public static void setCurrentLoginProvider(Context context, String provider) {
        if (provider == null) { // (새로추가됨) null provider 방지
            provider = PROVIDER_UNKNOWN;
        }
        Log.d("AutoLoginManager", "setCurrentLoginProvider: " + provider); // (새로추가됨) 로그 추가
        sp(context).edit().putString(KEY_LOGIN_PROVIDER, provider).apply();
    }

    // (새로추가됨) 현재 로그인 제공자 정보 조회
    public static String getCurrentLoginProvider(Context context) {
        String provider = sp(context).getString(KEY_LOGIN_PROVIDER, PROVIDER_UNKNOWN);
        // (새로추가됨) 이전 버전 호환성을 위해, 만약 PROVIDER_UNKNOWN이고 자동 로그인이 켜져있으나 Firebase 유저가 없다면 강제로 UNKNOWN 유지
        // (이 부분은 앱의 기존 로직에 따라 더 정교하게 조정될 수 있습니다. 현재는 간단히 기본값 반환)
        if (TextUtils.isEmpty(provider)) { // (새로추가됨) 이전 버전에서 키가 없을 경우 대비
             provider = PROVIDER_UNKNOWN;
        }
        Log.d("AutoLoginManager", "getCurrentLoginProvider: " + provider); // (새로추가됨) 로그 추가
        return provider;
    }

    /** 필요 시 전체 흔적 삭제 (변경된부분) 로그인 제공자 정보도 삭제 */
    public static void clearAll(Context context) {
        Log.d("AutoLoginManager", "clearAll: 모든 자동 로그인 정보 삭제"); // (새로추가됨) 로그 추가
        sp(context).edit().clear().apply();
        // (새로추가됨) clear() 호출 후, 기본값 재설정을 고려할 수 있으나,
        // 현재는 SharedPreferences가 완전히 비워지므로 다음 getCurrentLoginProvider 호출 시 기본값(PROVIDER_UNKNOWN)이 반환됩니다.
    }

    /**
     * 완전 로그아웃: (변경된부분) 로그인 제공자 정보도 초기화
     * - Firebase 세션 종료
     * - 자동로그인 OFF
     * - 다음 앱 진입은 무조건 로그인(1회)하도록 플래그 ON
     * - 로그인 제공자 정보 초기화 (PROVIDER_UNKNOWN으로 설정)
     */
    public static void logout(Context context) {
        Log.d("AutoLoginManager", "logout() 호출됨 → Firebase signOut + auto=false + forceReLoginOnce=true + provider=UNKNOWN"); // (변경된부분) 로그 메시지 업데이트
        FirebaseAuth.getInstance().signOut();
        setAutoLogin(context, false);
        setForceReLoginOnce(context, true);
        // (새로추가됨) 로그아웃 시 로그인 제공자 정보를 UNKNOWN으로 초기화
        setCurrentLoginProvider(context, PROVIDER_UNKNOWN);
    }
}
