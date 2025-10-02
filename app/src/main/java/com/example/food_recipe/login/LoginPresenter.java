
package com.example.food_recipe.login;

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
    // Presenter는 View와 Model을 모두 알고 있어야 합니다.
    private final LoginContract.View view;   // "화면 담당 직원"에게 지시를 내리기 위한 리모컨
    private final LoginContract.Model model; // "데이터 전문가"에게 작업을 시키기 위한 리모컨

    // --- 설정 값 ---
    // Firebase 최신 정책 변경으로, 존재하지 않는 이메일로 로그인 시도 시
    // "USER_NOT_FOUND" 대신 모호한 "INVALID_LOGIN_CREDENTIALS" 에러가 발생합니다.
    // 이 때, 이메일의 유효성을 추가로 체크해서 "존재하지 않는 사용자"라고 확실히 알려줄지,
    // 아니면 보안을 위해 "이메일 또는 비밀번호 오류"라고 뭉뚱그려 알려줄지 결정하는 스위치입니다.
    // false로 두면 더 안전합니다.
    private static final boolean TRUST_FETCH_FOR_USER_NOT_FOUND = false;

    // --- 자주 사용하는 메시지 상수 ---
    // 똑같은 문자열을 여러 번 쓰는 실수를 방지하고, 나중에 메시지를 바꿀 때 편하도록 상수로 빼둡니다.
    private static final String MSG_INVALID_EMAIL   = "이메일 형식이 올바르지 않습니다.";
    private static final String MSG_USER_NOT_FOUND  = "존재하지 않는 사용자입니다.";
    private static final String MSG_WRONG_PASSWORD  = "비밀번호가 올바르지 않습니다.";
    private static final String MSG_AMBIGUOUS       = "이메일 또는 비밀번호가 올바르지 않습니다.";

    /**
     * 생성자: LoginPresenter가 처음 만들어질 때 호출됩니다.
     * @param view 이 Presenter가 제어할 View (화면)
     * @param model 이 Presenter가 사용할 Model (데이터 처리 도구)
     */
    public LoginPresenter(LoginContract.View view, LoginContract.Model model) {
        this.view = view;
        this.model = model;
    }

    /**
     * View로부터 "이메일 로그인" 요청을 받았을 때 호출됩니다.
     */
    @Override
    public void attemptLogin(String rawEmail, String password, boolean autoLoginChecked) {
        // 1. 입력값 전처리: 이메일 앞뒤 공백 제거 등
        String email = ValidationUtils.normalizeEmail(rawEmail);

        // 2. 입력값 검증 (Validation): 가장 기본적인 방어 로직
        if (email.isEmpty()) {
            view.showEmailError("이메일을 입력해주세요.");
            return; // 여기서 작업을 중단합니다.
        }
        if (!ValidationUtils.validateEmail(email)) {
            view.showEmailError(MSG_INVALID_EMAIL);
            return;
        }
        if (password.isEmpty()) {
            view.showPasswordError("비밀번호를 입력해주세요.");
            return;
        }

        // 3. 작업 시작 전 UI 비활성화: 사용자가 여러 번 버튼 누르는 것을 방지
        view.setUiEnabled(false);

        // 4. Model에게 실제 로그인 작업 지시 (비동기)
        model.signInWithEmail(email, password, new LoginContract.Model.AuthCallback() {
            // --- Model의 작업이 끝나면 아래 둘 중 하나가 호출됩니다 ---

            @Override
            public void onSuccess(FirebaseUser user) {
                if (view != null) {
                    // [추가] 로그인 성공 기록을 남깁니다.
                    AutoLoginManager.setAutoLogin(view.getContext(), autoLoginChecked);
                    AutoLoginManager.setCurrentLoginProvider(view.getContext(), AutoLoginManager.PROVIDER_EMAIL);
                    view.setUiEnabled(true); // UI 다시 활성화
                    view.onLoginSuccess(autoLoginChecked); // View에게 최종 성공 처리를 지시
                }
            }

            @Override
            public void onFailure(Exception e) {
                // 실패 원인을 분석해서 사용자에게 더 친절한 피드백을 줍니다.
                String code = (e instanceof FirebaseAuthException)
                        ? ((FirebaseAuthException) e).getErrorCode()
                        : null;

                if ("ERROR_USER_NOT_FOUND".equals(code)) { // 이제는 거의 발생하지 않는 예전 에러코드
                    view.showEmailError(MSG_USER_NOT_FOUND);
                } else if ("ERROR_INVALID_EMAIL".equals(code)) {
                    view.showEmailError(MSG_INVALID_EMAIL);
                } else if ("ERROR_WRONG_PASSWORD".equals(code)) { // 이것도 예전 에러코드
                    view.showWrongPassword();
                } else if (isAmbiguous(code, e)) {
                    // 에러가 모호할 경우, 추가 조사를 시작합니다.
                    refineAmbiguousWithFetch(email);
                    return; // 추가 조사는 비동기이므로, 여기서 UI 활성화를 하지 않고 바로 종료합니다.
                } else {
                    // 원인을 알 수 없는 나머지 실패 사례
                    view.toast("로그인 실패");
                }
                view.setUiEnabled(true); // UI 다시 활성화
            }
        });
    }

    /**
     * View로부터 "구글 로그인" 결과 처리를 요청받았을 때 호출됩니다.
     */
    @Override
    public void handleGoogleLoginResult(android.content.Intent data, boolean autoLoginChecked) {
        try {
            // 1. 구글 로그인 결과(Intent)에서 사용자 정보(Account)와 ID 토큰을 추출합니다.
            GoogleSignInAccount account =
                    GoogleSignIn.getSignedInAccountFromIntent(data).getResult(ApiException.class);
            if (account == null || account.getIdToken() == null) {
                view.toast("Google 계정 또는 ID Token이 없습니다.");
                return;
            }

            String idToken = account.getIdToken();
            view.setUiEnabled(false); // UI 비활성화

            // 2. Model에게 ID 토큰을 전달하며 Firebase 로그인을 지시합니다.
            model.signInWithGoogle(idToken, new LoginContract.Model.AuthCallback() {
                @Override public void onSuccess(FirebaseUser user) {
                    if (view != null) {
                        // [추가] 로그인 성공 기록 남기기
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
            // 구글 로그인 자체가 실패한 경우 (네트워크 오류, 사용자 취소 등)
            if (view != null) {
                view.toast("Google Sign-In 실패: " + e.getStatusCode());
            }
        }
    }

    /**
     * Firebase에서 받은 에러가 "모호한" 에러인지 판단합니다.
     * 최신 Firebase 정책은 보안을 위해 이메일이 틀렸는지, 비밀번호가 틀렸는지 알려주지 않고
     * "INVALID_LOGIN_CREDENTIALS" 라는 애매한 에러코드를 줍니다.
     */
    @Override
    public boolean isAmbiguous(String code, Exception e) {
        return (e instanceof FirebaseAuthInvalidCredentialsException)
                || "ERROR_INVALID_CREDENTIAL".equals(code) // 예전 버전의 모호한 에러
                || "INVALID_LOGIN_CREDENTIALS".equals(code); // 최신 버전의 모호한 에러
    }

    /**
     * 모호한 에러가 발생했을 때, 이메일이 실제로 가입된 계정인지 추가 조사를 지시합니다.
     */
    @Override
    public void refineAmbiguousWithFetch(String email) {
        model.fetchSignInMethods(email, methods -> {
            if (view == null) return;
            view.setUiEnabled(true); // 여기서 UI를 다시 활성화합니다.

            // 1. 조사 결과, 해당 이메일로 가입된 계정이 아예 없는 경우
            if (methods == null || methods.isEmpty()) {
                if (TRUST_FETCH_FOR_USER_NOT_FOUND) {
                    // 우리 서비스는 이메일 존재 여부를 알려줘도 괜찮다고 판단하면, "존재하지 않는 사용자"라고 명확히 알려줌
                    view.showEmailError(MSG_USER_NOT_FOUND);
                } else {
                    // 보안을 중시하면, 그냥 "이메일 또는 비밀번호 오류"라고 알려줌
                    view.showAmbiguous();
                }
                return;
            }

            // 2. 조사 결과, 계정은 있지만 '비밀번호' 방식이 아닌 경우
            boolean supportsPassword = methods.contains(EmailAuthProvider.EMAIL_PASSWORD_SIGN_IN_METHOD);
            boolean isGoogleAccount = methods.contains(GoogleAuthProvider.PROVIDER_ID);

            if (isGoogleAccount && !supportsPassword) {
                // 이메일은 존재하지만, 비밀번호 방식으로는 가입한 적 없고 구글로만 가입한 경우
                view.toast("이 계정은 Google 로그인을 사용해야 합니다.");
            } else if (supportsPassword) {
                // 이메일도 존재하고 비밀번호 방식도 지원하는 경우
                // -> 그렇다면 비밀번호가 틀린 것이 확실합니다.
                view.showWrongPassword();
            } else {
                // 그 외의 경우 (다른 로그인 방식 등)
                view.showAmbiguous();
            }
        });
    }

    /**
     * View로부터 "게스트 로그인" 요청을 받았을 때 호출됩니다.
     */
    /*@Override
    public void attemptGusetLogin(boolean autoLoginChecked) {
        if (view == null) return;

        view.setUiEnabled(false);

        // Model에게 익명(게스트) 로그인을 지시합니다.
        model.signInAnonyGuest(new LoginContract.Model.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                if (view != null) {
                    // [추가] 게스트 로그인 성공 기록을 남깁니다.
                    AutoLoginManager.setAutoLogin(view.getContext(), autoLoginChecked);
                    AutoLoginManager.setCurrentLoginProvider(view.getContext(), AutoLoginManager.PROVIDER_GUEST);
                    view.setUiEnabled(true);
                    // [리팩토링-변경] 통합된 성공 처리 메소드를 호출합니다.
                    view.onLoginSuccess(autoLoginChecked);
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
    }*/

    /**
     * View(Activity)가 파괴될 때 호출되어, 메모리 누수를 방지합니다.
     */
    @Override
    public void detachView() {
        // 현재 구조에서는 view가 final이라 null로 만들 수 없지만,
        // 만약 Presenter가 긴 작업을 하고 있다면 여기서 중단시키는 코드를 넣을 수 있습니다.
        // 예를 들어, 네트워크 요청을 취소하는 등의 작업을 합니다.
    }
}
