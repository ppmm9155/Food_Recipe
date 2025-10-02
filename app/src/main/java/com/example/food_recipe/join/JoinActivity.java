package com.example.food_recipe.join;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.food_recipe.R;
import com.example.food_recipe.login.LoginActivity;
import com.example.food_recipe.utils.SimpleWatcher;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class JoinActivity extends AppCompatActivity implements JoinContract.View {

    private TextInputLayout tilId, tilEmail, tilPassword, tilPasswordConfirm;
    private TextInputEditText etId, etEmail, etPassword, etPasswordConfirm;
    private MaterialButton btnCheckId, btnVerifyEmail, btnRegister;

    private JoinContract.Presenter presenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Phase 3: Edge-to-Edge 모드 활성화
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        setContentView(R.layout.activity_join);

        bindViews();

        // Phase 3: 충돌 방지 센서 부착
        View contentView = findViewById(R.id.join);
        ViewCompat.setOnApplyWindowInsetsListener(contentView, (v, windowInsets) -> {
            // 버그 수정: WindowInsetsCompat -> Insets 타입으로 변경
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            // 시스템 바(상태표시줄, 네비게이션바) 영역만큼 패딩 적용
            v.setPadding(v.getPaddingLeft(), insets.top, v.getPaddingRight(), insets.bottom);
            return WindowInsetsCompat.CONSUMED;
        });

        presenter = new JoinPresenter(this, new JoinModel());
        bindEvents();
    }

    @Override
    protected void onDestroy() {
        if (presenter != null) {
            presenter.detachView();
        }
        super.onDestroy();
    }

    private void bindViews() {
        tilId = findViewById(R.id.join_tilId);
        tilEmail = findViewById(R.id.join_tilEmail);
        tilPassword = findViewById(R.id.join_tilPassword);
        tilPasswordConfirm = findViewById(R.id.join_tilPasswordConfirm);

        etId = findViewById(R.id.join_etId);
        etEmail = findViewById(R.id.join_etEmail);
        etPassword = findViewById(R.id.join_etPassword);
        etPasswordConfirm = findViewById(R.id.join_etPasswordConfirm);

        btnCheckId = findViewById(R.id.join_btnCheckId);
        btnVerifyEmail = findViewById(R.id.join_btnVerifyEmail);
        btnRegister = findViewById(R.id.join_btnRegister);
    }

    private void bindEvents() {
        etId.addTextChangedListener(new SimpleWatcher(() -> {
            clearErrorOnId();
            presenter.onUsernameEdited();
        }));

        etEmail.addTextChangedListener(new SimpleWatcher(() -> {
            clearErrorOnEmail();
            showEmailHelper(null);
        }));

        etPassword.addTextChangedListener(new SimpleWatcher(this::clearErrorOnPassword));

        etPasswordConfirm.addTextChangedListener(new SimpleWatcher(() -> {
            clearErrorOnPasswordConfirm();
            String p1 = text(etPassword);
            String p2 = text(etPasswordConfirm);
            if (!p2.isEmpty()) {
                if (p1.equals(p2)) showPasswordConfirmOk("비밀번호가 일치합니다.");
                else showPasswordConfirmError("비밀번호가 일치하지 않습니다.");
            } else {
                showPasswordConfirmOk(null);
            }
        }));

        btnCheckId.setOnClickListener(v -> presenter.checkUsernameAvailability(text(etId)));
        btnVerifyEmail.setOnClickListener(v -> presenter.checkEmailAvailability(text(etEmail)));
        btnRegister.setOnClickListener(v -> presenter.attemptRegister(
                text(etId), text(etEmail), text(etPassword), text(etPasswordConfirm)
        ));
    }

    // ===== View 구현 =====
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
