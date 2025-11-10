// FindIdContract.java
package com.example.food_recipe.findid;

import com.example.food_recipe.base.BaseContract;

// [변경] BaseContract를 상속받도록 수정
public interface FindIdContract {
    // [변경] BaseContract.View를 상속받음
    interface View extends BaseContract.View {
        void showEmailSentMessage();
        void showErrorMessage(String message);
        void showLoginRedirectMessage();
    }

    // [변경] BaseContract.Presenter를 상속받음
    interface Presenter extends BaseContract.Presenter<View> {
        void onVerifyEmailClicked(String email);
        void onLoginClicked();
        // [삭제] detachView()는 BaseContract.Presenter에 이미 정의되어 있으므로 제거
    }

    interface Model {
        void sendVerificationEmail(String email, Callback callback);

        interface Callback {
            void onSuccess();
            void onError(String error);
        }
    }
}
