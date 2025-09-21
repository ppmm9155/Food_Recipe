package com.example.food_recipe.utils;

import android.util.Patterns;

import java.text.Normalizer;
import java.util.Locale;

/**
 * 🔧 ValidationUtils
 * - 이메일/아이디/비밀번호에 대한 "검증(Validate)" 과 "정규화(Normalize)"를 모아놓은 유틸 클래스
 * - Presenter/Activity 어디서든 공통으로 재사용 가능
 * - ✅ 기존 로직과 100% 동일 (동작 변경 없음, 위치만 utils로 분리)
 */
public final class ValidationUtils {

    private ValidationUtils() {
        // 인스턴스화 방지 (모든 메서드는 static)
    }

    // ─────────────────────────────────────────
    // 정규화 (Normalize)
    // ─────────────────────────────────────────

    /** 이메일 정규화: 공백 제거 + NFC 정규화 + 소문자 변환 */
    public static String normalizeEmail(String raw) {
        String s = raw == null ? "" : raw.trim();
        s = Normalizer.normalize(s, Normalizer.Form.NFC);
        return s.toLowerCase(Locale.ROOT);
    }

    /** 아이디 정규화: 공백 제거 + NFC 정규화 */
    public static String normalizeUsername(String raw) {
        String s = raw == null ? "" : raw.trim();
        return Normalizer.normalize(s, Normalizer.Form.NFC);
    }

    // ─────────────────────────────────────────
    // 검증 (Validate)
    // ─────────────────────────────────────────

    /** 아이디 검증: 비어있지 않고 4~16자의 영문/숫자 */
    public static boolean validateUsername(String username) {
        if (username == null || username.isEmpty()) return false;
        return username.matches("^[a-zA-Z0-9]{4,16}$");
    }

    /** 이메일 검증: 형식 체크 */
    public static boolean validateEmail(String email) {
        if (email == null || email.isEmpty()) return false;
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /** 비밀번호 검증: 최소 6자, 동일 확인 */
    public static boolean validatePasswords(String p1, String p2) {
        if (p1 == null || p1.isEmpty()) return false;
        if (p1.length() < 6) return false;
        if (p2 == null || p2.isEmpty()) return false;
        return p1.equals(p2);
    }
}
