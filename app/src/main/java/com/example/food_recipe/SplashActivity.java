package com.example.food_recipe;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {
    private static final long SPLASH_DELAY = 1000; // 1초 지연
    private boolean keepSplash = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // SplashScreen API 설치
        SplashScreen splash = SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);

        // 스플래시 유지 조건
        splash.setKeepOnScreenCondition(() -> keepSplash);

        // 일정 시간 후 자동 로그인 분기
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            keepSplash = false; // 스플래시 해제

            Intent intent;
            if (AutoLoginManager.isLoggedIn(this)) {
                // 자동 로그인 → 메인화면
                intent = new Intent(SplashActivity.this, MainActivity.class);
            } else {
                // 로그인 필요
                intent = new Intent(SplashActivity.this, LoginActivity.class);
            }
            startActivity(intent);
            finish(); // 스플래시 종료
        }, SPLASH_DELAY);
    }
}
