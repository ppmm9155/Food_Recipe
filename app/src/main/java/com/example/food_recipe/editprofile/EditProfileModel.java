package com.example.food_recipe.editprofile;

import android.util.Log;
import androidx.annotation.NonNull;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue; // [추가] 서버 타임스탬프를 위한 import
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * [기존 주석 유지] 프로필 수정 기능의 Model. Firestore 데이터 처리를 담당합니다.
 */
public class EditProfileModel implements EditProfileContract.Model {

    private static final String TAG = "EditProfileModel";
    private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    public void updateUsername(@NonNull String newUsername, @NonNull OnFinishedListener listener) {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            listener.onError("로그인이 필요합니다.");
            return;
        }

        String newUsernameLower = newUsername.toLowerCase(Locale.ROOT);
        String uid = user.getUid();
        DocumentReference userDocRef = db.collection("users").document(uid);
        DocumentReference newUsernameRef = db.collection("usernames").document(newUsernameLower);

        db.runTransaction((Transaction.Function<Void>) transaction -> {
            // 1. 현재 사용자 문서 읽기
            DocumentSnapshot userSnapshot = transaction.get(userDocRef);
            if (!userSnapshot.exists()) {
                throw new FirebaseFirestoreException("사용자 정보가 존재하지 않습니다.", FirebaseFirestoreException.Code.NOT_FOUND);
            }
            String oldUsernameLower = userSnapshot.getString("usernameLower");

            // [변경] 닉네임이 실제로 변경되었는지 명확하게 확인 (기존 닉네임이 null인 경우도 처리)
            final boolean isUsernameChanged = oldUsernameLower == null || !oldUsernameLower.equals(newUsernameLower);

            if (isUsernameChanged) {
                // 2. (닉네임이 변경된 경우) 새 닉네임이 이미 사용 중인지 확인
                DocumentSnapshot newUsernameSnapshot = transaction.get(newUsernameRef);
                if (newUsernameSnapshot.exists()) {
                    throw new FirebaseFirestoreException("이미 사용 중인 닉네임입니다.", FirebaseFirestoreException.Code.ALREADY_EXISTS);
                }

                // 3. (닉네임이 변경된 경우) 이전 닉네임 문서 삭제
                if (oldUsernameLower != null) {
                    DocumentReference oldUsernameRef = db.collection("usernames").document(oldUsernameLower);
                    transaction.delete(oldUsernameRef);
                }

                // 4. (닉네임이 변경된 경우) 새 닉네임 문서 생성
                Map<String, Object> usernameData = new HashMap<>();
                usernameData.put("uid", uid);
                // [핵심 수정] 보안 규칙을 통과하기 위해 createdAt 타임스탬프 추가
                usernameData.put("createdAt", FieldValue.serverTimestamp());
                transaction.set(newUsernameRef, usernameData);
            }

            // 5. 사용자 문서 업데이트 (닉네임이 변경되지 않은 경우에도 중복 실행되지만, 현재 흐름상 허용)
            transaction.update(userDocRef, "username", newUsername);
            transaction.update(userDocRef, "usernameLower", newUsernameLower);

            return null;
        }).addOnSuccessListener(aVoid -> {
            Log.d(TAG, "Firestore transaction successful.");
            // Firestore 트랜잭션 성공 후, FirebaseAuth 프로필도 업데이트
            updateFirebaseAuthProfile(newUsername, listener);
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Firestore transaction failed.", e);
            if (e != null && e.getMessage() != null) {
                listener.onError(e.getMessage());
            } else {
                listener.onError("닉네임 변경 중 오류가 발생했습니다.");
            }
        });
    }

    private void updateFirebaseAuthProfile(@NonNull String newUsername, @NonNull OnFinishedListener listener) {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            listener.onError("프로필 업데이트 중 오류가 발생했습니다.");
            return;
        }

        // [추가] 변경하려는 닉네임이 Auth 프로필의 닉네임과 이미 같으면, 불필요한 업데이트를 생략하고 바로 성공 처리
        if (newUsername.equals(user.getDisplayName())) {
            listener.onSuccess(newUsername);
            return;
        }

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(newUsername)
                .build();

        user.updateProfile(profileUpdates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "Firebase Auth profile updated successfully.");
                listener.onSuccess(newUsername);
            } else {
                Log.e(TAG, "Firebase Auth profile update failed.", task.getException());
                listener.onError("프로필 업데이트에 실패했습니다.");
            }
        });
    }
}
