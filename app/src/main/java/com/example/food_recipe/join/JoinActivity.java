package com.example.food_recipe.join;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.food_recipe.R;
import com.example.food_recipe.login.LoginActivity;
import com.example.food_recipe.utils.SimpleWatcher;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * View: 화면/UI만 담당
 * - 버튼 클릭/입력변경을 Presenter에 위임
 * - Presenter가 전달한 결과를 화면에 반영
 * - ✅ 원본 JoinActivity의 UX/문구/흐름을 그대로 유지
 */
public class JoinActivity extends AppCompatActivity implements JoinContract.View {

    private TextInputLayout tilId, tilEmail, tilPassword, tilPasswordConfirm;
    private TextInputEditText etId, etEmail, etPassword, etPasswordConfirm;
    private MaterialButton btnCheckId, btnVerifyEmail, btnRegister;

    private JoinContract.Presenter presenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);

        bindViews();
        presenter = new JoinPresenter(this, new JoinModel());
        bindEvents();
    }

    // ✅ 추가: Activity 종료 시 Presenter가 View 참조 끊도록
    @Override
    protected void onDestroy() {
        if (presenter != null) {
            presenter.detachView();
        }
        super.onDestroy();
    }

    private void bindViews() {
        tilId = findViewById(R.id.tilId);
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        tilPasswordConfirm = findViewById(R.id.tilPasswordConfirm);

        etId = findViewById(R.id.Join_etId);
        etEmail = findViewById(R.id.Join_etEmail);
        etPassword = findViewById(R.id.Join_etPassword);
        etPasswordConfirm = findViewById(R.id.Join_etPasswordConfirm);

        btnCheckId = findViewById(R.id.btnCheckId);
        btnVerifyEmail = findViewById(R.id.btnVerifyEmail);
        btnRegister = findViewById(R.id.Join_btnRegister);
    }

    private void bindEvents() {
        // ✅ 원본: 아이디 입력 바뀌면 에러 정리 + 중복확인 캐시 무효화
        etId.addTextChangedListener(new SimpleWatcher(() -> {
            clearErrorOnId();
            presenter.onUsernameEdited(); // 원본의 lastCheckedUsernameLower/Available 리셋을 Presenter가 수행
        }));

        // ✅ 원본: 이메일 입력 중 에러/헬퍼 정리
        etEmail.addTextChangedListener(new SimpleWatcher(() -> {
            clearErrorOnEmail();
            showEmailHelper(null);
        }));

        // ✅ 원본: 비밀번호 입력 중 에러 정리
        etPassword.addTextChangedListener(new SimpleWatcher(this::clearErrorOnPassword));

        // ✅ 원본: 비밀번호 확인 → 즉시 일치 여부 helper로 안내
        etPasswordConfirm.addTextChangedListener(new SimpleWatcher(() -> {
            clearErrorOnPasswordConfirm();
            String p1 = text(etPassword);
            String p2 = text(etPasswordConfirm);
            if (!p2.isEmpty()) {
                if (p1.equals(p2)) showPasswordConfirmOk("비밀번호가 일치합니다.");
                else showPasswordConfirmError("비밀번호가 일치하지 않습니다.");
            } else {
                showPasswordConfirmOk(null); // helper 초기화
            }
        }));

        // 버튼 → Presenter 위임 (원본 흐름 유지)
        btnCheckId.setOnClickListener(v -> presenter.checkUsernameAvailability(text(etId)));
        btnVerifyEmail.setOnClickListener(v -> presenter.checkEmailAvailability(text(etEmail)));
        btnRegister.setOnClickListener(v -> presenter.attemptRegister(
                text(etId), text(etEmail), text(etPassword), text(etPasswordConfirm)
        ));
    }

    // ===== View 구현 (원본 메시지/동작 동일) =====
    @Override public void showIdError(String msg) { if (tilId != null) { tilId.setError(msg); tilId.setHelperText(null); focus(tilId); } }
    @Override public void showIdOk(String msg)    { if (tilId != null) { tilId.setError(null); tilId.setHelperText(msg); } }
    @Override public void showIdHelper(String msg){ if (tilId != null) tilId.setHelperText(msg); }

    @Override public void showEmailError(String msg) { if (tilEmail != null) { tilEmail.setError(msg); tilEmail.setHelperText(null); focus(tilEmail); } }
    @Override public void showEmailOk(String msg)    { if (tilEmail != null) { tilEmail.setError(null); tilEmail.setHelperText(msg); } }
    @Override public void showEmailHelper(String msg){ if (tilEmail != null) tilEmail.setHelperText(msg); }

    @Override public void showPasswordError(String msg) { if (tilPassword != null) { tilPassword.setError(msg); focus(tilPassword); } }
    @Override public void showPasswordConfirmError(String msg) { if (tilPasswordConfirm != null) { tilPasswordConfirm.setError(msg); tilPasswordConfirm.setHelperText(null); focus(tilPasswordConfirm); } }
    @Override public void showPasswordConfirmOk(String msg) {
        if (tilPasswordConfirm != null) {
            tilPasswordConfirm.setError(null);
            tilPasswordConfirm.setHelperText(msg);
        }
    }

    @Override public void clearErrorOnId() { if (tilId != null) tilId.setError(null); }
    @Override public void clearErrorOnEmail() { if (tilEmail != null) tilEmail.setError(null); }
    @Override public void clearErrorOnPassword() { if (tilPassword != null) tilPassword.setError(null); }
    @Override public void clearErrorOnPasswordConfirm() { if (tilPasswordConfirm != null) tilPasswordConfirm.setError(null); }

    @Override public void toast(String msg) {
        if (!isFinishing() && !isDestroyed())
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void setUiEnabled(boolean enabled) {
        if (btnCheckId != null) btnCheckId.setEnabled(enabled);
        if (btnVerifyEmail != null) btnVerifyEmail.setEnabled(enabled);
        if (btnRegister != null) btnRegister.setEnabled(enabled);

        if (etId != null) etId.setEnabled(enabled);
        if (etEmail != null) etEmail.setEnabled(enabled);
        if (etPassword != null) etPassword.setEnabled(enabled);
        if (etPasswordConfirm != null) etPasswordConfirm.setEnabled(enabled);

        float alpha = enabled ? 1f : 0.5f;
        if (btnCheckId != null) btnCheckId.setAlpha(alpha);
        if (btnVerifyEmail != null) btnVerifyEmail.setAlpha(alpha);
        if (btnRegister != null) btnRegister.setAlpha(alpha);
    }

    @Override
    public void navigateToLogin() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    // ===== 유틸 =====
    private String text(TextInputEditText et) {
        return (et != null && et.getText() != null) ? et.getText().toString() : "";
    }
    private void focus(TextInputLayout til) {
        if (til != null && til.getEditText() != null) til.getEditText().requestFocus();
    }
}
