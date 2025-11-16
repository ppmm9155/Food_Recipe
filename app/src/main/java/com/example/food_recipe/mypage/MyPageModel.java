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
import com.google.firebase.auth.UserInfo;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;

/**
 * [기존 주석 유지] 마이페이지 기능의 Model. 계정 탈퇴 등 Firestore 데이터 처리를 담당합니다.
 * [변경] 로그아웃과 계정 탈퇴의 성공 콜백을 분리하여 호출합니다.
 */
public class MyPageModel implements MyPageContract.Model {

    private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final Context appContext;

    public MyPageModel(Context context) {
        this.appContext = context.getApplicationContext();
    }

    /**
     * [수정] 현재 로그인한 사용자가 구글 계정인지 확인합니다.
     * FirebaseUser 객체 대신, SharedPreferences에 저장된 로그인 제공자 정보를 확인합니다.
     * 이 방법은 Firebase 계정이 삭제된 후에도 로그인 방식을 확인할 수 있어 더 안정적입니다.
     */
    @Override
    public boolean isGoogleUser() {
        return AutoLoginManager.PROVIDER_GOOGLE.equals(AutoLoginManager.getCurrentLoginProvider(appContext));
    }

    @Override
    public void logout(@NonNull OnFinishedListener listener) {
        try {
            boolean isGoogle = isGoogleUser();
            // AutoLoginManager.logout()은 Firebase 세션 종료, 자동로그인 해제 등을 처리합니다.
            AutoLoginManager.logout(appContext);

            if (isGoogle) {
                // 기기에 캐시된 구글 계정 정보까지 완전히 로그아웃시킵니다.
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(appContext.getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build();
                GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(appContext, gso);
                googleSignInClient.signOut().addOnCompleteListener(task -> {
                    // 구글 로그아웃의 성공/실패 여부와 관계없이, 앱의 로그아웃 절차는 성공한 것으로 간주합니다.
                    listener.onLogoutSuccess();
                });
            } else {
                listener.onLogoutSuccess(); // 구글 유저가 아니면 바로 성공 콜백
            }
        } catch (Exception e) {
            listener.onError("로그아웃 중 오류가 발생했습니다: " + e.getMessage());
        }
    }


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
                listener.onDeleteAccountSuccess(); // [변경] 계정 탈퇴 성공 콜백 호출
            } else {
                listener.onError("계정 삭제에 실패했습니다. 다시 로그인 후 시도해주세요.");
            }
        });
    }
}
