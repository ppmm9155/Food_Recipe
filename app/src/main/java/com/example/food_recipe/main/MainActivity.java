package com.example.food_recipe.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.Fragment;

import com.example.food_recipe.R;
import com.example.food_recipe.login.LoginActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.appbar.MaterialToolbar;

public class MainActivity extends AppCompatActivity implements MainContract.View {

    private MainContract.Presenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Edge-to-Edge 모드 활성화
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        // 상태표시줄 아이콘을 밝은 색으로 변경
        WindowInsetsControllerCompat windowInsetsController = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (windowInsetsController != null) {
            windowInsetsController.setAppearanceLightStatusBars(false);
        }

        setContentView(R.layout.activity_main);

        presenter = new MainPresenter(this);
        presenter.attach(this);

        // --- UI 요소 초기화 ---
        MaterialToolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        View mainContainer = findViewById(R.id.main_container);

        // 충돌 방지 센서 부착 (하단 네비게이션바만 처리)
        ViewCompat.setOnApplyWindowInsetsListener(mainContainer, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());

            // AppBarLayout의 패딩 처리는 XML의 fitsSystemWindows가 담당하므로, 해당 코드는 삭제합니다.

            // BottomNavigationView의 하단 패딩을 네비게이션 바 높이만큼 추가
            bottomNav.setPadding(bottomNav.getPaddingLeft(), bottomNav.getPaddingTop(), bottomNav.getPaddingRight(), insets.bottom);

            // Inset을 소비했음을 시스템에 알림
            return WindowInsetsCompat.CONSUMED;
        });


        // 하단 네비게이션 아이템 클릭 시 동작을 설정합니다.
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selected = null;
            int id = item.getItemId();
            if (id == R.id.nav_pantry) {
                Toast.makeText(this, "냉장고 화면 (추후 구현)", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_search) {
                Toast.makeText(this, "레시피 검색 화면 (추후 구현)", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_favorites) {
                selected = new HomeFragment();
            }

            if (selected != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main_container, selected)
                        .commit();
            }
            return true;
        });

        // 앱 시작 시 기본으로 선택될 프래그먼트를 설정합니다.
        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.nav_favorites);
        }
    }

    // === Activity 생명주기 관련 메서드 ===

    @Override
    protected void onStart() {
        super.onStart();
        presenter.start();
    }

    @Override
    protected void onDestroy() {
        if (presenter != null) presenter.detach();
        super.onDestroy();
    }

    // === 툴바 메뉴 관련 메서드 ===

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_profile) {
            Toast.makeText(this, "프로필 화면(추후 연결)", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.action_logout) {
            presenter.onLogoutClicked();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // === Presenter의 지시를 수행하는 메서드 (MainContract.View 구현) ===

    @Override
    public void showLogoutMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void setLogoutEnabled(boolean enabled) {
        // 필요 시 메뉴 enable/disable 구현
    }
}
