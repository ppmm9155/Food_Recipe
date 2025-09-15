package com.example.food_recipe;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private EditText login_etEmail, login_etPassword;
    private Button login_btnLogin;
    private TextView login_tvJoint, login_tvFindId, login_tvFindPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        login_btnLogin = (Button) findViewById(R.id.login_btn);
        login_etEmail = (EditText) findViewById(R.id.ETemail);
        login_etPassword = (EditText) findViewById(R.id.ETpassword);
        login_tvJoint = (TextView) findViewById(R.id.joinT);

        // 로그인 버튼 클릭 시 동작 생성
        login_btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = login_etEmail.getText().toString().trim();
                String password = login_etPassword.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "이메일과 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }
                loginUser(email, password);
            }
        });

        login_tvJoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, JoinActivity.class);
                startActivity(intent);
            }
        });
    }

    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(LoginActivity.this, "로그인 성공", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Exception exception = task.getException();
                    Log.e("LoginError", "로그인 실패", exception);

                    String userMessage = "로그인에 실패했습니다. 다시 시도해주세요";

                    if(exception instanceof FirebaseAuthException) {
                        String errorCode = ((FirebaseAuthException) exception).getErrorCode();

                        switch (errorCode) {
                            case "ERROR_INVALID_EMAIL":
                                userMessage = "이메일 형식이 올바르지 않습니다.";
                                break;
                                case "ERROR_WRONG_PASSWORD":
                                userMessage = "비밀번호가 올바르지 않습니다.";
                                break;
                            case "ERROR_USER_NOT_FOUND":
                                userMessage = "존재하지 않는 사용자입니다.";
                                break;
                                case "ERROR_USER_DISABLED":
                                    userMessage = "사용자 계정이 비활성화되었습니다.";
                                    break;
                        }
                    }
                    Toast.makeText(LoginActivity.this, userMessage, Toast.LENGTH_SHORT).show();
                }
            }
        });
    };
}
