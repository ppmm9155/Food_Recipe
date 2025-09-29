package com.example.food_recipe.login;

import android.content.Context;
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

import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import com.example.food_recipe.utils.SimpleWatcher;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.textview.MaterialTextView;


public class LoginActivity extends AppCompatActivity implements LoginContract.View {

    // 👉 추가 필드
    private Button btnGoogleLogin;                     // @id/login_btn_googleLogin
    private static final int RC_GOOGLE_SIGN_IN = 9001; // 요청 코드
    private GoogleSignInClient googleClient;

    MaterialTextView guestLogin;
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
        btnGoogleLogin = findViewById(R.id.login_btn_googleLogin); // 👉 추가
        //게스트 로그인
        guestLogin = findViewById(R.id.login_guest);


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

        // 👉 Google Sign-In 옵션 (ID Token + 이메일 요청)
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleClient = GoogleSignIn.getClient(this, gso);

        // 👉 구글 로그인 버튼 클릭 → 구글 로그인 플로우 시작
        btnGoogleLogin.setOnClickListener(v -> {
            Intent signInIntent = googleClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN);
        });

        //게스트 로그인 이벤트 등록
        guestLogin.setOnClickListener(v -> {
            // Presenter에게 게스트 로그인 요청 전달
            presenter.attemptGusetLogin(cbAutoLogin.isChecked());
        });

        // 찾기/회원가입 이동
        findViewById(R.id.joinT).setOnClickListener(v ->
                startActivity(new Intent(this, JoinActivity.class)));

        findViewById(R.id.Tfind_id).setOnClickListener(v ->
                startActivity(new Intent(this, FindIdActivity.class)));

        findViewById(R.id.Tfind_password).setOnClickListener(v ->
                startActivity(new Intent(this, FindPsActivity.class)));

    }

    // 👉 구글 로그인 결과 Presenter로 위임(+ 자동로그인 체크 상태 전달)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_GOOGLE_SIGN_IN && data != null) {
            boolean autoChecked = cbAutoLogin != null && cbAutoLogin.isChecked();
            presenter.handleGoogleLoginResult(data, autoChecked);
        }else{
            toast("Google 로그인 취소됨");
        }
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

        // 👉 추가: 구글 버튼도 함께 토글
        if (btnGoogleLogin != null) {
            btnGoogleLogin.setEnabled(enabled);
            btnGoogleLogin.setAlpha(enabled ? 1f : 0.5f);
        }
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
        navigateToHome();
        finish();
    }

        @Override
        public void onGuestLoginSuccess(boolean autoLoginChecked) {
            // 게스트 로그인 성공이라면 따로 구분하고 싶다면 여기서 처리 가능
            toast("게스트 로그인 성공");
            navigateToHome();
        }

    private String text(TextInputEditText et) {
        return et != null && et.getText() != null ? et.getText().toString() : "";
    }

    // (새로추가됨) LoginContract.View 인터페이스의 getContext() 메소드 구현
    @Override
    public Context getContext() {
        return this; // Activity Context 반환
    }
}
