package com.example.food_recipe.home;

import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class HomeModel implements HomeContract.Model {

    @Override
    public void fetchUsername(UsernameCallback cb) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            cb.onSuccess(null); // 로그인 안 됨 → 기본 문구
            return;
        }
        String uid = user.getUid();

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener((@Nullable DocumentSnapshot doc) -> {
                    String username = null;
                    if (doc != null && doc.exists()) {
                        username = doc.getString("username");
                        if (username != null) {
                            username = username.trim();
                            if (username.isEmpty()) username = null;
                        }
                    }
                    cb.onSuccess(username);
                })
                .addOnFailureListener(cb::onError);
    }
}
