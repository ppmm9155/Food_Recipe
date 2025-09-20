package com.example.food_recipe.login;

import android.util.Patterns;
import com.google.firebase.auth.*;

import java.text.Normalizer;
import java.util.Locale;

// ✅ Presenter 계층 (MVP의 P)
// - View(화면)와 Model(데이터) 사이의 중재자
// - 입력값 검증, 비즈니스 로직, 흐름 제어 담당
// - View는 오직 화면만, Model은 오직 Firebase 통신만 → Presenter가 이 둘을 연결
public class LoginPresenter implements LoginContract.Presenter {

    private final LoginContract.View view;   // View 참조 (UI 업데이트용)
    private final LoginContract.Model model; // Model 참조 (Firebase 처리용)

    // ⚙️ 안전모드 설정: fetch 결과가 []일 때 "사용자 없음"으로 단정할지 여부
    private static final boolean TRUST_FETCH_FOR_USER_NOT_FOUND = false;

    // 공통 메시지 상수
    private static final String MSG_LOGIN_SUCCESS   = "로그인 성공";
    private static final String MSG_INVALID_EMAIL   = "이메일 형식이 올바르지 않습니다.";
    private static final String MSG_USER_NOT_FOUND  = "존재하지 않는 사용자입니다.";
    private static final String MSG_WRONG_PASSWORD  = "비밀번호가 올바르지 않습니다.";
    private static final String MSG_AMBIGUOUS       = "이메일 또는 비밀번호가 올바르지 않습니다.";

    // 생성자: View와 Model을 주입받음
    public LoginPresenter(LoginContract.View view, LoginContract.Model model) {
        this.view = view;
        this.model = model;
    }

    // 🔹 로그인 시도
    @Override
    public void attemptLogin(String rawEmail, String password, boolean autoLoginChecked) {
        String email = normalizeEmail(rawEmail); // 이메일 정규화 (소문자, trim 등)

        // 1) 입력값 검증
        if (email.isEmpty()) { view.showEmailError("이메일을 입력해주세요."); return; }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) { view.showEmailError(MSG_INVALID_EMAIL); return; }
        if (password.isEmpty()) { view.showPasswordError("비밀번호를 입력해주세요."); return; }

        // UI 잠금 (중복 입력 방지)
        view.setUiEnabled(false);

        // 2) Model을 통해 Firebase 로그인 시도
        model.signInWithEmail(email, password, new LoginModel.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                // ✅ Presenter는 AutoLoginManager 호출하지 않음
                // → View에게 성공 알림만 전달
                view.onLoginSuccess(autoLoginChecked);
            }

            @Override
            public void onFailure(Exception e) {
                // Firebase 에러코드 추출
                String code = (e instanceof FirebaseAuthException)
                        ? ((FirebaseAuthException) e).getErrorCode()
                        : null;

                // 명확한 오류는 즉시 View에 전달
                if ("ERROR_USER_NOT_FOUND".equals(code)) { view.showEmailError(MSG_USER_NOT_FOUND); view.setUiEnabled(true); return; }
                if ("ERROR_INVALID_EMAIL".equals(code)) { view.showEmailError(MSG_INVALID_EMAIL); view.setUiEnabled(true); return; }
                if ("ERROR_WRONG_PASSWORD".equals(code)) { view.showWrongPassword(); view.setUiEnabled(true); return; }

                // 모호한 오류 → fetchSignInMethods로 재확인
                if (isAmbiguous(code, e)) {
                    refineAmbiguousWithFetch(email);
                    return;
                }

                // 그 외 공통 실패 처리
                view.toast("로그인 실패");
                view.setUiEnabled(true);
            }
        });
    }

    // 🔹 Firebase 에러코드가 모호한 상황인지 판별
    @Override
    public boolean isAmbiguous(String code, Exception e) {
        return (e instanceof FirebaseAuthInvalidCredentialsException)
                || "ERROR_INVALID_CREDENTIAL".equals(code)
                || "ERROR_INVALID_LOGIN_CREDENTIALS".equals(code)
                || "ERROR_USER_TOKEN_EXPIRED".equals(code)
                || "ERROR_INVALID_USER_TOKEN".equals(code);
    }

    // 🔹 모호한 상황일 경우 → fetchSignInMethods로 계정 상태 재확인
    @Override
    public void refineAmbiguousWithFetch(String email) {
        model.fetchSignInMethods(email, methods -> {
            view.setUiEnabled(true);

            if (methods == null) {
                // 실패 시 그냥 모호 메시지 표시
                view.showAmbiguous();
                return;
            }

            boolean hasAny = !methods.isEmpty();
            if (!hasAny) {
                // 계정이 아예 없는 경우 (단, 안전모드일 때는 모호 메시지로 처리)
                if (TRUST_FETCH_FOR_USER_NOT_FOUND) {
                    view.showEmailError(MSG_USER_NOT_FOUND);
                } else {
                    view.showAmbiguous();
                }
                return;
            }

            // 비밀번호 로그인 지원 여부 확인
            boolean supportsPassword = methods.contains(EmailAuthProvider.EMAIL_PASSWORD_SIGN_IN_METHOD);
            // 구글 로그인 전용 계정인지 확인
            boolean googleOnly = methods.contains(GoogleAuthProvider.PROVIDER_ID) && !supportsPassword;

            if (googleOnly) {
                view.toast("이 계정은 Google 로그인을 사용해야 합니다.");
                return;
            }
            if (supportsPassword) {
                view.showWrongPassword();
                return;
            }
            view.showAmbiguous();
        });
    }

    @Override
    public void detachView() {
        // 현재는 특별히 해제할 리소스 없음
        // (ex: Coroutine/Observable 사용 시 여기서 정리)
    }

    // 🔹 이메일 정규화 유틸
    private String normalizeEmail(String raw) {
        String s = raw == null ? "" : raw;
        s = Normalizer.normalize(s, Normalizer.Form.NFC); // 유니코드 정규화
        s = s.trim();
        return s.toLowerCase(Locale.ROOT); // 소문자로 변환
    }
}
