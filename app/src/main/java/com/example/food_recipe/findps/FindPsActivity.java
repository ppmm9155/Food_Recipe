// FindPsActivity.java
package com.example.food_recipe.findps;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.example.food_recipe.login.LoginActivity;
import com.example.food_recipe.R;

public class FindPsActivity extends AppCompatActivity implements FindPsContract.View {
    private Button Fs_buttonVerify;
    private EditText Fs_editTextEmail;

    private FindPsContract.Presenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_password);

        Fs_editTextEmail = findViewById(R.id.fs_editTextEmail);
        Fs_buttonVerify = findViewById(R.id.fs_buttonVerify);

        presenter = new FindPsPresenter(this, new FindPsModel());

        Fs_buttonVerify.setOnClickListener(v -> {
            String email = Fs_editTextEmail.getText().toString();
            presenter.onVerifyEmailClicked(email);
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
