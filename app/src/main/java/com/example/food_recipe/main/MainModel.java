package com.example.food_recipe.main;

import android.content.Context;

import com.example.food_recipe.utils.AutoLoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * 데이터와 관련된 모든 실제 작업을 담당하는 Model 입니다.
 * MVP 패턴에서 Model 역할을 수행하며, MainContract.Model 인터페이스를 구현합니다.
 * Presenter로부터 요청을 받으면, 데이터베이스, 네트워크, 파일 등 데이터 소스에 접근하여
 * 실제 작업을 처리하고 그 결과를 콜백을 통해 Presenter에게 전달합니다.
 * Model은 View나 Presenter가 누구인지 전혀 알지 못합니다.
 */
public class MainModel implements MainContract.Model {

    // AutoLoginManager와 같은 유틸리티 클래스를 사용하기 위해 Context가 필요할 수 있습니다.
    // 메모리 누수를 방지하기 위해 항상 Application Context를 사용하는 것이 안전합니다.
    private final Context appContext;

    public MainModel(Context context) {
        this.appContext = context.getApplicationContext();
    }

    /**
     * Presenter가 "로그아웃 처리해줘" 라고 호출하면 실행되는 메서드입니다.
     * @param cb 작업이 끝난 후 Presenter에게 결과를 알려줄 콜백 객체
     */
    @Override
    public void logout(LogoutCallback cb) {
        try {
            // 자동 로그인 정보를 삭제하는 실제 작업을 수행합니다.
            AutoLoginManager.logout(appContext);
            // 작업이 성공했음을 콜백을 통해 Presenter에게 알립니다.
            cb.onSuccess();
        } catch (Exception e) {
            // 작업 중 오류가 발생했음을 콜백을 통해 Presenter에게 알립니다.
            cb.onError(e);
        }
    }

    /**
     * Presenter가 "로그인 상태 확인해줘" 라고 호출하면 실행되는 메서드입니다.
     * @param cb 작업이 끝난 후 Presenter에게 결과를 알려줄 콜백 객체
     */
    @Override
    public void checkLoginStatus(LoginStatusCallback cb) {
        // Firebase 인증 시스템을 통해 현재 로그인된 사용자가 있는지 확인합니다.
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            // 로그인된 사용자가 있으면, "로그인 되어있어" 라고 콜백을 통해 Presenter에게 알립니다.
            cb.onLoggedIn();
        } else {
            // 로그인된 사용자가 없으면, "로그아웃 상태야" 라고 콜백을 통해 Presenter에게 알립니다.
            cb.onLoggedOut();
        }
    }
}
