// FindPsModel.java
package com.example.food_recipe.findps;

import com.google.firebase.auth.FirebaseAuth;

public class FindPsModel implements FindPsContract.Model {
    private final FirebaseAuth auth;

    public FindPsModel() {
        this.auth = FirebaseAuth.getInstance();
    }

    @Override
    public void sendPasswordResetEmail(String email, Callback callback) {
        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess();
                    } else {
                        callback.onFailure();
                    }
                });
    }
}
