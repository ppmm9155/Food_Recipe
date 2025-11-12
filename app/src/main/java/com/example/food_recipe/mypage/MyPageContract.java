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
        void executeLogout();
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
     * [추가] Model이 구현해야 하는 메서드 목록.
     */
    interface Model {
        /**
         * [추가] 계정 탈퇴의 모든 과정을 처리하고, 완료 후 리스너를 통해 결과를 알립니다.
         */
        void deleteAccount(OnFinishedListener listener);

        /**
         * [추가] Model의 작업 완료 후 Presenter에게 결과를 전달하는 콜백 인터페이스.
         */
        interface OnFinishedListener {
            /**
             * [추가] 계정 탈퇴 성공 시 호출됩니다.
             */
            void onSuccess();

            /**
             * [추가] 계정 탈퇴 실패 시 호출됩니다.
             */
            void onError(String message);
        }
    }
}
