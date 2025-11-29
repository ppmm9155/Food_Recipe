package com.example.food_recipe.join;

import com.example.food_recipe.base.BaseContract;

// [변경] BaseContract를 상속받도록 수정
public interface JoinContract {
    // [변경] BaseContract.View를 상속받음
    interface View extends BaseContract.View {
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

    // [변경] BaseContract.Presenter를 상속받음
    interface Presenter extends BaseContract.Presenter<View> {
        void checkUsernameAvailability(String usernameRaw);
        void checkEmailAvailability(String emailRaw);
        void attemptRegister(String usernameRaw, String emailRaw, String password, String confirmPassword);

        // ✅ 원본처럼 "아이디 입력이 바뀌면 중복확인 캐시 무효화"를 그대로 재현하기 위한 추가
        void onUsernameEdited();

        // [삭제] detachView()는 BaseContract.Presenter에 이미 정의되어 있으므로 제거
    }

    interface Model {
        void checkUsernameAvailability(String lowerUsername, JoinModel.UsernameCallback callback);
        void checkEmailAvailability(String email, JoinModel.EmailCallback callback);
        void createUserThenSaveProfile(String username, String email, String password, JoinModel.RegisterCallback callback);
    }
}
