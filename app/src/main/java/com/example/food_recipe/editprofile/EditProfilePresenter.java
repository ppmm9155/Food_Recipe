package com.example.food_recipe.editprofile;

import android.text.TextUtils;
import com.example.food_recipe.base.BasePresenter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * [파일 생성] 프로필 수정 기능의 Presenter. View와 Model을 연결하는 비즈니스 로직을 담당합니다.
 */
public class EditProfilePresenter extends BasePresenter<EditProfileContract.View>
        implements EditProfileContract.Presenter, EditProfileContract.Model.OnFinishedListener {

    private final EditProfileContract.Model model;

    public EditProfilePresenter() {
        this.model = new EditProfileModel();
    }

    @Override
    public void loadCurrentUsername() {
        if (!isViewAttached()) return;

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && user.getDisplayName() != null) {
            getView().showCurrentUsername(user.getDisplayName());
        }
    }

    @Override
    public void saveUsername(String newUsername) {
        if (!isViewAttached()) return;

        // [추가] 닉네임 유효성 검사
        if (TextUtils.isEmpty(newUsername)) {
            getView().setUsernameError("닉네임을 입력해주세요.");
            return;
        }
        if (newUsername.length() < 2) {
            getView().setUsernameError("닉네임은 2자 이상이어야 합니다.");
            return;
        }
        // TODO: 욕설 필터링 등 추가적인 유효성 검사 추가 가능

        getView().showProgress();
        model.updateUsername(newUsername, this);
    }

    // ===== Model.OnFinishedListener 구현 =====

    @Override
    public void onSuccess(String newUsername) {
        if (isViewAttached()) {
            getView().hideProgress();
            getView().showSuccessAndClose("닉네임이 성공적으로 변경되었습니다.", newUsername);
        }
    }

    @Override
    public void onError(String message) {
        if (isViewAttached()) {
            getView().hideProgress();
            getView().showError(message);
        }
    }
}
