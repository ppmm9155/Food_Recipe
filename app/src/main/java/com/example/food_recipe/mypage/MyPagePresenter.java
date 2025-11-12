package com.example.food_recipe.mypage;

import com.example.food_recipe.base.BasePresenter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * [기존 주석 유지]
 */
public class MyPagePresenter extends BasePresenter<MyPageContract.View> implements MyPageContract.Presenter, MyPageContract.Model.OnFinishedListener {

    private FirebaseAuth firebaseAuth;
    private MyPageContract.Model model;

    /**
     * [수정] 생성자에서 Context 주입을 제거하여 메모리 누수 위험을 방지합니다.
     */
    public MyPagePresenter() {
        // FirebaseAuth와 Model 초기화는 View가 연결되는 시점에 안전하게 처리됩니다.
    }
    
    /**
     * [추가] 단위 테스트를 할 때 가짜(Mock) Model을 주입하기 위한 보조 생성자입니다.
     */
    public MyPagePresenter(MyPageContract.Model model, FirebaseAuth auth) {
        this.model = model;
        this.firebaseAuth = auth;
    }

    /**
     * [수정] View가 연결될 때 FirebaseAuth와 Model을 초기화합니다.
     */
    @Override
    public void attachView(MyPageContract.View view) {
        super.attachView(view);
        if (model == null) {
            this.firebaseAuth = FirebaseAuth.getInstance();
            this.model = new MyPageModel(getView().getContext());
        }
    }

    // --- 기존 로직 모두 그대로 유지 ---
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

    @Override
    public void onLogoutSuccess() {
        if (isViewAttached()) {
            getView().showToast("로그아웃 되었습니다.");
            getView().navigateToLogin();
        }
    }

    @Override
    public void onDeleteAccountSuccess() {
        if (isViewAttached()) {
            getView().showToast("계정이 삭제되었습니다.");
            getView().navigateToLogin();
        }
    }

    @Override
    public void onError(String message) {
        if (isViewAttached()) {
            getView().showToast(message);
        }
    }
}
