package com.example.food_recipe.mypage;

import com.example.food_recipe.base.BasePresenter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * [기존 주석 유지] MyPage의 비즈니스 로직을 처리하는 Presenter. BasePresenter를 상속받습니다.
 */
public class MyPagePresenter extends BasePresenter<MyPageContract.View> implements MyPageContract.Presenter {

    /**
     * [기존 주석 유지] Firebase 인증 인스턴스
     */
    private final FirebaseAuth firebaseAuth;

    /**
     * [기존 주석 유지] 생성자
     */
    public MyPagePresenter() {
        this.firebaseAuth = FirebaseAuth.getInstance();
    }

    /**
     * [기존 주석 유지] View와 Presenter 연결 로직은 BasePresenter에 위임합니다.
     */
    @Override
    public void attachView(MyPageContract.View view) {
        super.attachView(view);
    }

    /**
     * [기존 주석 유지] 현재 로그인된 사용자 정보를 가져와 View에 전달합니다.
     */
    @Override
    public void loadUserData() {
        if (isViewAttached()) {
            FirebaseUser currentUser = firebaseAuth.getCurrentUser();
            if (currentUser != null) {
                getView().showUserInfo(currentUser);
            }
        }
    }

    /**
     * [변경] 메뉴 아이템 클릭 이벤트를 처리합니다. '비밀번호 변경' 클릭 시 화면 전환을 지시합니다.
     */
    @Override
    public void handleMenuClick(String menuTitle) {
        if (!isViewAttached()) return;

        switch (menuTitle) {
            case "로그아웃":
                getView().showLogoutDialog();
                break;
            case "계정 탈퇴":
                getView().showDeleteAccountDialog();
                break;
            case "비밀번호 변경": // [변경] '비밀번호 변경' 케이스 분리
                getView().navigateToFindPassword(); // [변경] View에 화면 전환 지시
                break;
            case "프로필 수정":
                getView().showToast("준비 중인 기능입니다.");
                break;
        }
    }

    /**
     * [기존 주석 유지] View에 로그아웃 절차 실행을 지시합니다.
     */
    @Override
    public void logout() {
        if (isViewAttached()) {
            getView().executeLogout();
        }
    }

    /**
     * [기존 주석 유지] 계정 탈퇴를 실행합니다.
     */
    @Override
    public void deleteAccount() {
        if (isViewAttached()) {
            // TODO: 실제 계정 탈퇴 로직 구현 필요
            getView().showToast("계정 탈퇴 기능은 준비 중입니다.");
        }
    }
}
