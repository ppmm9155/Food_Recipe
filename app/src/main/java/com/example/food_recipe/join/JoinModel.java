package com.example.food_recipe.join;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 📦 Model (데이터 담당)
 * - FirebaseAuth, Firestore 같은 외부 서비스와 통신하는 코드만 담당
 * - View나 Presenter는 Firebase 구체적인 코드 몰라도 됨
 * - 테스트용: Firebase 연결 상태를 Log로 확인 가능
 */
public class JoinModel implements JoinContract.Model {

    private static final String TAG = "JoinModel";

    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    // ===== Firebase 상태 점검용 메서드 =====
    public void logFirebaseStatus() {
        try {
            com.google.firebase.FirebaseOptions opt = com.google.firebase.FirebaseApp.getInstance().getOptions();
            Log.d(TAG, "Firebase Project Info:");
            Log.d(TAG, "API Key: " + opt.getApiKey());
            Log.d(TAG, "ApplicationId: " + opt.getApplicationId());
            Log.d(TAG, "ProjectId: " + opt.getProjectId());
            Log.d(TAG, "DatabaseUrl: " + opt.getDatabaseUrl());
        } catch (Exception e) {
            Log.e(TAG, "FirebaseApp not initialized", e);
        }

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Log.d(TAG, "Current User: " + currentUser.getEmail() + ", UID: " + currentUser.getUid());
        } else {
            Log.d(TAG, "No user currently logged in");
        }

        // Firestore 테스트 쓰기
        db.collection("test_connection").document("ping")
                .set(Collections.singletonMap("timestamp", FieldValue.serverTimestamp()))
                .addOnSuccessListener(v -> Log.d(TAG, "Firestore write test success"))
                .addOnFailureListener(e -> Log.e(TAG, "Firestore write test failed", e));
    }

    // ===== 아이디 중복 확인 =====
    @Override
    public void checkUsernameAvailability(String lowerUsername, UsernameCallback callback) {
        db.collection("usernames").document(lowerUsername).get()
                .addOnSuccessListener(doc -> {
                    boolean available = !doc.exists();
                    Log.d(TAG, "Username check: " + lowerUsername + " available=" + available);
                    callback.onResult(available);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Username check failed", e);
                    callback.onError(e);
                });
    }

    // ===== 이메일 중복 확인 =====
    @Override
    public void checkEmailAvailability(String email, EmailCallback callback) {
        mAuth.fetchSignInMethodsForEmail(email)
                .addOnSuccessListener(result -> {
                    List<String> methods = (result != null) ? result.getSignInMethods() : null;
                    boolean available = (methods != null && methods.isEmpty());
                    Log.d(TAG, "Email check: " + email + ", available=" + available + ", methods=" + methods);
                    callback.onResult(available);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Email check failed", e);
                    callback.onError(e);
                });
    }

    // ===== 회원가입 + Firestore 저장 =====
    @Override
    public void createUserThenSaveProfile(String username, String email, String password, RegisterCallback callback) {
        Log.d(TAG, "Attempting user creation: " + email + " / username: " + username);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener((AuthResult result) -> {
                    FirebaseUser user = result.getUser();
                    if (user == null) {
                        callback.onError(new IllegalStateException("User is null after creation"));
                        return;
                    }
                    Log.d(TAG, "User created successfully: " + user.getEmail());

                    // 프로필 표시 이름 업데이트
                    user.updateProfile(new UserProfileChangeRequest.Builder()
                                    .setDisplayName(username)
                                    .build())
                            .addOnSuccessListener(v -> Log.d(TAG, "Profile displayName updated"))
                            .addOnFailureListener(e -> Log.w(TAG, "Profile update failed", e));

                    // 이메일 인증 메일 발송
                    user.sendEmailVerification()
                            .addOnSuccessListener(v -> Log.d(TAG, "Email verification sent"))
                            .addOnFailureListener(e -> Log.w(TAG, "Email verification failed", e));

                    // Firestore 저장
                    saveUserProfileBatch(user, username, email, callback);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "User creation failed", e);
                    callback.onError(e);
                });
    }

    private void saveUserProfileBatch(@NonNull FirebaseUser fUser,
                                      @NonNull String username,
                                      @NonNull String email,
                                      RegisterCallback callback) {

        String uid = fUser.getUid();
        String lower = username.toLowerCase(Locale.ROOT);

        DocumentReference userRef = db.collection("users").document(uid);
        DocumentReference nameRef = db.collection("usernames").document(lower);

        Map<String, Object> userDoc = new HashMap<>();
        userDoc.put("uid", uid);
        userDoc.put("username", username);
        userDoc.put("usernameLower", lower);
        userDoc.put("email", email);
        userDoc.put("emailVerified", fUser.isEmailVerified());
        userDoc.put("createdAt", FieldValue.serverTimestamp());
        userDoc.put("provider", "password");

        Map<String, Object> unameDoc = new HashMap<>();
        unameDoc.put("uid", uid);
        unameDoc.put("createdAt", FieldValue.serverTimestamp());

        WriteBatch batch = db.batch();
        batch.set(nameRef, unameDoc);
        batch.set(userRef, userDoc);

        batch.commit()
                .addOnSuccessListener(v -> {
                    Log.d(TAG, "Firestore batch write successful");
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Firestore batch write failed", e);
                    callback.onError(e);
                });
    }

    // ===== Callback 인터페이스 =====
    public interface UsernameCallback {
        void onResult(boolean available);
        void onError(Exception e);
    }

    public interface EmailCallback {
        void onResult(boolean available);
        void onError(Exception e);
    }

    public interface RegisterCallback {
        void onSuccess();
        void onError(Exception e);
    }
}
