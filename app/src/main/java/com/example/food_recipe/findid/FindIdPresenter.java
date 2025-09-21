// FindIdPresenter.java
package com.example.food_recipe.findid;

public class FindIdPresenter implements FindIdContract.Presenter {
    private final FindIdContract.View view;
    private final FindIdContract.Model model;

    public FindIdPresenter(FindIdContract.View view, FindIdContract.Model model) {
        this.view = view;
        this.model = model;
    }

    @Override
    public void onVerifyEmailClicked(String email) {
        if (email.isEmpty()) {
            view.showErrorMessage("이메일을 입력하세요.");
            return;
        }

        model.sendVerificationEmail(email, new FindIdContract.Model.Callback() {
            @Override
            public void onSuccess() {
                view.showEmailSentMessage();
            }

            @Override
            public void onError(String error) {
                view.showErrorMessage(error);
            }
        });
    }

    @Override
    public void onLoginClicked() {
        view.showLoginRedirectMessage();
    }

    @Override
    public void detachView() {
        android.util.Log.d("FindIdPresenter", "detachView() called, view 참조 해제 요청");
        // 현재 view가 final이라 null 처리 불가 → 로그만 남김
        // 나중에 view = null 처리하려면 final 제거해야 함
        // 현재는 정리할 리소스 없음
    }
}
