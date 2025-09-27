package com.example.food_recipe.login;

import android.util.Log; // (새로추가됨) 로그 사용을 위해

import com.google.firebase.auth.*;

import java.util.List;

// ✅ Model 클래스
// - FirebaseAuth를 사용해서 실제 "데이터 처리(로그인, 이메일 확인)"를 담당
// - View나 Presenter에 의존하지 않고 오직 Firebase와 통신만 함
public class LoginModel implements LoginContract.Model {

    // (새로추가됨) 로그 태그
    private static final String TAG = "LoginModel";

    // Firebase 인증 객체 생성 (싱글톤: 앱 전체에서 하나만 사용)
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();

    // 🔹 이메일 + 비밀번호 로그인 실행
    // - Presenter가 호출 → Model이 Firebase와 통신 → 결과를 Callback으로 돌려줌
    @Override
    public void signInWithEmail(String email, String password, AuthCallback callback) {
        mAuth.signInWithEmailAndPassword(email, password)   // Firebase 로그인 API 호출
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // 로그인 성공 → 현재 로그인된 FirebaseUser 반환
                        callback.onSuccess(mAuth.getCurrentUser());
                    } else {
                        // 로그인 실패 → Exception 전달
                        callback.onFailure(task.getException());
                    }
                })
                // 네트워크 오류 등 예외 처리
                .addOnFailureListener(callback::onFailure);
    }

    // 🔹 특정 이메일의 로그인 방식(비밀번호/구글 등) 확인
    // - 비밀번호 틀림 vs 사용자 없음 vs 다른 로그인 방식(구글 로그인 등)을 구분하기 위함
    @Override
    public void fetchSignInMethods(String email, FetchCallback callback) {
        mAuth.fetchSignInMethodsForEmail(email)   // Firebase API 호출
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        // 로그인 가능한 방법들(ex: "password", "google.com") 리스트 반환
                        callback.onResult(task.getResult().getSignInMethods());
                    } else {
                        // 실패 시 null 전달
                        callback.onResult(null);
                    }
                })
                // 네트워크 오류 발생 시에도 null 반환
                .addOnFailureListener(e -> callback.onResult(null));
    }
    // 🔹 구글 로그인 실행
    // - Presenter가 호출 → Model이 Firebase와 통신 → 결과를 Callback으로 돌려줌
    @Override
    public void signInWithGoogle(String idToken, AuthCallback callback) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)   // Firebase 구글 로그인 API 호출
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        // (새로추가됨) Google 로그인 성공 로그
                        Log.d(TAG, "signInWithGoogle:success | User UID: " + (user != null ? user.getUid() : "null"));
                        callback.onSuccess(user);
                    } else {
                        // (새로추가됨) Google 로그인 실패 로그
                        Log.w(TAG, "signInWithGoogle:failure", task.getException());
                        callback.onFailure(task.getException());
                    }
                })
                // 네트워크 오류 등 예외 처리
                .addOnFailureListener(e -> {
                    // (새로추가됨) Google 로그인 리스너 자체 실패 로그
                    Log.e(TAG, "signInWithGoogle:addOnFailureListener", e);
                    callback.onFailure(e);
                });
    }

}
