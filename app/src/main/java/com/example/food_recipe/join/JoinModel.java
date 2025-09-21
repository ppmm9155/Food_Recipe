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

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * 📦 Model (데이터 담당)
 * - FirebaseAuth, Firestore 같은 외부 서비스와 통신하는 코드만 담당합니다.
 * - View나 Presenter는 Firebase의 구체적인 코드를 몰라도 됩니다.
 */
public class JoinModel implements JoinContract.Model {

    private static final String TAG = "JoinModel";

    // Firebase 인증 & Firestore 객체
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * ✅ 아이디 중복확인
     * - Firestore의 "usernames" 컬렉션에서 동일한 문서가 있는지 확인
     */
    @Override
    public void checkUsernameAvailability(String lowerUsername, UsernameCallback callback) {
        db.collection("usernames").document(lowerUsername).get()
                .addOnSuccessListener(doc -> callback.onResult(!doc.exists())) // 존재하지 않으면 사용 가능
                .addOnFailureListener(callback::onError);
    }

    /**
     * ✅ 이메일 사용 가능 여부 확인
     * - FirebaseAuth의 fetchSignInMethodsForEmail 사용
     */
    @Override
    public void checkEmailAvailability(String email, EmailCallback callback) {
        mAuth.fetchSignInMethodsForEmail(email)
                .addOnSuccessListener(result -> {
                    // 🔎 어떤 프로젝트에 붙어있는지 확인
                    com.google.firebase.FirebaseOptions opt =
                            com.google.firebase.FirebaseApp.getInstance().getOptions();
                    android.util.Log.d("JoinModel",
                            "checkEmailAvailability email=" + email
                                    + ", projectId=" + opt.getProjectId());

                    java.util.List<String> methods = (result != null) ? result.getSignInMethods() : null;
                    android.util.Log.d("JoinModel", "signInMethods=" + methods);

                    // ✅ 리스트가 "비어있을 때만" 사용 가능
                    boolean available = (methods != null && methods.isEmpty());
                    callback.onResult(available);
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("JoinModel", "fetchSignInMethods error", e);
                    callback.onError(e);
                });
    }

    /**
     * ✅ 회원가입 + Firestore 저장
     * - FirebaseAuth로 계정 생성
     * - 이메일 인증 메일 발송
     * - Firestore에 users / usernames 문서 저장
     */
    @Override
    public void createUserThenSaveProfile(String username, String email, String password, RegisterCallback callback) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener((AuthResult result) -> {
                    FirebaseUser user = result.getUser();
                    if (user == null) {
                        callback.onError(new IllegalStateException("User is null after creation"));
                        return;
                    }

                    // 프로필 표시 이름 업데이트
                    user.updateProfile(new UserProfileChangeRequest.Builder()
                                    .setDisplayName(username)
                                    .build())
                            .addOnFailureListener(e -> Log.w(TAG, "updateProfile failed", e));

                    // 이메일 인증 메일 발송
                    user.sendEmailVerification()
                            .addOnSuccessListener(v -> Log.d(TAG, "sendEmailVerification success"))
                            .addOnFailureListener(e -> Log.w(TAG, "sendEmailVerification failed", e));

                    // Firestore 저장
                    saveUserProfileBatch(user, username, email, callback);
                })
                .addOnFailureListener(callback::onError);
    }

    /**
     * Firestore에 users / usernames 문서를 동시에 저장 (배치로 원자성 보장)
     */
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
                .addOnSuccessListener(v -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    // === Callback 인터페이스 ===
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
