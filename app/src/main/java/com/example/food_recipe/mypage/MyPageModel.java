package com.example.food_recipe.mypage;

import android.content.Context;
import androidx.annotation.NonNull;

import com.example.food_recipe.R;
import com.example.food_recipe.utils.AutoLoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;

/**
 * [기존 주석 유지] 마이페이지 기능의 Model. 계정 탈퇴 등 Firestore 데이터 처리를 담당합니다.
 */
public class MyPageModel implements MyPageContract.Model {

    private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final Context appContext; // [추가] Google 로그아웃에 필요

    // [추가] Presenter로부터 Context를 받기 위한 생성자
    public MyPageModel(Context context) {
        this.appContext = context.getApplicationContext();
    }

    /**
     * [추가] 완전한 로그아웃 로직 (Firebase + Google SDK)
     */
    @Override
    public void logout(@NonNull OnFinishedListener listener) {
        try {
            String loginProvider = AutoLoginManager.getCurrentLoginProvider(appContext);

            // 1. Firebase Auth 로그아웃 및 로컬 플래그 제거 (모든 제공자 공통)
            AutoLoginManager.logout(appContext);

            // 2. Google 제공자의 경우, GoogleSignInClient에서도 로그아웃 처리
            if (AutoLoginManager.PROVIDER_GOOGLE.equals(loginProvider)) {
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(appContext.getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build();
                GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(appContext, gso);
                googleSignInClient.signOut().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        listener.onSuccess();
                    } else {
                        // Google 로그아웃 실패는 치명적이지 않으므로, 오류 메시지를 로그에만 남기고 성공으로 처리할 수도 있습니다.
                        // 하지만 여기서는 명확한 피드백을 위해 에러로 처리합니다.
                        listener.onError("구글 계정 로그아웃에 실패했습니다.");
                    }
                });
            } else {
                // 구글 외 다른 제공자는 추가 작업 없이 성공 처리
                listener.onSuccess();
            }
        } catch (Exception e) {
            listener.onError("로그아웃 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * [기존 주석 유지] 계정 탈퇴의 모든 과정을 하나의 트랜잭션처럼 처리합니다.
     */
    @Override
    public void deleteAccount(@NonNull OnFinishedListener listener) {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            listener.onError("로그인 정보가 없습니다. 다시 로그인해주세요.");
            return;
        }

        String uid = user.getUid();
        DocumentReference userDocRef = db.collection("users").document(uid);

        userDocRef.get().addOnSuccessListener(userDocument -> {
            String usernameLower = userDocument.getString("usernameLower");
            Task<Void> deleteFavoritesTask = deleteSubCollection(userDocRef.collection("favorites"));
            Task<Void> deleteIngredientsTask = deleteSubCollection(userDocRef.collection("ingredients"));

            Tasks.whenAll(deleteFavoritesTask, deleteIngredientsTask).addOnSuccessListener(aVoid -> {
                WriteBatch batch = db.batch();
                if (usernameLower != null && !usernameLower.isEmpty()) {
                    DocumentReference usernameDocRef = db.collection("usernames").document(usernameLower);
                    batch.delete(usernameDocRef);
                }
                batch.delete(userDocRef);
                batch.commit().addOnSuccessListener(aVoid2 -> {
                    deleteAuthData(user, listener);
                }).addOnFailureListener(e -> listener.onError("계정 정보 삭제 중 오류가 발생했습니다: " + e.getMessage()));
            }).addOnFailureListener(e -> listener.onError("사용자 데이터(즐겨찾기/재료) 삭제 중 오류가 발생했습니다: " + e.getMessage()));
        }).addOnFailureListener(e -> listener.onError("사용자 정보를 불러오는 데 실패했습니다: " + e.getMessage()));
    }

    private Task<Void> deleteSubCollection(CollectionReference collection) {
        return collection.get().continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }
            List<Task<Void>> tasks = new ArrayList<>();
            for (QueryDocumentSnapshot doc : task.getResult()) {
                tasks.add(doc.getReference().delete());
            }
            return Tasks.whenAll(tasks);
        });
    }

    private void deleteAuthData(FirebaseUser user, OnFinishedListener listener) {
        user.delete().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                listener.onSuccess();
            } else {
                listener.onError("계정 삭제에 실패했습니다. 다시 로그인 후 시도해주세요.");
            }
        });
    }
}
