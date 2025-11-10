// FindIdPresenter.java
package com.example.food_recipe.findid;

import com.example.food_recipe.base.BasePresenter;

// [변경] BasePresenter를 상속받아 View 생명주기를 안전하게 관리
public class FindIdPresenter extends BasePresenter<FindIdContract.View> implements FindIdContract.Presenter {
    // [삭제] view 멤버 변수. BasePresenter가 관리하므로 제거.
    private final FindIdContract.Model model;

    // [변경] 생성자에서 View를 받지 않음.
    public FindIdPresenter(FindIdContract.Model model) {
        this.model = model;
    }

    @Override
    public void onVerifyEmailClicked(String email) {
        if (!isViewAttached()) return;
        if (email.isEmpty()) {
            getView().showErrorMessage("이메일을 입력하세요.");
            return;
        }

        model.sendVerificationEmail(email, new FindIdContract.Model.Callback() {
            @Override
            public void onSuccess() {
                if (!isViewAttached()) return;
                getView().showEmailSentMessage();
            }

            @Override
            public void onError(String error) {
                if (!isViewAttached()) return;
                getView().showErrorMessage(error);
            }
        });
    }

    @Override
    public void onLoginClicked() {
        if (!isViewAttached()) return;
        getView().showLoginRedirectMessage();
    }

    // [삭제] detachView()는 BasePresenter에 구현되어 있으므로 제거
}
