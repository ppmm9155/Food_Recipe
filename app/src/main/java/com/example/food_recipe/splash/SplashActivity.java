
package com.example.food_recipe.splash;

import android.annotation.SuppressLint;
import android.content.Context;
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
 * [기존 주석 유지]
 */
@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity implements SplashContract.View {

    private static final String TAG = "SplashActivity";
    private SplashContract.Presenter presenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);

        // [수정] 변경된 Presenter 생성 방식을 적용합니다. Context 주입이 필요 없습니다.
        presenter = new SplashPresenter();
        presenter.attachView(this);

        presenter.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.detachView();
    }

    // ===== SplashContract.View 구현부 (기존 코드 유지) =====

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
        log("showLoading: " + show);
    }

    @Override
    public void log(String msg) {
        Log.d(TAG, msg);
    }

    /**
     * [추가] BaseContract.View 인터페이스의 요구사항을 구현합니다.
     */
    @Override
    public Context getContext() {
        return this;
    }
}
