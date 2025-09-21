// FindIdActivity.java
package com.example.food_recipe.findid;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.food_recipe.R;

public class FindIdActivity extends AppCompatActivity implements FindIdContract.View {
    private EditText Fi_editTextEmail;
    private Button Fi_buttonVerify, Fi_buttonLogin;

    private FindIdContract.Presenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_id);

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
