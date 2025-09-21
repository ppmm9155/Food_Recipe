// FindPsContract.java
package com.example.food_recipe.findps;

public interface FindPsContract {
    interface View {
        void showEmailEmptyError();
        void showResetEmailSentMessage();
        void showResetEmailFailedMessage();
        void navigateToLogin();
    }

    interface Presenter {
        void onVerifyEmailClicked(String email);

        void detachView();
    }

    interface Model {
        void sendPasswordResetEmail(String email, Callback callback);

        interface Callback {
            void onSuccess();
            void onFailure();
        }
    }
}
