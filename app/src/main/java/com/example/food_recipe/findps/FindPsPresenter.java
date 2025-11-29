// FindPsPresenter.java
package com.example.food_recipe.findps;

import com.example.food_recipe.base.BasePresenter;

// [변경] BasePresenter를 상속받아 View 생명주기를 안전하게 관리
public class FindPsPresenter extends BasePresenter<FindPsContract.View> implements FindPsContract.Presenter {
    // [삭제] view 멤버 변수. BasePresenter가 관리하므로 제거.
    private final FindPsContract.Model model;

    // [변경] 생성자에서 View를 받지 않음.
    public FindPsPresenter(FindPsContract.Model model) {
        this.model = model;
    }

    @Override
    public void onVerifyEmailClicked(String email) {
        if (!isViewAttached()) return;
        if (email == null || email.trim().isEmpty()) {
            getView().showEmailEmptyError();
            return;
        }

        model.sendPasswordResetEmail(email.trim(), new FindPsContract.Model.Callback() {
            @Override
            public void onSuccess() {
                if (!isViewAttached()) return;
                getView().showResetEmailSentMessage();
            }

            @Override
            public void onFailure() {
                if (!isViewAttached()) return;
                getView().showResetEmailFailedMessage();
            }
        });
    }

    // [삭제] detachView()는 BasePresenter에 구현되어 있으므로 제거
}
