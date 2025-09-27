package com.example.food_recipe.main;

import android.content.Context;
import android.util.Log;

// Google 로그아웃 관련 import
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.example.food_recipe.R;

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

    private static final String TAG = "MainModel";
    private final Context appContext;

    public MainModel(Context context) {
        this.appContext = context.getApplicationContext();
    }

    /**
     * Presenter가 "로그아웃 처리해줘" 라고 호출하면 실행되는 메서드입니다. (변경된부분)
     * @param loginProvider 현재 로그인된 방식 (AutoLoginManager.PROVIDER_EMAIL, AutoLoginManager.PROVIDER_GOOGLE 등) (새로추가됨)
     * @param cb 작업이 끝난 후 Presenter에게 결과를 알려줄 콜백 객체
     */
    @Override
    public void logout(String loginProvider, LogoutCallback cb) { // (핵심 변경) loginProvider 파라미터 추가
        Log.d(TAG, "logout:called with loginProvider = " + loginProvider);
        try {
            Log.d(TAG, "logout:Attempting AutoLoginManager.logout() which includes Firebase signOut...");
            AutoLoginManager.logout(appContext);
            Log.d(TAG, "logout:AutoLoginManager.logout() successful (Firebase signOut and local flags cleared, provider set to UNKNOWN).");

            if (AutoLoginManager.PROVIDER_GOOGLE.equals(loginProvider)) {
                Log.d(TAG, "logout:Login provider is GOOGLE. Attempting GoogleSignInClient.signOut...");
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(appContext.getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build();
                GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(appContext, gso);

                googleSignInClient.signOut().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "logout:GoogleSignInClient.signOut successful for GOOGLE provider.");
                        cb.onSuccess();
                    } else {
                        Log.w(TAG, "logout:GoogleSignInClient.signOut failure for GOOGLE provider.", task.getException());
                        cb.onError(task.getException());
                    }
                });
            } else {
                Log.d(TAG, "logout:Login provider is " + loginProvider + ". Skipping GoogleSignInClient.signOut. All necessary logout steps (Firebase, local flags) are done.");
                cb.onSuccess();
            }
        } catch (Exception e) {
            Log.e(TAG, "logout:Exception during synchronous part of logout (e.g., AutoLoginManager or GSO/GSC init).", e);
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
