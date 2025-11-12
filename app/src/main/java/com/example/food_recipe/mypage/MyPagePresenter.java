package com.example.food_recipe.mypage;

import android.content.Context;

import com.example.food_recipe.base.BasePresenter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * [기존 주석 유지] MyPage의 비즈니스 로직을 처리하는 Presenter. BasePresenter를 상속받습니다.
 */
public class MyPagePresenter extends BasePresenter<MyPageContract.View> implements MyPageContract.Presenter, MyPageContract.Model.OnFinishedListener {

    private final FirebaseAuth firebaseAuth;
    private final MyPageContract.Model model; // [변경] 생성자에서 초기화되도록 final로 변경

    // [변경] Model에게 Context를 전달하기 위해 생성자를 수정합니다.
    public MyPagePresenter(Context context) {
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.model = new MyPageModel(context); // [변경] MyPageModel에 context 전달
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
     * [기존 주석 유지] 메뉴 클릭 시의 동작을 정의합니다.
     */
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

    /**
     * [변경] 실제 로그아웃 로직을 Model에 위임하도록 수정합니다.
     */
    @Override
    public void logout() {
        if (isViewAttached()) {
            // [변경] Model의 logout 메서드를 호출하고, Presenter 자신을 리스너로 전달합니다.
            model.logout(this);
        }
    }

    /**
     * [기존 주석 유지] 실제 계정 탈퇴 로직을 Model에 위임하도록 수정합니다.
     */
    @Override
    public void deleteAccount() {
        if (isViewAttached()) {
            model.deleteAccount(this);
        }
    }

    /**
     * [변경] 로그아웃과 계정 탈퇴 성공 시 Model로부터 호출됩니다.
     */
    @Override
    public void onSuccess() {
        if (isViewAttached()) {
            // [변경] 어떤 작업이 성공했든, 최종적으로 로그인 화면으로 이동시킵니다.
            getView().showToast("요청하신 작업이 완료되었습니다.");
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
