package com.example.food_recipe.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

// ✅ AutoLoginManager
// - 앱에서 "자동 로그인" 기능을 담당하는 유틸 클래스
// - SharedPreferences(내부 저장소)에 자동 로그인 여부를 저장/조회
// - FirebaseAuth와 연동해서 현재 로그인 상태 확인 및 로그아웃 처리도 지원
public class AutoLoginManager {

    // 내부 저장소 파일 이름
    private static final String PREF_NAME = "auto_login_prefs";
    // 자동 로그인 여부를 저장할 key 값
    private static final String KEY_AUTO_LOGIN = "auto_login_enabled";

    // 🔹 자동 로그인 여부 저장
    // - 사용자가 로그인 성공 시 체크박스를 켰다면 enabled=true 저장
    // - 체크하지 않았다면 enabled=false 저장
    public static void setAutoLogin(Context context, boolean enabled) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putBoolean(KEY_AUTO_LOGIN, enabled) // true/false 값 저장
                .apply(); // apply()는 비동기로 저장 (commit보다 빠름)
    }

    // 🔹 자동 로그인 여부 불러오기
    // - 앱 실행 시 자동 로그인을 켰는지 여부 확인
    public static boolean isAutoLoginEnabled(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_AUTO_LOGIN, false);
        // 기본값은 false (저장된 값이 없으면 자동 로그인 꺼짐)
    }

    // 🔹 현재 Firebase 로그인 상태 확인
    // - FirebaseUser가 존재하는지 + 자동 로그인 옵션이 켜져 있는지 확인
    public static boolean isLoggedIn(Context context) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return (user != null && isAutoLoginEnabled(context));
    }

    // 🔹 로그아웃 처리
    // - FirebaseAuth에서 로그아웃 실행
    // - 자동 로그인 옵션도 false로 초기화
    public static void logout(Context context) {
        FirebaseAuth.getInstance().signOut();   // Firebase 세션 종료
        setAutoLogin(context, false);           // 자동 로그인 설정 해제
    }
}
