package com.example.food_recipe.login;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.food_recipe.findid.FindIdActivity;
import com.example.food_recipe.findps.FindPsActivity;
import com.example.food_recipe.join.JoinActivity;

import com.example.food_recipe.main.MainActivity;
import com.example.food_recipe.R;

import com.example.food_recipe.utils.AutoLoginManager;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import com.example.food_recipe.utils.SimpleWatcher;


public class LoginActivity extends AppCompatActivity implements LoginContract.View {

    private TextInputLayout tilEmail, tilPassword;
    private TextInputEditText etEmail, etPassword;
    private MaterialCheckBox cbAutoLogin;
    private Button btnLogin;

    private LoginContract.Presenter presenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Phase 3: Edge-to-Edge 모드 활성화
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        setContentView(R.layout.activity_login);

        // ===== 뷰 바인딩 =====
        tilEmail = findViewById(R.id.tilEmail);
        etEmail = findViewById(R.id.ETemail);
        tilPassword = findViewById(R.id.tilPassword);
        etPassword = findViewById(R.id.ETpassword);
        cbAutoLogin = findViewById(R.id.autoLoginCheckBox);
        btnLogin = findViewById(R.id.login_btn);
        View contentView = findViewById(R.id.login); // 콘텐츠를 담고 있는 부모 뷰

        // Phase 3: 충돌 방지 센서 부착
        ViewCompat.setOnApplyWindowInsetsListener(contentView, (v, windowInsets) -> {
            // 버그 수정: WindowInsetsCompat -> Insets 타입으로 변경
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            // 시스템 바(상태표시줄, 네비게이션바) 영역만큼 패딩 적용
            v.setPadding(v.getPaddingLeft(), insets.top, v.getPaddingRight(), insets.bottom);
            return WindowInsetsCompat.CONSUMED;
        });

        presenter = new LoginPresenter(this, new LoginModel());

        // ===== 버튼 이벤트 등록 =====
        btnLogin.setOnClickListener(v -> presenter.attemptLogin(
                text(etEmail),
                text(etPassword),
                cbAutoLogin != null && cbAutoLogin.isChecked()
        ));

        etEmail.addTextChangedListener(new SimpleWatcher(this::clearEmailError));
        etPassword.addTextChangedListener(new SimpleWatcher(this::clearPasswordError));

        findViewById(R.id.joinT).setOnClickListener(v ->
                startActivity(new Intent(this, JoinActivity.class)));

        findViewById(R.id.Tfind_id).setOnClickListener(v ->
                startActivity(new Intent(this, FindIdActivity.class)));

        findViewById(R.id.Tfind_password).setOnClickListener(v ->
                startActivity(new Intent(this, FindPsActivity.class)));
    }

    @Override
    protected void onDestroy() {
        if (presenter != null) {
            presenter.detachView();
        }
        super.onDestroy();
    }

    // ===== LoginContract.View 인터페이스 구현 =====

    @Override
    public void showEmailError(String msg) {
        tilEmail.setError(msg);
        etEmail.requestFocus();
    }

    @Override
    public void showPasswordError(String msg) {
        tilPassword.setError(msg);
        etPassword.requestFocus();
    }

    @Override
    public void showWrongPassword() {
        tilPassword.setError("비밀번호가 올바르지 않습니다.");
        tilPassword.setEndIconMode(TextInputLayout.END_ICON_NONE);
        etPassword.requestFocus();
    }

    @Override
    public void showAmbiguous() {
        tilEmail.setError("이메일 또는 비밀번호가 올바르지 않습니다.");
        etEmail.requestFocus();
    }

    @Override
    public void clearEmailError() {
        tilEmail.setError(null);
    }

    @Override
    public void clearPasswordError() {
        tilPassword.setError(null);
        tilPassword.setEndIconMode(TextInputLayout.END_ICON_PASSWORD_TOGGLE);
    }

    @Override
    public void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void setUiEnabled(boolean enabled) {
        btnLogin.setEnabled(enabled);
        etEmail.setEnabled(enabled);
        etPassword.setEnabled(enabled);
        cbAutoLogin.setEnabled(enabled);
        btnLogin.setAlpha(enabled ? 1f : 0.5f);
    }

    @Override
    public void navigateToHome() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    public void onLoginSuccess(boolean autoLoginChecked) {
        AutoLoginManager.setAutoLogin(this, autoLoginChecked);
        AutoLoginManager.clearForceReLoginOnce(this);
        Log.d("LoginFlow", "로그인 성공: auto=" + autoLoginChecked + ", force 플래그 해제됨");
        toast("로그인 성공");
        startActivity(new Intent(this, com.example.food_recipe.main.MainActivity.class));
        finish();
    }

    private String text(TextInputEditText et) {
        return et != null && et.getText() != null ? et.getText().toString() : "";
    }

}
