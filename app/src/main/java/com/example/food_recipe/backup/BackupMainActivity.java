package com.example.food_recipe.backup;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.food_recipe.R;
import com.example.food_recipe.login.LoginActivity;
import com.example.food_recipe.utils.AutoLoginManager;

public class BackupMainActivity extends AppCompatActivity {

    private Button btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        btnLogout = findViewById(R.id.btnLogout);

        btnLogout.setOnClickListener(v -> {
            // 중복 클릭 방지
            btnLogout.setEnabled(false);

            // ✅ 자동로그인 해제 + Firebase 로그아웃
            AutoLoginManager.logout(this);

            // (선택) Google 로그인도 쓰는 경우, 아래 주석 해제해서 함께 로그아웃
            // GoogleSignInClient gsc = GoogleSignIn.getClient(
            //         this, new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build());
            // gsc.signOut();

            Toast.makeText(this, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show();

            // ✅ 백스택 정리하고 로그인 화면로 이동 (뒤로가기 시 메인으로 못 돌아오게)
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            // finish()는 CLEAR_TASK로 대체됨
        });
    }
}
