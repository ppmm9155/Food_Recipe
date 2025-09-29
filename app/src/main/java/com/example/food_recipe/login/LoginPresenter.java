package com.example.food_recipe.login;

import com.example.food_recipe.utils.AutoLoginManager; // (새로추가됨) AutoLoginManager 사용
import com.example.food_recipe.utils.ValidationUtils;
import com.google.firebase.auth.*;

import java.util.List;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;

/**
 * ✅ LoginPresenter (MVP의 P)
 * - View(화면)와 Model(데이터) 사이의 중재자
 * - 입력값 검증, Firebase 결과 분기 처리 담당
 */
public class LoginPresenter implements LoginContract.Presenter {

    private final LoginContract.View view;   // View 참조 (UI 업데이트용)
    private final LoginContract.Model model; // Model 참조 (Firebase 처리용)

    // ⚙️ 안전모드 설정: fetch 결과가 []일 때 "사용자 없음"으로 단정할지 여부
    private static final boolean TRUST_FETCH_FOR_USER_NOT_FOUND = false;

    // 공통 메시지 상수
    private static final String MSG_INVALID_EMAIL   = "이메일 형식이 올바르지 않습니다.";
    private static final String MSG_USER_NOT_FOUND  = "존재하지 않는 사용자입니다.";
    private static final String MSG_WRONG_PASSWORD  = "비밀번호가 올바르지 않습니다.";
    private static final String MSG_AMBIGUOUS       = "이메일 또는 비밀번호가 올바르지 않습니다.";

    public LoginPresenter(LoginContract.View view, LoginContract.Model model) {
        this.view = view;
        this.model = model;
    }

    @Override
    public void attemptLogin(String rawEmail, String password, boolean autoLoginChecked) {
        String email = ValidationUtils.normalizeEmail(rawEmail); // 이메일 정규화

        // 1) 입력값 검증
        if (email.isEmpty()) {
            view.showEmailError("이메일을 입력해주세요.");
            return;
        }
        if (!ValidationUtils.validateEmail(email)) {
            view.showEmailError(MSG_INVALID_EMAIL);
            return;
        }
        if (password.isEmpty()) {
            view.showPasswordError("비밀번호를 입력해주세요.");
            return;
        }

        // UI 잠금 (중복 입력 방지)
        view.setUiEnabled(false);

        // 2) Firebase 로그인 시도
        model.signInWithEmail(email, password, new LoginContract.Model.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                if (view != null) {
                    // (새로 추가됨) 이메일 로그인 성공 시, 로그인 방식을 "EMAIL"로 저장합니다.
                    AutoLoginManager.setCurrentLoginProvider(view.getContext(), AutoLoginManager.PROVIDER_EMAIL);

                    view.onLoginSuccess(autoLoginChecked);
                    view.setUiEnabled(true);
                }
            }

            @Override
            public void onFailure(Exception e) {
                String code = (e instanceof FirebaseAuthException)
                        ? ((FirebaseAuthException) e).getErrorCode()
                        : null;

                if ("ERROR_USER_NOT_FOUND".equals(code)) {
                    view.showEmailError(MSG_USER_NOT_FOUND);
                    view.setUiEnabled(true);
                    return;
                }
                if ("ERROR_INVALID_EMAIL".equals(code)) {
                    view.showEmailError(MSG_INVALID_EMAIL);
                    view.setUiEnabled(true);
                    return;
                }
                if ("ERROR_WRONG_PASSWORD".equals(code)) {
                    view.showWrongPassword();
                    view.setUiEnabled(true);
                    return;
                }

                // 모호한 에러 → fetchSignInMethods 확인
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

    // 👉 추가: 구글 로그인 결과 처리
    @Override
    public void handleGoogleLoginResult(android.content.Intent data, boolean autoLoginChecked) {
        try {
            GoogleSignInAccount account =
                    GoogleSignIn.getSignedInAccountFromIntent(data).getResult(ApiException.class);
            if (account == null || account.getIdToken() == null) {
                view.toast("Google 계정 또는 ID Token이 없습니다.");
                return;
            }

            String idToken = account.getIdToken();
            view.setUiEnabled(false);

            model.signInWithGoogle(idToken, new LoginContract.Model.AuthCallback() {
                @Override public void onSuccess(FirebaseUser user) {
                    // (새로추가됨) Google 로그인 성공 시, 로그인 제공자 정보 저장
                    if (view != null) {
                        AutoLoginManager.setCurrentLoginProvider(view.getContext(), AutoLoginManager.PROVIDER_GOOGLE);
                        view.setUiEnabled(true);
                        view.onLoginSuccess(autoLoginChecked);
                    }
                }
                @Override public void onFailure(Exception e) {
                    if (view != null) {
                        view.setUiEnabled(true);
                        view.toast("구글 로그인 실패: " + e.getMessage());
                    }
                }
            });
        } catch (ApiException e) {
            if (view != null) {
                view.toast("Google Sign-In 실패: " + e.getStatusCode());
            }
        }
    }

    @Override
    public boolean isAmbiguous(String code, Exception e) {
        return (e instanceof FirebaseAuthInvalidCredentialsException)
                || "ERROR_INVALID_CREDENTIAL".equals(code)
                || "ERROR_INVALID_LOGIN_CREDENTIALS".equals(code)
                || "ERROR_USER_TOKEN_EXPIRED".equals(code)
                || "ERROR_INVALID_USER_TOKEN".equals(code);
    }

    @Override
    public void refineAmbiguousWithFetch(String email) {
        model.fetchSignInMethods(email, methods -> {
            if (view == null) return;
            view.setUiEnabled(true);

            if (methods == null) {
                view.showAmbiguous();
                return;
            }

            boolean hasAny = !methods.isEmpty();
            if (!hasAny) {
                if (TRUST_FETCH_FOR_USER_NOT_FOUND) {
                    view.showEmailError(MSG_USER_NOT_FOUND);
                } else {
                    view.showAmbiguous();
                }
                return;
            }

            boolean supportsPassword = methods.contains(EmailAuthProvider.EMAIL_PASSWORD_SIGN_IN_METHOD);
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
    public void attemptGusetLogin(boolean autoLoginChecked) {
        if (view == null) return;

        view.setUiEnabled(false);

        model.signInAnonyGuest(new LoginContract.Model.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                if (view != null) {
                    AutoLoginManager.setCurrentLoginProvider(view.getContext(), AutoLoginManager.PROVIDER_GUEST);
                    view.setUiEnabled(true);
                    //view.onLoginSuccess(autoLoginChecked);
                    view.onGuestLoginSuccess(autoLoginChecked);
                }
            }

            @Override
            public void onFailure(Exception e) {
                if (view != null) {
                    view.setUiEnabled(true);
                    view.toast("게스트 로그인 실패: " + e.getMessage());
                }
            }
        });
    }

    @Override
    public void detachView() {
        android.util.Log.d("LoginPresenter", "detachView() called, view 참조 해제 요청");
        // 현재 view가 final이라 null 처리 불가 → 로그만 남김
        // 나중에 view = null 처리하려면 final 제거해야 함
        // 특별히 해제할 리소스 없음
    }
}
