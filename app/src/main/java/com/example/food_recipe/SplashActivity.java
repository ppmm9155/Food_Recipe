package com.example.food_recipe;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;  // SplashScreen API 임포트

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {
    // Splash 화면이 계속 보일지 여부를 제어하는 변수
    private boolean keepSplash = true;
    // 스플래시를 유지할 시간 (밀리초)
    private static final long SPLASH_DELAY = 1000;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // SplashScreen API 설치: 시스템이 Splash Theme 를 적용하고,
        // 여기서 화면 전환 시점 제어 가능하게 함
        SplashScreen splash = SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);

        // Splash 화면을 유지할 조건 설정.
        // keepSplash 가 true 인 동안은 Splash 화면 유지됨
        splash.setKeepOnScreenCondition(() -> keepSplash);



        // 일정 시간 후 작업 실행: SPLASH_DELAY 후 Runnable 실행
        new Handler().postDelayed(() -> {
            keepSplash = false;  // 이제 Splash 유지 조건 해제

            // 로그인 상태 체크
            boolean userLoggedIn = checkUserLoginStatus();
            Intent intent;
            if (userLoggedIn) {
                intent = new Intent(SplashActivity.this, MainActivity.class);
            } else {
                intent = new Intent(SplashActivity.this, LoginActivity.class);
            }
            startActivity(intent);
            finish();  // SplashActivity 종료
        }, SPLASH_DELAY);

    }
    private boolean checkUserLoginStatus() {
        // FirebaseAuth 인스턴스로부터 현재 로그인된 유저 검사
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        // 유저 객체가 null 이 아니면 로그인 상태 있음 → true 반환
        // null 이면 로그인 상태 없음 → false 반환
        return currentUser != null;
    }

}
