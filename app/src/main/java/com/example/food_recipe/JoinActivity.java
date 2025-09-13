package com.example.food_recipe;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class JoinActivity extends AppCompatActivity {

    // 화면이름 + 변수명

    private FirebaseAuth Join_mAuth;

    private EditText Join_email, Join_password, Join_passwordCheck;
    private Button Join_btnJoin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_join);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.join), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Firebase Auth 인스턴스 초기화
        Join_mAuth = FirebaseAuth.getInstance();

        //설정
        Join_email = (EditText) findViewById(R.id.Join_etEmail);
        Join_password = (EditText) findViewById(R.id.Join_etPassword);
        Join_passwordCheck = (EditText) findViewById(R.id.Join_etPasswordConfirm);
        Join_btnJoin = (Button) findViewById(R.id.Join_btnRegister);

        //회원가입 버튼 클릭 시 동작 설정
        Join_btnJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = Join_email.getText().toString().trim();
                String password = Join_password.getText().toString().trim();
                String passwordCheck = Join_passwordCheck.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty() || passwordCheck.isEmpty()) {
                    Toast.makeText(JoinActivity.this, "모든 항목을 입력해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!password.equals(passwordCheck)) {
                    Toast.makeText(JoinActivity.this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Firebase로 회원가입 시도
                registerUser(email, password);

            }
        });

    }

    //입력받은 이메일과 비밀번호로 Firebase에 새로운 사용자를 등록하는 메서드
    private void registerUser(String email, String password) {
        Join_mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //회원가입 성공
                            Toast.makeText(JoinActivity.this, "회원가입 성공! 로그인해주세요", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            //회원가입 실패
                            Toast.makeText(JoinActivity.this, "회원가입에 실패했습니다. 이미 가입된 이메일일 수 있습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

}

