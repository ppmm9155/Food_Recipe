
package com.example.food_recipe.login;

import android.content.Context;
import com.google.firebase.auth.FirebaseUser;
import java.util.List;

/**
 * 로그인 MVP 패턴의 "룰북(Rulebook)" 📜
 *
 * 로그인 기능을 구현하기 전에, View, Presenter, Model이 각각
 * 어떤 역할을 하고 어떤 함수를 가져야 하는지 약속해두는 곳입니다.
 *
 * - View: UI 덩어리. 화면에 보여주는 것만 신경 씀. (LoginActivity)
 * - Presenter: 중간 보스. View의 요청을 받아 로직을 처리하고 Model을 괴롭힘. (LoginPresenter)
 * - Model: 데이터 전문가. Firebase 같은 외부 시스템과 통신하는 실제 깡패. (LoginModel)
 *
 * 이렇게 역할을 나눠두면 나중에 코드가 꼬이지 않고, 각자 자기 할 일만 집중할 수 있어서 편해집니다.
 */
public interface LoginContract {

    // ===================================================================
    // View: 화면 담당. Presenter가 시키는 대로 화면을 바꾸는 역할.
    // LoginActivity가 이 규칙을 따라서 구현해야 합니다.
    // ===================================================================
    interface View {
        // --- Presenter가 View에게 내리는 UI 변경 지시들 ---
        void showEmailError(String msg);
        void showPasswordError(String msg);
        void showWrongPassword();
        void showAmbiguous();
        void showEmailVerificationRequired();
        // [추가] 쿨다운 상태일 때 사용자에게 안내 메시지를 표시하라는 지시
        void showCoolDownMessage(String message);

        void clearEmailError();
        void clearPasswordError();

        void toast(String msg);
        void setUiEnabled(boolean enabled); // 로그인 시도 중 중복 클릭을 막기 위해 UI를 비활성화 시킬 때 사용
        void navigateToHome();              // 로그인 성공 시 메인 화면으로 이동

        // 모든 로그인(이메일, 구글, 게스트) 성공 시 최종적으로 호출됩니다.
        void onLoginSuccess(boolean autoLoginChecked);

        // Presenter가 가끔 Context가 필요할 때가 있어서 만들어 둔 창구
        Context getContext();
    }

    // ===================================================================
    // Presenter: 로직 담당. View로부터 이벤트를 받아 Model에 데이터를 요청하고,
    // 그 결과를 가공해서 다시 View에 업데이트하라고 지시합니다.
    // LoginPresenter가 이 규칙을 따라서 구현해야 합니다.
    // ===================================================================
    interface Presenter {
        // --- View가 Presenter에게 요청하는 작업들 ---
        void attemptLogin(String rawEmail, String password, boolean autoLoginChecked);
        void handleGoogleLoginResult(android.content.Intent data, boolean autoLoginChecked);
        void resendVerificationEmail();
        void onVerificationSnackbarDismissed();
        //void attemptGusetLogin(boolean autoLoginChecked); //게스트 로그인

        // --- 내부 로직 처리 ---
        // Firebase 에러가 애매할 때 (e.g. "INVALID_LOGIN_CREDENTIALS"), 이게 단순 비밀번호 오류인지, 계정이 없는건지 판단하기 위한 로직
        boolean isAmbiguous(String code, Exception e);
        void refineAmbiguousWithFetch(String email);

        // View가 파괴될 때 Presenter와의 연결을 끊어 메모리 누수를 방지합니다.
        void detachView();
    }

    // ===================================================================
    // Model: 데이터 담당. Firebase와의 통신 등 실제 데이터 소스를 다루는 역할.
    // Presenter나 View에 대해서는 아무것도 몰라야 합니다 (독립적).
    // LoginModel이 이 규칙을 따라서 구현해야 합니다.
    // ===================================================================
    interface Model {
        // --- 콜백 인터페이스 ---
        // Model의 작업(네트워크 통신 등)은 대부분 비동기로 이루어집니다.
        // 작업이 끝났을 때 Presenter에게 성공/실패를 알려주기 위한 연락책입니다.
        interface AuthCallback {
            void onSuccess(FirebaseUser user);
            void onFailure(Exception e);
        }

        interface FetchCallback {
            void onResult(List<String> methods);
        }

        // --- Presenter가 Model에게 요청하는 작업들 ---
        void signInWithEmail(String email, String password, AuthCallback callback);
        void fetchSignInMethods(String email, FetchCallback callback);
        void signInWithGoogle(String idToken, AuthCallback callback);
        
        // [추가] 사용자의 이메일 인증 상태를 Firestore DB에 업데이트합니다.
        void updateUserVerificationStatus(FirebaseUser user);

        //void signInAnonyGuest(AuthCallback callback); //게스트 로그인실행
    }
}
