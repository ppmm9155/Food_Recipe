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

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        setContentView(R.layout.activity_find_id);

        View contentView = findViewById(R.id.fi_findid);
        ViewCompat.setOnApplyWindowInsetsListener(contentView, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), insets.top, v.getPaddingRight(), insets.bottom);
            return WindowInsetsCompat.CONSUMED;
        });

        Fi_editTextEmail = findViewById(R.id.fi_editTextEmail);
        Fi_buttonVerify = findViewById(R.id.fi_buttonVerify);
        Fi_buttonLogin = findViewById(R.id.fi_buttonLogin);

        // [변경] Presenter 생성 시 View를 넘기지 않음
        presenter = new FindIdPresenter(new FindIdModel());
        // [추가] Presenter에 View를 연결
        presenter.attachView(this);

        Fi_buttonVerify.setOnClickListener(v -> {
            String email = Fi_editTextEmail.getText().toString();
            presenter.onVerifyEmailClicked(email);
        });

        Fi_buttonLogin.setOnClickListener(v -> {
            presenter.onLoginClicked();
        });
    }

    @Override
    protected void onDestroy() {
        // [변경] Presenter와의 연결을 끊어서 메모리 누수를 방지
        presenter.detachView();
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
