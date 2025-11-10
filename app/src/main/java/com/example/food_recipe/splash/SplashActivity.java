
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
 *  스플래시 화면 (View)
 * [변경] MVP 패턴을 적용하여 Presenter에게 모든 로직 처리를 위임합니다.
 */
@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity implements SplashContract.View {

    private static final String TAG = "SplashActivity";
    private SplashContract.Presenter presenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);

        // [변경] Model과 Presenter를 생성하고 View를 연결합니다.
        SplashContract.Model model = new SplashModel(this);
        presenter = new SplashPresenter(model);
        presenter.attachView(this);

        // [변경] Presenter에게 시작을 알립니다.
        presenter.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // [추가] Activity가 파괴될 때 Presenter와의 연결을 끊습니다.
        presenter.detachView();
    }

    // ===== SplashContract.View 구현부 =====

    @Override
    public void navigateToMain() {
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void navigateToLogin() {
        Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void showLoading(boolean show) {
        // 스플래시 화면 자체는 로딩 상태를 시각적으로 표현할 필요가 없으므로 로그만 남깁니다.
        log("showLoading: " + show);
    }

    @Override
    public void log(String msg) {
        Log.d(TAG, msg);
    }
}
