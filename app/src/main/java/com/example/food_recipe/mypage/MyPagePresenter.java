package com.example.food_recipe.mypage;

import android.content.Context;

import com.example.food_recipe.base.BasePresenter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * [기존 주석 유지] MyPage의 비즈니스 로직을 처리하는 Presenter. BasePresenter를 상속받습니다.
 * [변경] 로그아웃과 계정 탈퇴의 성공 콜백을 분리하여 처리합니다.
 */
public class MyPagePresenter extends BasePresenter<MyPageContract.View> implements MyPageContract.Presenter, MyPageContract.Model.OnFinishedListener {

    private final FirebaseAuth firebaseAuth;
    private final MyPageContract.Model model;

    public MyPagePresenter(Context context) {
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.model = new MyPageModel(context);
    }

    @Override
    public void attachView(MyPageContract.View view) {
        super.attachView(view);
    }

    @Override
    public void loadUserData() {
        if (isViewAttached()) {
            FirebaseUser currentUser = firebaseAuth.getCurrentUser();
            if (currentUser != null) {
                getView().showUserInfo(currentUser);
            }
        }
    }

    @Override
    public void handleMenuClick(String menuTitle) {
        if (!isViewAttached()) return;

        switch (menuTitle) {
            case "프로필 수정":
                getView().navigateToEditProfile();
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

    @Override
    public void logout() {
        if (isViewAttached()) {
            model.logout(this);
        }
    }

    @Override
    public void deleteAccount() {
        if (isViewAttached()) {
            model.deleteAccount(this);
        }
    }

    /**
     * [추가] 로그아웃 성공 시 Model로부터 호출됩니다.
     */
    @Override
    public void onLogoutSuccess() {
        if (isViewAttached()) {
            getView().showToast("로그아웃 되었습니다.");
            getView().navigateToLogin();
        }
    }

    /**
     * [추가] 계정 탈퇴 성공 시 Model로부터 호출됩니다.
     */
    @Override
    public void onDeleteAccountSuccess() {
        if (isViewAttached()) {
            getView().showToast("계정이 삭제되었습니다.");
            getView().navigateToLogin();
        }
    }

    /**
     * [기존 주석 유지] 작업 실패 시 Model로부터 호출됩니다.
     */
    @Override
    public void onError(String message) {
        if (isViewAttached()) {
            getView().showToast(message);
        }
    }
}
