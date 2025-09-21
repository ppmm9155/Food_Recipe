// FindIdContract.java
package com.example.food_recipe.findid;

public interface FindIdContract {
    interface View {
        void showEmailSentMessage();
        void showErrorMessage(String message);
        void showLoginRedirectMessage();
    }

    interface Presenter {
        void onVerifyEmailClicked(String email);
        void onLoginClicked();
        void detachView();
    }

    interface Model {
        void sendVerificationEmail(String email, Callback callback);

        interface Callback {
            void onSuccess();
            void onError(String error);
        }
    }
}
