package com.example.food_recipe.mypage;

import androidx.annotation.NonNull;

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
 * [추가] 마이페이지 기능의 Model. 계정 탈퇴 등 Firestore 데이터 처리를 담당합니다.
 */
public class MyPageModel implements MyPageContract.Model {

    private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * [추가] 계정 탈퇴의 모든 과정을 하나의 트랜잭션처럼 처리합니다.
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

        // 1. 사용자 문서에서 usernameLower 값을 먼저 가져옵니다.
        userDocRef.get().addOnSuccessListener(userDocument -> {

            String usernameLower = userDocument.getString("usernameLower");

            // 2. 하위 컬렉션(favorites, ingredients) 삭제 작업을 준비합니다.
            Task<Void> deleteFavoritesTask = deleteSubCollection(userDocRef.collection("favorites"));
            Task<Void> deleteIngredientsTask = deleteSubCollection(userDocRef.collection("ingredients"));

            // 3. 모든 하위 컬렉션 삭제 작업이 완료된 후, 나머지 문서 삭제 및 인증 정보 삭제를 진행합니다.
            Tasks.whenAll(deleteFavoritesTask, deleteIngredientsTask).addOnSuccessListener(aVoid -> {

                WriteBatch batch = db.batch();

                // 3-1. usernames 문서 삭제 준비
                if (usernameLower != null && !usernameLower.isEmpty()) {
                    DocumentReference usernameDocRef = db.collection("usernames").document(usernameLower);
                    batch.delete(usernameDocRef);
                }

                // 3-2. users/{uid} 문서 삭제 준비
                batch.delete(userDocRef);

                // 3-3. 배치 작업 커밋
                batch.commit().addOnSuccessListener(aVoid2 -> {
                    // 4. 모든 Firestore 데이터 삭제 성공 시, 마지막으로 인증 정보를 삭제합니다.
                    deleteAuthData(user, listener);
                }).addOnFailureListener(e -> listener.onError("계정 정보 삭제 중 오류가 발생했습니다: " + e.getMessage()));

            }).addOnFailureListener(e -> listener.onError("사용자 데이터(즐겨찾기/재료) 삭제 중 오류가 발생했습니다: " + e.getMessage()));

        }).addOnFailureListener(e -> listener.onError("사용자 정보를 불러오는 데 실패했습니다: " + e.getMessage()));
    }

    /**
     * [추가] 지정된 하위 컬렉션의 모든 문서를 삭제하는 작업을 반환합니다.
     * @param collection 삭제할 컬렉션 참조
     * @return 모든 문서 삭제가 완료되었을 때 성공하는 Task
     */
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

    /**
     * [추가] Firebase Authentication에서 현재 사용자를 삭제합니다.
     * @param user 삭제할 사용자 객체
     * @param listener 결과를 전달할 콜백 리스너
     */
    private void deleteAuthData(FirebaseUser user, OnFinishedListener listener) {
        user.delete().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                listener.onSuccess();
            } else {
                // 재인증이 필요한 경우 등 복잡한 오류 처리를 할 수 있습니다.
                listener.onError("계정 삭제에 실패했습니다. 다시 로그인 후 시도해주세요.");
            }
        });
    }
}
