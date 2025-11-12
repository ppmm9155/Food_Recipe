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
        /**
         * [기존 주석 유지] 사용자 정보를 화면에 표시합니다.
         */
        void showUserInfo(FirebaseUser user);

        /**
         * [기존 주석 유지] 로그아웃 확인 다이얼로그를 표시합니다.
         */
        void showLogoutDialog();

        /**
         * [기존 주석 유지] 계정 탈퇴 확인 다이얼로그를 표시합니다.
         */
        void showDeleteAccountDialog();

        /**
         * [기존 주석 유지] 간단한 안내 메시지를 Toast로 표시합니다.
         */
        void showToast(String message);

        /**
         * [기존 주석 유지] AutoLoginManager를 포함한 모든 로그아웃 절차를 수행하고 로그인 화면으로 이동합니다.
         */
        void executeLogout();

        /**
         * [추가] 비밀번호 변경(찾기) 화면으로 이동합니다.
         */
        void navigateToFindPassword();
    }

    /**
     * [기존 주석 유지] Presenter가 구현해야 하는 메서드 목록. BaseContract.Presenter를 상속받습니다.
     */
    interface Presenter extends BaseContract.Presenter<View> {
        /**
         * [기존 주석 유지] 현재 로그인된 사용자 정보를 로드하도록 요청합니다.
         */
        void loadUserData();

        /**
         * [기존 주석 유지] 메뉴 아이템 클릭 이벤트를 처리합니다.
         */
        void handleMenuClick(String menuTitle);

        /**
         * [기존 주석 유지] 로그아웃을 실행합니다.
         */
        void logout();

        /**
         * [기존 주석 유지] 계정 탈퇴를 실행합니다.
         */
        void deleteAccount();
    }
}
