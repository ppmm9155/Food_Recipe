package com.example.food_recipe.backup;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.food_recipe.R;

public class BackupFindIdActivity extends AppCompatActivity {
    private TextView Fi_textViewTitle, Fi_textViewEmailLabel, Fi_textViewEmailAuthLabel, Fi_textViewNewIdLabel;
    private EditText Fi_editTextEmail, Fi_editTextEmailAuth, Fi_editTextNewId;
    private Button Fi_buttonVerify, Fi_buttonLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_id);

        // 뷰 바인딩
        Fi_textViewTitle = findViewById(R.id.fi_textViewTitle);
        Fi_textViewEmailLabel = findViewById(R.id.fi_textViewEmailLabel);
        Fi_textViewEmailAuthLabel = findViewById(R.id.fi_textViewEmailAuthLabel);
        Fi_textViewNewIdLabel = findViewById(R.id.fi_textViewNewIdLabel);

        Fi_editTextEmail = findViewById(R.id.fi_editTextEmail);
        Fi_editTextEmailAuth = findViewById(R.id.fi_editTextEmailAuth);
        Fi_editTextNewId = findViewById(R.id.fi_editTextNewId);

        Fi_buttonVerify = findViewById(R.id.fi_buttonVerify);
        Fi_buttonLogin = findViewById(R.id.fi_buttonLogin);

        // 메세지만 띄웠지 실제 이메일로 링크가 전송되지는 않음
        Fi_buttonVerify.setOnClickListener(v -> {
            Toast.makeText(this, "인증번호가 전송되었습니다.", Toast.LENGTH_SHORT).show();
        });

        Fi_buttonLogin.setOnClickListener(v -> {
            Toast.makeText(this, "로그인 페이지로 이동합니다.", Toast.LENGTH_SHORT).show();
        });
    }
}
