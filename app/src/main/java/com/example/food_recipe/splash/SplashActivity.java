// File: app/src/main/java/com/example/food_recipe/splash/SplashActivity.java
package com.example.food_recipe.splash;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.example.food_recipe.login.LoginActivity;
import com.example.food_recipe.main.MainActivity;


/**
 * ✅ SplashActivity (MVP의 View 역할)
 *
 * 이 클래스는 "화면"만 담당합니다.
 * - 화면 전환(Main/Login으로 이동)
 * - 간단한 로딩/로그 출력
 *
 * 핵심 판단(자동로그인 여부, Firebase 세션 확인 등)은
 * Presenter + Model이 수행합니다.
 * → 그래서 여기서는 복잡한 if/else나 Firebase 코드를 직접 쓰지 않습니다.
 *
 * [MVP 한 줄 요약]
 * - Model: 데이터/비즈니스 로직 (예: 로그인 상태 판단)
 * - View : 화면 그리기/전환 (Activity, XML)
 * - Presenter: Model과 View를 연결(중간 관리자)
 */
@SuppressLint("CustomSplashScreen") // Android 12+ 스플래시 화면을 커스텀 레이아웃 없이 쓰기 위한 안내
public class SplashActivity extends AppCompatActivity implements SplashContract.View {

    // Presenter는 View와 Model을 이어주는 "중간 관리자"입니다.
    // View는 Presenter에게 "시작해!"라고 부탁만 하고, 결과를 콜백으로 전달받아 화면을 바꿉니다.
    private SplashContract.Presenter presenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // Android 12(API 31)+ 공식 스플래시 API.
        // 앱 실행 직후 로고/아이콘이 깔끔하게 보이도록 시스템이 처리합니다.
        SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);

        // 1) Presenter 생성 시 Model도 함께 주입합니다.
        //    - new SplashModel(getApplicationContext())
        //      : Model은 Context가 필요한데, Activity 컨텍스트 대신
        //        Application 컨텍스트를 주어 메모리 누수(액티비티 참조 유지) 위험을 줄입니다.
        presenter = new SplashPresenter(new SplashModel(getApplicationContext()));

        // 2) View 연결: Presenter가 이 화면(SplashActivity)에 접근할 수 있게 합니다.
        presenter.attach(this);

        // 3) 로직 시작: Presenter가 내부에서 Model에 "메인으로 갈 수 있나?"를 물어보고,
        //    판단 결과에 맞게 아래의 navigateToMain()/navigateToLogin()을 호출하게 됩니다.
        presenter.start();
    }

    @Override
    protected void onDestroy() {
        // Activity가 사라질 때 Presenter와의 연결을 끊어줍니다.
        // 이렇게 해야 Presenter가 죽은 화면을 붙잡고 있지 않아 메모리 누수가 나지 않습니다.
        if (presenter != null) presenter.detach();
        super.onDestroy();
    }

    // ======================
    // ⬇️ View 인터페이스 구현부
    // Presenter가 결과에 따라 아래 메서드를 호출합니다.
    // ======================

    @Override
    public void navigateToMain() {
        // 메인 화면으로 이동하고, 스플래시는 닫습니다(뒤로가기 눌러도 안 돌아오게).
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(intent);
        finish(); // 스플래시는 스택에서 제거
    }

    @Override
    public void navigateToLogin() {
        // 로그인 화면으로 이동하고, 스플래시는 닫습니다.
        Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void showLoading(boolean show) {
        // 스플래시는 시스템이 기본 로딩 UI를 보여주므로,
        // 여기서는 단순히 로그만 남깁니다.
        // (만약 커스텀 로딩 UI가 필요하면 ProgressBar 등을 추가하면 됩니다.)
        Log.d("SplashActivity", "showLoading=" + show);
    }

    @Override
    public void log(String msg) {
        // 디버깅 편의를 위한 공용 로그 메서드
        Log.d("SplashActivity", msg);
    }
}
