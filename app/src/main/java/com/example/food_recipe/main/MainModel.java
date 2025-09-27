package com.example.food_recipe.main;

import android.content.Context;
import android.util.Log; // (새로추가됨) 로그 기능을 사용하기 위해 import

// Google 로그아웃 관련 import
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.example.food_recipe.R; // R.string.default_web_client_id 사용을 위해

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

    // (새로추가됨) Logcat 출력을 위한 태그 정의
    private static final String TAG = "MainModel";

    private final Context appContext;

    public MainModel(Context context) {
        this.appContext = context.getApplicationContext();
    }

    @Override
    public void logout(LogoutCallback cb) {
        // (새로추가됨) logout 메소드 호출 시점 로그
        Log.d(TAG, "logout:called - 전체 로그아웃 프로세스 시작");
        try {
            // (새로추가됨) Firebase 로그아웃 시도 로그
            Log.d(TAG, "logout:Attempting Firebase signOut...");
            FirebaseAuth.getInstance().signOut();
            // (새로추가됨) Firebase 로그아웃 성공 로그
            Log.d(TAG, "logout:Firebase signOut successful.");

            // (새로추가됨) AutoLoginManager 로그아웃 시도 로그
            Log.d(TAG, "logout:Attempting AutoLoginManager.logout...");
            AutoLoginManager.logout(appContext);
            // (새로추가됨) AutoLoginManager 로그아웃 성공 로그
            Log.d(TAG, "logout:AutoLoginManager.logout successful.");

            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(appContext.getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();

            GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(appContext, gso);
            
            // (새로추가됨) GoogleSignInClient 로그아웃 시도 로그
            Log.d(TAG, "logout:Attempting GoogleSignInClient.signOut...");
            googleSignInClient.signOut().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // (새로추가됨) GoogleSignInClient 로그아웃 성공 로그
                    Log.d(TAG, "logout:GoogleSignInClient.signOut successful.");
                    cb.onSuccess();
                } else {
                    // (새로추가됨) GoogleSignInClient 로그아웃 실패 로그 및 예외 정보
                    Log.w(TAG, "logout:GoogleSignInClient.signOut failure.", task.getException());
                    cb.onError(task.getException());
                }
            });

        } catch (Exception e) {
            // (새로추가됨) 로그아웃 동기 처리 중 예외 발생 시 로그 및 예외 정보
            Log.e(TAG, "logout:Exception during synchronous part of logout (Firebase signOut or AutoLoginManager).", e);
            cb.onError(e);
        }
    }

    @Override
    public void checkLoginStatus(LoginStatusCallback cb) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            cb.onLoggedIn();
        } else {
            cb.onLoggedOut();
        }
    }
}
