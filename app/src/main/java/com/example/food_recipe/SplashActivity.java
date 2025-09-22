// File: app/src/main/java/com/example/food_recipe/SplashActivity.java
package com.example.food_recipe;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.example.food_recipe.login.LoginActivity;
import com.example.food_recipe.main.MainActivity;
import com.example.food_recipe.utils.AutoLoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * ✅ ZIP 구조(루트 패키지: com.example.food_recipe)에 맞춘 스플래시 전체 코드
 * 동작 원리
 * 1) AutoLoginManager.isLoggedIn(this) 결과로 1차 분기
 *    - 내부 조건: FirebaseUser != null && autoLogin == true && !forceReLoginOnce
 * 2) 메인 진입 조건이면 user.reload()로 세션 최신화 후 최종 결정
 *    - 실패/무효 시 안전하게 로그인 화면으로
 * 3) 로그인/메인으로 이동 시 finish()로 스플래시 종료 (백스택 잔존 방지)
 */
@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // Android 12+ 스플래시 API 적용 (레이아웃 없이도 표시됨)
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);

        // 1차 분기: 로컬 플래그 + Firebase 세션 상태
        final boolean shouldGoMain = AutoLoginManager.isLoggedIn(this);
        Log.d("SplashFlow", "초기 분기: shouldGoMain=" + shouldGoMain);

        if (shouldGoMain) {
            // 세션 최신화 (네트워크 실패/토큰 무효 등 대비)
            final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            // 방어 코드: user가 null이면 바로 로그인으로
            if (user == null) {
                Log.d("SplashFlow", "FirebaseUser == null → 로그인 화면 이동");
                goLogin();
                return;
            }

            user.reload().addOnCompleteListener(task -> {
                // reload 이후에도 조건을 다시 점검 (forceReLoginOnce가 켜졌을 수 있음)
                boolean ok = task.isSuccessful()
                        && FirebaseAuth.getInstance().getCurrentUser() != null
                        && AutoLoginManager.isLoggedIn(this);

                Log.d("SplashFlow", "user.reload() 결과: ok=" + ok);

                if (ok) {
                    goMain();
                } else {
                    goLogin();
                }
            });

        } else {
            // 강제 재로그인(1회) 또는 자동로그인 미사용/세션 없음 → 로그인으로
            Log.d("SplashFlow", "자동로그인 조건 불충족 → 로그인 화면 이동");
            goLogin();
        }
    }

    /** 메인으로 이동 */
    private void goMain() {
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    /** 로그인으로 이동 */
    private void goLogin() {
        Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
