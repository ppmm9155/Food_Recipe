package com.example.food_recipe.findid;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.food_recipe.R;

public class FindIdActivity extends AppCompatActivity implements FindIdContract.View {
    private EditText Fi_editTextEmail;
    private Button Fi_buttonVerify, Fi_buttonLogin;

    private FindIdContract.Presenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Phase 3: Edge-to-Edge 모드 활성화
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        setContentView(R.layout.activity_find_id);

        // Phase 3: 충돌 방지 센서 부착
        View contentView = findViewById(R.id.fi_findid);
        ViewCompat.setOnApplyWindowInsetsListener(contentView, (v, windowInsets) -> {
            // 버그 수정: WindowInsetsCompat -> Insets 타입으로 변경
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            // 시스템 바(상태표시줄, 네비게이션바) 영역만큼 패딩 적용
            v.setPadding(v.getPaddingLeft(), insets.top, v.getPaddingRight(), insets.bottom);
            return WindowInsetsCompat.CONSUMED;
        });

        // 버그 수정: 주석 해제하여 NullPointerException 방지
        Fi_editTextEmail = findViewById(R.id.fi_editTextEmail);
        Fi_buttonVerify = findViewById(R.id.fi_buttonVerify);
        Fi_buttonLogin = findViewById(R.id.fi_buttonLogin);

        // Presenter 연결 (Model 주입)
        presenter = new FindIdPresenter(this, new FindIdModel());

        Fi_buttonVerify.setOnClickListener(v -> {
            String email = Fi_editTextEmail.getText().toString();
            presenter.onVerifyEmailClicked(email);
        });

        Fi_buttonLogin.setOnClickListener(v -> {
            presenter.onLoginClicked();
        });
    }

    // ✅ 추가: Activity 종료 시 Presenter가 View 참조 끊도록
    @Override
    protected void onDestroy() {
        if (presenter != null) {
            presenter.detachView();
        }
        super.onDestroy();
    }

    @Override
    public void showEmailSentMessage() {
        Toast.makeText(this, "인증번호가 전송되었습니다.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showErrorMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showLoginRedirectMessage() {
        Toast.makeText(this, "로그인 페이지로 이동합니다.", Toast.LENGTH_SHORT).show();
    }
}
