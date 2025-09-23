// 파일: app/src/main/java/com/example/food_recipe/splash/SplashModel.java
package com.example.food_recipe.splash;

import android.content.Context;
import android.util.Log;

import com.example.food_recipe.utils.AutoLoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * SplashModel
 * - 자동로그인 플래그 + Firebase 세션 + 1회 강제 재로그인 플래그를 종합 판단
 * - 세션이 유효하다고 판단되면 FirebaseUser.reload()로 최종 검증
 */
public class SplashModel implements SplashContract.Model {

    private final Context appContext; // 메모리릭 방지용 Application Context만 보관

    public SplashModel(Context appContext) {
        this.appContext = appContext.getApplicationContext();
    }

    @Override
    public void canProceedToMain(Callback cb) {
        // 1차 로컬 판단
        final boolean shouldGoMain = AutoLoginManager.isLoggedIn(appContext);
        Log.d("SplashModel", "1차분기 shouldGoMain=" + shouldGoMain);

        if (!shouldGoMain) {
            // 자동로그인 OFF / 세션없음 / 강제재로그인1회
            cb.onResult(false);
            return;
        }

        // 2차 서버 세션 최신화
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            cb.onResult(false);
            return;
        }

        user.reload().addOnCompleteListener(task -> {
            boolean ok = task.isSuccessful()
                    && FirebaseAuth.getInstance().getCurrentUser() != null
                    && AutoLoginManager.isLoggedIn(appContext);
            Log.d("SplashModel", "reload() ok=" + ok);
            cb.onResult(ok);
        });
    }
}
