// FindPsPresenter.java
package com.example.food_recipe.findps;

public class FindPsPresenter implements FindPsContract.Presenter {
    private final FindPsContract.View view;
    private final FindPsContract.Model model;

    public FindPsPresenter(FindPsContract.View view, FindPsContract.Model model) {
        this.view = view;
        this.model = model;
    }

    @Override
    public void onVerifyEmailClicked(String email) {
        if (email == null || email.trim().isEmpty()) {
            view.showEmailEmptyError();
            return;
        }

        model.sendPasswordResetEmail(email.trim(), new FindPsContract.Model.Callback() {
            @Override
            public void onSuccess() {
                view.showResetEmailSentMessage();
            }

            @Override
            public void onFailure() {
                view.showResetEmailFailedMessage();
            }


        });
    }

    @Override
    public void detachView() {
        android.util.Log.d("FindPsPresenter", "detachView() called, view 참조 해제 요청");
        // 현재 view가 final이라 null 처리 불가 → 로그만 남김
        // 나중에 view = null 처리하려면 final 제거해야 함
        // 현재는 정리할 리소스 없음
    }


}
