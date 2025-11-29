
package com.example.food_recipe.login;

import android.content.Context;
import com.example.food_recipe.base.BaseContract;
import com.google.firebase.auth.FirebaseUser;
import java.util.List;

/**
 * [기존 주석 유지] 로그인 MVP 패턴의 "룰북(Rulebook)"
 */
public interface LoginContract {

    /**
     * [기존 주석 유지] View: 화면 담당.
     */
    interface View extends BaseContract.View {
        void showEmailError(String msg);
        void showPasswordError(String msg);
        void showWrongPassword();
        void showAmbiguous();
        void showEmailVerificationRequired();
        void showCoolDownMessage(String message);
        void clearEmailError();
        void clearPasswordError();
        void toast(String msg);
        void setUiEnabled(boolean enabled);
        void navigateToHome();
        void onLoginSuccess(boolean autoLoginChecked);
        // getContext()는 BaseContract.View에 이미 정의되어 있으므로 여기서 중복 선언할 필요가 없습니다.
    }

    /**
     * [기존 주석 유지] Presenter: 로직 담당.
     */
    interface Presenter extends BaseContract.Presenter<View> {
        void attemptLogin(String rawEmail, String password, boolean autoLoginChecked);
        void handleGoogleLoginResult(android.content.Intent data, boolean autoLoginChecked);
        void resendVerificationEmail();
        void onVerificationSnackbarDismissed();
        boolean isAmbiguous(String code, Exception e);
        void refineAmbiguousWithFetch(String email);
    }

    /**
     * [기존 주석 유지] Model: 데이터 담당.
     */
    interface Model {
        interface AuthCallback {
            void onSuccess(FirebaseUser user);
            void onFailure(Exception e);
        }

        interface FetchCallback {
            void onResult(List<String> methods);
        }

        void signInWithEmail(String email, String password, AuthCallback callback);
        void fetchSignInMethods(String email, FetchCallback callback);
        void signInWithGoogle(String idToken, AuthCallback callback);
        void updateUserVerificationStatus(FirebaseUser user);
    }
}
