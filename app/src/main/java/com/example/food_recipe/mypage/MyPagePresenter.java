package com.example.food_recipe.mypage;

import com.example.food_recipe.base.BasePresenter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * [기존 주석 유지] MyPage의 비즈니스 로직을 처리하는 Presenter. BasePresenter를 상속받습니다.
 */
public class MyPagePresenter extends BasePresenter<MyPageContract.View> implements MyPageContract.Presenter, MyPageContract.Model.OnFinishedListener { // [변경] Model의 리스너를 구현

    /**
     * [기존 주석 유지] Firebase 인증 인스턴스
     */
    private final FirebaseAuth firebaseAuth;
    /**
     * [추가] 계정 탈퇴 등 데이터 처리를 담당할 Model
     */
    private final MyPageContract.Model model;


    /**
     * [변경] Model 인스턴스를 생성하도록 생성자를 수정합니다.
     */
    public MyPagePresenter() {
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.model = new MyPageModel(); // [추가] Model 초기화
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
     * [변경] '프로필 수정' 메뉴 클릭 시 화면 전환을 지시하도록 수정합니다.
     */
    @Override
    public void handleMenuClick(String menuTitle) {
        if (!isViewAttached()) return;

        switch (menuTitle) {
            case "프로필 수정": // [변경] '프로필 수정' 케이스 처리
                getView().navigateToEditProfile(); // [변경] View에 화면 전환 지시
                break;
            case "비밀번호 변경":
                getView().navigateToFindPassword();
                break;
            case "로그아웃":
                getView().showLogoutDialog();
                break;
            case "계정 탈퇴":
                getView().showDeleteAccountDialog();
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
     * [변경] 실제 계정 탈퇴 로직을 Model에 위임하도록 수정합니다.
     */
    @Override
    public void deleteAccount() {
        if (isViewAttached()) {
            // [변경] 준비 중이던 Toast 메시지 대신, Model의 deleteAccount 메서드를 호출합니다.
            // [추가] Presenter 자신을 리스너로 전달하여 결과를 콜백으로 받습니다.
            model.deleteAccount(this);
        }
    }

    /**
     * [추가] 계정 탈퇴 성공 시 Model로부터 호출됩니다. (OnFinishedListener 구현)
     */
    @Override
    public void onSuccess() {
        if (isViewAttached()) {
            getView().showToast("계정 탈퇴가 완료되었습니다.");
            getView().executeLogout(); // 로그아웃 절차를 재사용하여 화면을 전환합니다.
        }
    }

    /**
     * [추가] 계정 탈퇴 실패 시 Model로부터 호출됩니다. (OnFinishedListener 구현)
     */
    @Override
    public void onError(String message) {
        if (isViewAttached()) {
            getView().showToast(message);
        }
    }
}
