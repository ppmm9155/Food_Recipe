// FindIdModel.java
package com.example.food_recipe.findid;

import android.os.Handler;

public class FindIdModel implements FindIdContract.Model {

    @Override
    public void sendVerificationEmail(String email, Callback callback) {
        // 실제 API 통신 대신 2초 지연 후 결과 반환 (테스트용)
        new Handler().postDelayed(() -> {
            if (email.contains("@")) {
                callback.onSuccess();
            } else {
                callback.onError("올바른 이메일 주소를 입력하세요.");
            }
        }, 2000);
    }
}
