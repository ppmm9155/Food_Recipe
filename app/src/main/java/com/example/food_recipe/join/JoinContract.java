package com.example.food_recipe.join;

public interface JoinContract {
    interface View {
        void showIdError(String msg);
        void showIdOk(String msg);
        void showIdHelper(String msg);

        void showEmailError(String msg);
        void showEmailOk(String msg);
        void showEmailHelper(String msg);

        void showPasswordError(String msg);
        void showPasswordConfirmError(String msg);
        void showPasswordConfirmOk(String msg);

        void clearErrorOnId();
        void clearErrorOnEmail();
        void clearErrorOnPassword();
        void clearErrorOnPasswordConfirm();

        void toast(String msg);
        void setUiEnabled(boolean enabled);
        void navigateToLogin();
    }

    interface Presenter {
        void checkUsernameAvailability(String usernameRaw);
        void checkEmailAvailability(String emailRaw);
        void attemptRegister(String usernameRaw, String emailRaw, String password, String confirmPassword);

        // ✅ 원본처럼 "아이디 입력이 바뀌면 중복확인 캐시 무효화"를 그대로 재현하기 위한 추가
        void onUsernameEdited();

        void detachView();
    }

    interface Model {
        void checkUsernameAvailability(String lowerUsername, JoinModel.UsernameCallback callback);
        void checkEmailAvailability(String email, JoinModel.EmailCallback callback);
        void createUserThenSaveProfile(String username, String email, String password, JoinModel.RegisterCallback callback);
    }
}
