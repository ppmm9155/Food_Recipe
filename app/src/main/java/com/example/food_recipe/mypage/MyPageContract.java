package com.example.food_recipe.mypage;

import com.example.food_recipe.base.BaseContract;
import com.google.firebase.auth.FirebaseUser;

/**
 * [기존 주석 유지] MyPage 기능에 대한 Contract. BaseContract를 상속받아 구현합니다.
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
        // [변경] Presenter의 지시에 따라 로그인 화면으로 이동합니다.
        void navigateToLogin();
        void navigateToFindPassword();
        void navigateToEditProfile();
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
        /**
         * [추가] 로그아웃 과정을 처리하고, 완료 후 리스너를 통해 결과를 알립니다.
         */
        void logout(OnFinishedListener listener);

        /**
         * [기존 주석 유지] 계정 탈퇴의 모든 과정을 처리하고, 완료 후 리스너를 통해 결과를 알립니다.
         */
        void deleteAccount(OnFinishedListener listener);

        /**
         * [기존 주석 유지] Model의 작업 완료 후 Presenter에게 결과를 전달하는 콜백 인터페이스.
         * [변경] 로그아웃과 계정 탈퇴 모두에서 재사용됩니다.
         */
        interface OnFinishedListener {
            /**
             * [기존 주석 유지] 작업 성공 시 호출됩니다.
             */
            void onSuccess();

            /**
             * [기존 주석 유지] 작업 실패 시 호출됩니다.
             */
            void onError(String message);
        }
    }
}
