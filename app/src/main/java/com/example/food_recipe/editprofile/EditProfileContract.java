package com.example.food_recipe.editprofile;

import com.example.food_recipe.base.BaseContract;

/**
 * [파일 생성] 프로필 수정 기능에 대한 Contract. MVP 구조의 각 컴포넌트 역할을 정의합니다.
 */
public interface EditProfileContract {

    /**
     * [추가] View가 구현해야 하는 메서드 목록
     */
    interface View extends BaseContract.View {
        void showCurrentUsername(String username);
        void showProgress();
        void hideProgress();
        void showSuccessAndClose(String message, String newUsername);
        void showError(String message);
        void setUsernameError(String message); // 닉네임 유효성 검사 에러 표시
    }

    /**
     * [추가] Presenter가 구현해야 하는 메서드 목록
     */
    interface Presenter extends BaseContract.Presenter<View> {
        void loadCurrentUsername();
        void saveUsername(String newUsername);
    }

    /**
     * [추가] Model이 구현해야 하는 메서드 목록 (데이터 처리)
     */
    interface Model {
        // [추가] Model의 비동기 작업 결과를 Presenter에게 전달하기 위한 콜백
        interface OnFinishedListener {
            void onSuccess(String newUsername); // [변경] 성공 시 새 닉네임을 전달하여 View에 즉시 반영하도록 함
            void onError(String message);
        }
        void updateUsername(String newUsername, OnFinishedListener listener);
    }
}
