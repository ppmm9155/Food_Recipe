package com.example.food_recipe.login;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class LoginModel implements LoginContract.Model {

    private static final String TAG = "LoginModel";

    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance(); // Firestore 인스턴스 추가

    @Override
    public void signInWithEmail(String email, String password, AuthCallback callback) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess(mAuth.getCurrentUser());
                    } else {
                        callback.onFailure(task.getException());
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    @Override
    public void fetchSignInMethods(String email, FetchCallback callback) {
        mAuth.fetchSignInMethodsForEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        callback.onResult(task.getResult().getSignInMethods());
                    } else {
                        callback.onResult(null);
                    }
                })
                .addOnFailureListener(e -> callback.onResult(null));
    }

    @Override
    public void signInWithGoogle(String idToken, AuthCallback callback) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            Log.d(TAG, "signInWithGoogle:success | User UID: " + user.getUid());
                            // Firestore에 사용자 정보 저장 로직 호출
                            saveGoogleUserToFirestore(user, callback);
                        } else {
                            callback.onFailure(new IllegalStateException("FirebaseUser is null after Google Sign-In."));
                        }
                    } else {
                        Log.w(TAG, "signInWithGoogle:failure", task.getException());
                        callback.onFailure(task.getException());
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "signInWithGoogle:addOnFailureListener", e);
                    callback.onFailure(e);
                });
    }

    /**
     * 구글 로그인 성공 후, Firestore 'users' 컬렉션에 사용자 정보를 저장합니다.
     * 이미 해당 UID의 문서가 존재하면 아무 작업도 하지 않습니다. (최초 로그인 시에만 저장)
     */
    private void saveGoogleUserToFirestore(@NonNull FirebaseUser fUser, AuthCallback callback) {
        DocumentReference userRef = db.collection("users").document(fUser.getUid());

        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null && document.exists()) {
                    // 문서가 이미 존재하므로, Firestore에 쓸 필요 없이 바로 로그인 성공 처리
                    Log.d(TAG, "User profile already exists in Firestore. UID: " + fUser.getUid());
                    callback.onSuccess(fUser);
                } else {
                    // 문서가 없으므로, 새로 생성 (최초 구글 로그인)
                    Log.d(TAG, "User profile does not exist. Creating new one. UID: " + fUser.getUid());
                    writeNewGoogleUserProfile(fUser, callback);
                }
            } else {
                // 문서 존재 여부 확인 실패. 일단 로그는 남기지만, 로그인 흐름은 계속 진행시킴.
                Log.e(TAG, "Failed to check user existence in Firestore.", task.getException());
                callback.onSuccess(fUser); // DB에 쓰지 못했더라도 인증은 성공했으므로 콜백 호출
            }
        });
    }

    /**
     * Firestore에 새로운 구글 사용자 프로필 문서를 생성합니다.
     */
    private void writeNewGoogleUserProfile(@NonNull FirebaseUser fUser, AuthCallback callback) {
        String uid = fUser.getUid();
        String username = fUser.getDisplayName();
        if (username == null || username.trim().isEmpty()) {
            // 표시 이름이 없는 경우 이메일 앞부분을 사용
            username = fUser.getEmail().split("@")[0];
        }
        String lower = username.toLowerCase(Locale.ROOT);

        // 'usernames' 컬렉션용 문서 참조
        DocumentReference nameRef = db.collection("usernames").document(lower);

        Map<String, Object> userDoc = new HashMap<>();
        userDoc.put("uid", uid);
        userDoc.put("username", username);
        userDoc.put("usernameLower", lower);
        userDoc.put("email", fUser.getEmail());
        userDoc.put("emailVerified", fUser.isEmailVerified());
        userDoc.put("createdAt", FieldValue.serverTimestamp());
        userDoc.put("provider", "google.com"); // provider를 google.com으로 명시
        userDoc.put("myIngredients", new ArrayList<>()); // 음식 재료 필드 추가

        Map<String, Object> unameDoc = new HashMap<>();
        unameDoc.put("uid", uid);
        unameDoc.put("createdAt", FieldValue.serverTimestamp());

        // Batch Write로 users와 usernames 컬렉션에 원자적으로 쓰기
        WriteBatch batch = db.batch();
        batch.set(nameRef, unameDoc);
        batch.set(db.collection("users").document(uid), userDoc);

        batch.commit()
                .addOnSuccessListener(v -> {
                    Log.d(TAG, "Firestore batch write successful for Google user.");
                    callback.onSuccess(fUser); // DB 저장 성공 후 최종 로그인 성공 콜백
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Firestore batch write failed for Google user.", e);
                    // DB 저장은 실패했지만, 인증 자체는 성공했으므로 로그인 성공 콜백을 호출해줌
                    callback.onSuccess(fUser);
                });
    }
}
