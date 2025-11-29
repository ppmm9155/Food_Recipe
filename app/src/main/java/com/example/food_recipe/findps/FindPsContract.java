// FindPsContract.java
package com.example.food_recipe.findps;

import com.example.food_recipe.base.BaseContract;

// [변경] BaseContract를 상속받도록 수정
public interface FindPsContract {
    // [변경] BaseContract.View를 상속받음
    interface View extends BaseContract.View {
        void showEmailEmptyError();
        void showResetEmailSentMessage();
        void showResetEmailFailedMessage();
        void navigateToLogin();
    }

    // [변경] BaseContract.Presenter를 상속받음
    interface Presenter extends BaseContract.Presenter<View> {
        void onVerifyEmailClicked(String email);
        // [삭제] detachView()는 BaseContract.Presenter에 이미 정의되어 있으므로 제거
    }

    interface Model {
        void sendPasswordResetEmail(String email, Callback callback);

        interface Callback {
            void onSuccess();
            void onFailure();
        }
    }
}
