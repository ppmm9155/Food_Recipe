package com.example.food_recipe.findps;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.food_recipe.login.LoginActivity;
import com.example.food_recipe.R;

public class FindPsActivity extends AppCompatActivity implements FindPsContract.View {
    private Button Fs_buttonVerify;
    private EditText Fs_editTextEmail;

    private FindPsContract.Presenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        setContentView(R.layout.activity_find_password);

        View contentView = findViewById(R.id.fs_findps);
        ViewCompat.setOnApplyWindowInsetsListener(contentView, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), insets.top, v.getPaddingRight(), insets.bottom);
            return WindowInsetsCompat.CONSUMED;
        });

        Fs_editTextEmail = findViewById(R.id.fs_editTextEmail);
        Fs_buttonVerify = findViewById(R.id.fs_buttonVerify);

        // [변경] Presenter 생성 시 View를 넘기지 않음
        presenter = new FindPsPresenter(new FindPsModel());
        // [추가] Presenter에 View를 연결
        presenter.attachView(this);

        Fs_buttonVerify.setOnClickListener(v -> {
            String email = Fs_editTextEmail.getText().toString();
            presenter.onVerifyEmailClicked(email);
        });
    }

    @Override
    protected void onDestroy() {
        // [변경] Presenter와의 연결을 끊어서 메모리 누수를 방지
        presenter.detachView();
        super.onDestroy();
    }

    @Override
    public void showEmailEmptyError() {
        new AlertDialog.Builder(this)
                .setTitle("이메일 입력 오류")
                .setMessage("이메일을 입력해주세요.")
                .setPositiveButton("확인", null)
                .show();
    }

    @Override
    public void showResetEmailSentMessage() {
        new AlertDialog.Builder(this)
                .setTitle("이메일 인증")
                .setMessage("귀하의 이메일로 인증 링크를 발송했습니다. 메일을 확인해주세요.")
                .setPositiveButton("확인", ((dialog, which) -> navigateToLogin()))
                .show();
    }

    @Override
    public void showResetEmailFailedMessage() {
        new AlertDialog.Builder(this)
                .setTitle("이메일 인증 실패")
                .setMessage("이메일 인증에 실패했습니다. 다시 시도해주세요.")
                .setPositiveButton("확인", null)
                .show();
    }

    @Override
    public void navigateToLogin() {
        Intent intent = new Intent(FindPsActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
