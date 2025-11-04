
package com.example.food_recipe.login;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.food_recipe.utils.AutoLoginManager;
import com.example.food_recipe.utils.ValidationUtils;
import com.google.firebase.auth.*;

import java.util.List;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;

/**
 * 로그인 기능의 "총괄 매니저" (Presenter)
 *
 * 이 클래스는 LoginActivity(View)와 LoginModel(Model) 사이의 중간 관리자 역할을 합니다.
 * 절대로 화면(UI)을 직접 건드리지 않고, 데이터 처리(Firebase)도 직접 하지 않습니다.
 *
 * 하는 일:
 * 1. View(화면)로부터 "로그인 버튼 눌렸어요!" 같은 요청을 받습니다.
 * 2. 입력된 이메일이나 비밀번호가 형식에 맞는지 검사(Validation)합니다.
 * 3. 검증이 끝나면, Model(데이터 전문가)에게 "이 정보로 Firebase에 로그인 좀 해줘!" 라고 작업을 지시합니다.
 * 4. Model로부터 받은 성공/실패 결과를 보고, View에게 "성공했으니 화면 전환해!", "실패했으니 에러 메시지 띄워!" 라고 지시합니다.
 *
 * 즉, 모든 비즈니스 로직과 판단은 여기서 이루어집니다.
 */
public class LoginPresenter implements LoginContract.Presenter {

    // --- 멤버 변수 ---
    private final LoginContract.View view;   // "화면 담당 직원"에게 지시를 내리기 위한 리모컨
    private final LoginContract.Model model; // "데이터 전문가"에게 작업을 시키기 위한 리모컨
    private FirebaseUser unverifiedUser = null; // 이메일 미인증 사용자의 재전송 요청을 처리하기 위해 FirebaseUser 객체를 임시 저장

    // --- 설정 값 ---
    private static final boolean TRUST_FETCH_FOR_USER_NOT_FOUND = false;
    private static final long RESEND_EMAIL_COOLDOWN_MS = 60 * 1000; // 60초

    // --- SharedPreferences 관련 상수 ---
    private static final String PREFS_NAME = "LoginPrefs";
    private static final String PREF_LAST_RESEND_TIMESTAMP = "lastResendTimestamp";

    // --- 자주 사용하는 메시지 상수 ---
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
        String email = ValidationUtils.normalizeEmail(rawEmail);
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
        view.setUiEnabled(false);
        model.signInWithEmail(email, password, new LoginContract.Model.AuthCallback() {

            @Override
            public void onSuccess(FirebaseUser user) {
                if (view == null) return;
                user.reload().addOnCompleteListener(reloadTask -> {
                    if (reloadTask.isSuccessful()) {
                        FirebaseUser refreshedUser = FirebaseAuth.getInstance().getCurrentUser();
                        if (refreshedUser == null) {
                            view.setUiEnabled(true);
                            view.toast("사용자 정보를 갱신하지 못했습니다.");
                            return;
                        }

                        if (!refreshedUser.isEmailVerified()) {
                            unverifiedUser = refreshedUser;
                            view.setUiEnabled(true);
                            view.showEmailVerificationRequired();
                            // [제거] 로그아웃 시점을 Snackbar가 사라진 후로 완전히 이전
                            return;
                        }

                        model.updateUserVerificationStatus(refreshedUser);
                        AutoLoginManager.setAutoLogin(view.getContext(), autoLoginChecked);
                        AutoLoginManager.setCurrentLoginProvider(view.getContext(), AutoLoginManager.PROVIDER_EMAIL);
                        view.setUiEnabled(true);
                        view.onLoginSuccess(autoLoginChecked);

                    } else {
                        view.setUiEnabled(true);
                        view.toast("사용자 정보 갱신에 실패했습니다. 네트워크 상태를 확인해주세요.");
                        FirebaseAuth.getInstance().signOut();
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                String code = (e instanceof FirebaseAuthException)
                        ? ((FirebaseAuthException) e).getErrorCode()
                        : null;

                if ("ERROR_USER_NOT_FOUND".equals(code)) {
                    view.showEmailError(MSG_USER_NOT_FOUND);
                } else if ("ERROR_INVALID_EMAIL".equals(code)) {
                    view.showEmailError(MSG_INVALID_EMAIL);
                } else if ("ERROR_WRONG_PASSWORD".equals(code)) {
                    view.showWrongPassword();
                } else if (isAmbiguous(code, e)) {
                    refineAmbiguousWithFetch(email);
                    return;
                } else {
                    view.toast("로그인 실패");
                }
                view.setUiEnabled(true);
            }
        });
    }

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
                    if (view != null) {
                        AutoLoginManager.setAutoLogin(view.getContext(), autoLoginChecked);
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

    // [수정] 메일 재전송 시 쿨다운 로직 추가
    @Override
    public void resendVerificationEmail() {
        if (unverifiedUser == null) {
            view.toast("오류: 사용자 정보가 없습니다. 다시 로그인해주세요.");
            return;
        }

        long now = System.currentTimeMillis();
        long lastSent = getLastResendTimestamp();

        if (now - lastSent < RESEND_EMAIL_COOLDOWN_MS) {
            long remaining = (lastSent + RESEND_EMAIL_COOLDOWN_MS - now) / 1000;
            view.showCoolDownMessage("잠시 후 다시 시도해주세요. (" + remaining + "초 남음)");
            return;
        }

        unverifiedUser.sendEmailVerification()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // [추가] 성공 시에만 마지막 전송 시간을 기록하여 쿨다운을 시작합니다.
                        saveLastResendTimestamp(System.currentTimeMillis());
                        view.toast("인증 메일을 다시 보냈습니다. 스팸함도 확인해 보세요.");
                    } else {
                        view.toast("메일 발송에 실패했습니다: " + task.getException().getMessage());
                    }
                });
    }

    // [수정] Snackbar 상호작용이 끝나면, 그 때 로그아웃을 실행
    @Override
    public void onVerificationSnackbarDismissed() {
        FirebaseAuth.getInstance().signOut();
    }

    @Override
    public boolean isAmbiguous(String code, Exception e) {
        return (e instanceof FirebaseAuthInvalidCredentialsException)
                || "ERROR_INVALID_CREDENTIAL".equals(code)
                || "INVALID_LOGIN_CREDENTIALS".equals(code);
    }

    @Override
    public void refineAmbiguousWithFetch(String email) {
        model.fetchSignInMethods(email, methods -> {
            if (view == null) return;
            view.setUiEnabled(true);

            if (methods == null || methods.isEmpty()) {
                if (TRUST_FETCH_FOR_USER_NOT_FOUND) {
                    view.showEmailError(MSG_USER_NOT_FOUND);
                } else {
                    view.showAmbiguous();
                }
                return;
            }

            boolean supportsPassword = methods.contains(EmailAuthProvider.EMAIL_PASSWORD_SIGN_IN_METHOD);
            boolean isGoogleAccount = methods.contains(GoogleAuthProvider.PROVIDER_ID);

            if (isGoogleAccount && !supportsPassword) {
                view.toast("이 계정은 Google 로그인을 사용해야 합니다.");
            } else if (supportsPassword) {
                view.showWrongPassword();
            } else {
                view.showAmbiguous();
            }
        });
    }

    // --- SharedPreferences 헬퍼 메소드 ---

    private void saveLastResendTimestamp(long timestamp) {
        SharedPreferences prefs = view.getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putLong(PREF_LAST_RESEND_TIMESTAMP, timestamp).apply();
    }

    private long getLastResendTimestamp() {
        SharedPreferences prefs = view.getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getLong(PREF_LAST_RESEND_TIMESTAMP, 0);
    }

    @Override
    public void detachView() {}
}
