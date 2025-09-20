package com.example.food_recipe.login;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.food_recipe.FindIdActivity;
import com.example.food_recipe.FindPsActivity;
import com.example.food_recipe.JoinActivity;
import com.example.food_recipe.MainActivity;
import com.example.food_recipe.R;
import com.example.food_recipe.utils.AutoLoginManager;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

// ✅ View 계층 (MVP의 V)
// - 화면(UI)을 담당하는 Activity
// - 사용자의 입력/버튼 클릭을 Presenter에게 전달
// - Presenter가 알려준 결과를 화면에 반영
public class LoginActivity extends AppCompatActivity implements LoginContract.View {

    // UI 컴포넌트
    private TextInputLayout tilEmail, tilPassword;
    private TextInputEditText etEmail, etPassword;
    private MaterialCheckBox cbAutoLogin;
    private Button btnLogin;

    // Presenter 참조 (중재자 역할)
    private LoginContract.Presenter presenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login); // 로그인 화면 XML과 연결

        // ===== 뷰 바인딩 (XML → Java 객체 연결) =====
        tilEmail = findViewById(R.id.tilEmail);
        etEmail = findViewById(R.id.ETemail);
        tilPassword = findViewById(R.id.tilPassword);
        etPassword = findViewById(R.id.ETpassword);
        cbAutoLogin = findViewById(R.id.autoLoginCheckBox);
        btnLogin = findViewById(R.id.login_btn);

        // Presenter 생성 (View=this, Model=LoginModel)
        presenter = new LoginPresenter(this, new LoginModel());

        // ===== 버튼 이벤트 등록 =====
        // 로그인 버튼 클릭 시 → Presenter에게 로그인 시도 요청
        btnLogin.setOnClickListener(v -> presenter.attemptLogin(
                text(etEmail),
                text(etPassword),
                cbAutoLogin != null && cbAutoLogin.isChecked()
        ));

        // 입력 중 에러 해제 (UX 개선)
        etEmail.addTextChangedListener(new SimpleWatcher(this::clearEmailError));
        etPassword.addTextChangedListener(new SimpleWatcher(this::clearPasswordError));

        // 회원가입 화면 이동
        findViewById(R.id.joinT).setOnClickListener(v ->
                startActivity(new Intent(this, JoinActivity.class)));

        // 아이디 찾기 화면 이동
        findViewById(R.id.Tfind_id).setOnClickListener(v ->
                startActivity(new Intent(this, FindIdActivity.class)));

        // 비밀번호 찾기 화면 이동
        findViewById(R.id.Tfind_password).setOnClickListener(v ->
                startActivity(new Intent(this, FindPsActivity.class)));
    }

    // ===== LoginContract.View 인터페이스 구현 =====

    @Override
    public void showEmailError(String msg) {
        tilEmail.setError(msg);
        etEmail.requestFocus(); // 이메일 입력창으로 포커스 이동
    }

    @Override
    public void showPasswordError(String msg) {
        tilPassword.setError(msg);
        etPassword.requestFocus();
    }

    @Override
    public void showWrongPassword() {
        tilPassword.setError("비밀번호가 올바르지 않습니다.");
        tilPassword.setEndIconMode(TextInputLayout.END_ICON_NONE); // 비밀번호 표시 아이콘 제거
        etPassword.requestFocus();
    }

    @Override
    public void showAmbiguous() {
        // 모호한 경우(사용자 없음? 비밀번호 틀림?) → 공통 메시지 표시
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
        tilPassword.setEndIconMode(TextInputLayout.END_ICON_PASSWORD_TOGGLE); // 다시 눈 아이콘 표시
    }

    @Override
    public void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void setUiEnabled(boolean enabled) {
        // UI 활성화/비활성화 (로그인 진행 중이면 버튼 회색 처리)
        btnLogin.setEnabled(enabled);
        etEmail.setEnabled(enabled);
        etPassword.setEnabled(enabled);
        cbAutoLogin.setEnabled(enabled);
        btnLogin.setAlpha(enabled ? 1f : 0.5f);
    }

    @Override
    public void navigateToHome() {
        // 로그인 성공 → 메인 화면으로 이동
        startActivity(new Intent(this, MainActivity.class));
        finish(); // 뒤로가기 시 로그인화면 안뜨게 종료
    }

    @Override
    public void onLoginSuccess(boolean autoLoginChecked) {
        // ✅ Presenter는 "성공했다"만 알려줌
        // ✅ 실제 AutoLoginManager 호출은 View(Activity)가 맡음
        AutoLoginManager.setAutoLogin(this, autoLoginChecked);
        toast("로그인 성공");
        navigateToHome();
    }

    // ===== 유틸리티 메서드 =====
    private String text(TextInputEditText et) {
        return et != null && et.getText() != null ? et.getText().toString() : "";
    }

    // 내부 TextWatcher → 입력 값 변경 시 실행
    private static class SimpleWatcher implements android.text.TextWatcher {
        private final Runnable onChange;
        SimpleWatcher(Runnable onChange) { this.onChange = onChange; }
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) { onChange.run(); }
        @Override public void afterTextChanged(android.text.Editable s) {}
    }
}
