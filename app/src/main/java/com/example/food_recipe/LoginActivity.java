package com.example.food_recipe;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.GoogleAuthProvider;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    // ⚠️ 안전모드: true로 바꾸면 fetch 결과 []일 때 "존재하지 않는 사용자"로 단정합니다.
    // 권장 기본값은 false (오판 방지)
    private static final boolean TRUST_FETCH_FOR_USER_NOT_FOUND = false;

    // UX 문구
    private static final String MSG_LOGIN_SUCCESS   = "로그인 성공";
    private static final String MSG_INVALID_EMAIL   = "이메일 형식이 올바르지 않습니다.";
    private static final String MSG_USER_NOT_FOUND  = "존재하지 않는 사용자입니다.";
    private static final String MSG_WRONG_PASSWORD  = "비밀번호가 올바르지 않습니다.";
    private static final String MSG_AMBIGUOUS       = "이메일 또는 비밀번호가 올바르지 않습니다.";
    private static final String MSG_NET_ERROR       = "네트워크/처리 중 오류가 발생했습니다. 다시 시도해 주세요.";

    private FirebaseAuth mAuth;

    private TextInputLayout tilEmail, tilPassword;
    private TextInputEditText etEmail, etPassword;
    private MaterialCheckBox cbAutoLogin;
    private Button btnLogin;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        mAuth.setLanguageCode("ko");

        // 뷰 바인딩
        tilEmail = findViewById(R.id.tilEmail);
        etEmail = findViewById(R.id.ETemail);
        tilPassword = findViewById(R.id.tilPassword);
        etPassword = findViewById(R.id.ETpassword);
        cbAutoLogin = findViewById(R.id.autoLoginCheckBox);
        btnLogin = findViewById(R.id.login_btn);


        btnLogin.setOnClickListener(v -> attemptLogin());

        // 입력 중 에러 해제 + 비번 눈 아이콘 복원
        etEmail.addTextChangedListener(new SimpleWatcher(this::clearEmailError));
        etPassword.addTextChangedListener(new SimpleWatcher(this::clearPasswordError));

        // 회원가입 이동
        findViewById(R.id.joinT).setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, JoinActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.Tfind_id).setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, FindIdActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.Tfind_password).setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, FindPsActivity.class);
            startActivity(intent);
        });
    }
    /** 로그인 시도 */
    private void attemptLogin() {
        String email = normalizeEmail(text(etEmail));
        String password = text(etPassword);

        // 1) 로컬 검증
        if (email.isEmpty()) { showEmailError("이메일을 입력해주세요."); return; }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) { showEmailError(MSG_INVALID_EMAIL); return; }
        if (password.isEmpty()) { showPasswordError("비밀번호를 입력해주세요."); return; }

        setUiEnabled(false);

        // 2) Firebase 로그인
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // 자동 로그인 저장
                        AutoLoginManager.setAutoLogin(this, cbAutoLogin != null && cbAutoLogin.isChecked());
                        toast(MSG_LOGIN_SUCCESS);
                        startActivity(new Intent(this, MainActivity.class));
                        finish();
                        return;
                    }

                    Exception e = task.getException();
                    String code = (e instanceof FirebaseAuthException)
                            ? ((FirebaseAuthException) e).getErrorCode()
                            : null;

                    Log.e(TAG, "Login failed" + (code != null ? " code=" + code : ""), e);

                    // 1차: 명확한 코드 즉시 처리
                    if ("ERROR_USER_NOT_FOUND".equals(code)) { showEmailError(MSG_USER_NOT_FOUND); setUiEnabled(true); return; }
                    if ("ERROR_INVALID_EMAIL".equals(code))   { showEmailError(MSG_INVALID_EMAIL);   setUiEnabled(true); return; }
                    if ("ERROR_WRONG_PASSWORD".equals(code))  { showWrongPassword();                 setUiEnabled(true); return; }

                    // 2차: 모호한 자격증명 → fetch로 보조 판정
                    if (isAmbiguous(code, e)) {
                        refineAmbiguousWithFetch(email);
                        return; // refine 내에서 UI 제어
                    }

                    // 기타 공통
                    toast("로그인 실패" + (code != null ? " (" + code + ")" : ""));
                    setUiEnabled(true);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Login failure (network or unexpected)", e);
                    toast(MSG_NET_ERROR);
                    setUiEnabled(true);
                });
    }

    /** 모호 코드 판별 */
    private boolean isAmbiguous(String code, Exception e) {
        return (e instanceof FirebaseAuthInvalidCredentialsException)
                || "ERROR_INVALID_CREDENTIAL".equals(code)
                || "ERROR_INVALID_LOGIN_CREDENTIALS".equals(code)
                || "ERROR_USER_TOKEN_EXPIRED".equals(code)
                || "ERROR_INVALID_USER_TOKEN".equals(code);
    }

    /**
     * 모호 오류 보조 판정
     * - googleOnly  : Google로 안내
     * - supportsPwd : 비밀번호 오류 확정
     * - methods == []:
     *      · 기본(안전)  → 모호 메시지로 처리(오판 방지)
     *      · 신뢰(옵션)  → 사용자 없음으로 단정
     */
    private void refineAmbiguousWithFetch(String email) {
        mAuth.fetchSignInMethodsForEmail(email)
                .addOnCompleteListener(task -> {
                    setUiEnabled(true);

                    if (!task.isSuccessful() || task.getResult() == null) {
                        Log.w(TAG, "fetchSignInMethods failed or null result", task.getException());
                        showAmbiguous();
                        return;
                    }

                    List<String> methods = task.getResult().getSignInMethods();
                    Log.d(TAG, "methods for " + email + " = " + methods);

                    boolean hasAny = methods != null && !methods.isEmpty();
                    if (!hasAny) {
                        if (TRUST_FETCH_FOR_USER_NOT_FOUND) {
                            showEmailError(MSG_USER_NOT_FOUND);
                        } else {
                            // ✅ 안전모드: 오판 방지(특히 비번 오입력인데 [] 오는 케이스)
                            showAmbiguous();
                        }
                        return;
                    }

                    boolean supportsPassword = methods.contains(EmailAuthProvider.EMAIL_PASSWORD_SIGN_IN_METHOD);
                    boolean googleOnly = methods.contains(GoogleAuthProvider.PROVIDER_ID) && !supportsPassword;

                    if (googleOnly) {
                        toast("이 계정은 Google 로그인을 사용해야 합니다.");
                        return;
                    }
                    if (supportsPassword) {
                        showWrongPassword();
                        return;
                    }
                    showAmbiguous();
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "refineAmbiguousWithFetch error", e);
                    setUiEnabled(true);
                    showAmbiguous();
                });
    }

    // ===== 에러 출력/해제 =====

    private void showEmailError(String msg) {
        if (tilEmail != null) tilEmail.setError(msg);
        if (etEmail != null) etEmail.requestFocus();
    }

    private void showPasswordError(String msg) {
        if (tilPassword != null) tilPassword.setError(msg);
        if (etPassword != null) etPassword.requestFocus();
    }

    /** 비밀번호 틀림: 에러 + 눈 아이콘 숨김 */
    private void showWrongPassword() {
        if (tilPassword != null) {
            tilPassword.setError(MSG_WRONG_PASSWORD);
            tilPassword.setEndIconMode(TextInputLayout.END_ICON_NONE);
        }
        if (etPassword != null) {
            etPassword.requestFocus();
            if (etPassword.getText() != null) {
                etPassword.setSelection(etPassword.getText().length());
            }
        }
    }

    /** 모호 메시지(오판 방지 기본값) */
    private void showAmbiguous() {
        if (tilEmail != null) tilEmail.setError(MSG_AMBIGUOUS);
        if (etEmail != null) etEmail.requestFocus();
    }

    private void clearEmailError() {
        if (tilEmail != null && tilEmail.getError() != null) {
            tilEmail.setError(null);
        }
    }

    private void clearPasswordError() {
        if (tilPassword != null && tilPassword.getError() != null) {
            tilPassword.setError(null);
            tilPassword.setEndIconMode(TextInputLayout.END_ICON_PASSWORD_TOGGLE);
        }
    }

    // ===== 유틸 =====

    private String text(TextInputEditText et) {
        return et != null && et.getText() != null ? et.getText().toString() : "";
    }

    /** 이메일 정규화: trim + 소문자 + NFC */
    private String normalizeEmail(String raw) {
        String s = raw == null ? "" : raw;
        s = Normalizer.normalize(s, Normalizer.Form.NFC);
        s = s.trim();
        return s.toLowerCase(Locale.ROOT);
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void setUiEnabled(boolean enabled) {
        if (btnLogin != null) {
            btnLogin.setEnabled(enabled);
            btnLogin.setAlpha(enabled ? 1f : 0.5f);
        }
        if (etEmail != null) etEmail.setEnabled(enabled);
        if (etPassword != null) etPassword.setEnabled(enabled);
        if (cbAutoLogin != null) cbAutoLogin.setEnabled(enabled);
    }

    // 간단 TextWatcher
    private static class SimpleWatcher implements android.text.TextWatcher {
        private final Runnable onChange;
        SimpleWatcher(Runnable onChange) { this.onChange = onChange; }
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) { onChange.run(); }
        @Override public void afterTextChanged(android.text.Editable s) {}
    }
}
