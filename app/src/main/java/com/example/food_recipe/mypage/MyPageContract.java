package com.example.food_recipe.mypage;

import com.example.food_recipe.base.BaseContract;
import com.google.firebase.auth.FirebaseUser;

/**
 * [기존 주석 유지] MyPage 기능에 대한 Contract. BaseContract를 상속받아 구현합니다.
 * [변경] 로그아웃과 계정 탈퇴의 성공 콜백을 분리하여 명확성을 높였습니다.
 */
public interface MyPageContract {

    /**
     * [기존 주석 유지] View가 구현해야 하는 메서드 목록. BaseContract.View를 상속받습니다.
     */
    interface View extends BaseContract.View {
        void showUserInfo(FirebaseUser user);
        void showLogoutDialog();
        void showDeleteAccountDialog();
        void showToast(String message);
        void navigateToLogin();
        void navigateToFindPassword();
        void navigateToEditProfile();
        void googleSignOut(); // [추가] 구글 로그아웃 수행
    }

    /**
     * [기존 주석 유지] Presenter가 구현해야 하는 메서드 목록. BaseContract.Presenter를 상속받습니다.
     */
    interface Presenter extends BaseContract.Presenter<View> {
        void loadUserData();
        void handleMenuClick(String menuTitle);
        void logout();
        void deleteAccount();
    }

    /**
     * [기존 주석 유지] Model이 구현해야 하는 메서드 목록.
     */
    interface Model {
        boolean isGoogleUser(); // [추가] 구글 사용자인지 확인
        void logout(OnFinishedListener listener);
        void deleteAccount(OnFinishedListener listener);

        /**
         * [기존 주석 유지] Model의 작업 완료 후 Presenter에게 결과를 전달하는 콜백 인터페이스.
         * [변경] 작업 성공의 종류를 명확히 구분하기 위해 onSuccess를 onLogoutSuccess와 onDeleteAccountSuccess로 분리합니다.
         */
        interface OnFinishedListener {
            /**
             * [추가] 로그아웃 작업 성공 시 호출됩니다.
             */
            void onLogoutSuccess();

            /**
             * [추가] 계정 탈퇴 작업 성공 시 호출됩니다.
             */
            void onDeleteAccountSuccess();

            /**
             * [기존 주석 유지] 작업 실패 시 호출됩니다.
             */
            void onError(String message);
        }
    }
}
