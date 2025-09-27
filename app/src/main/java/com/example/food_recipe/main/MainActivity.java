package com.example.food_recipe.main;

import android.content.Context; // (새로추가됨) Context 사용을 위해 import
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
import com.example.food_recipe.home.HomeFragment;
import com.example.food_recipe.login.LoginActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.appbar.MaterialToolbar;

public class MainActivity extends AppCompatActivity implements MainContract.View {

    private MainContract.Presenter presenter;
    private Menu optionsMenu; // (새로추가됨) 옵션 메뉴 객체를 저장하기 위한 멤버 변수

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
        presenter.start(); // Presenter에게 시작을 알림 (로그인 상태 확인 등)
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
        this.optionsMenu = menu; // (새로추가됨) 메뉴 객체 저장

        // --- 프로필 아이콘 클릭 리스너 설정 추가 ---
        MenuItem profileItem = menu.findItem(R.id.action_profile);
        if (profileItem != null) {
            View actionView = profileItem.getActionView();
            if (actionView != null) {
                actionView.setOnClickListener(v -> {
                    Toast.makeText(MainActivity.this, "프로필 화면(추후 연결)", Toast.LENGTH_SHORT).show();
                });
            }
        }
        // --- 여기까지 추가 ---
        // (새로추가됨) 초기 presenter 상태에 따라 로그아웃 버튼 활성화 여부 반영 (선택적이지만 권장)
        // 이 시점에는 presenter가 아직 start()를 호출하지 않았을 수 있으므로,
        // presenter.start() 완료 후 또는 로그인 상태가 명확해진 후 호출하는 것이 더 정확할 수 있습니다.
        // presenter.start() 내부의 onLoggedIn 콜백에서 setLogoutEnabled(true)가 호출되므로,
        // 여기서 별도로 호출할 필요는 없을 수 있습니다. 또는 onPrepareOptionsMenu에서 처리할 수도 있습니다.
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_profile) {
            // 이 부분은 actionView의 리스너에서 이미 처리하므로, 여기서는 호출되지 않거나
            // 중복될 수 있습니다. 필요에 따라 주석 처리하거나 다른 방식으로 관리할 수 있습니다.
            return true;
        } else if (id == R.id.action_logout) {
            if (item.isEnabled()) { // (새로추가됨) 버튼이 활성화되어 있을 때만 Presenter 호출
                presenter.onLogoutClicked();
            }
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
        // (변경된부분) 실제 로그아웃 메뉴 아이템 활성화/비활성화
        if (optionsMenu != null) {
            MenuItem logoutItem = optionsMenu.findItem(R.id.action_logout);
            if (logoutItem != null) {
                logoutItem.setEnabled(enabled);
                // (선택적) 아이콘의 알파값을 변경하여 시각적 피드백 강화
                // Drawable icon = logoutItem.getIcon();
                // if (icon != null) {
                //    icon.setAlpha(enabled ? 255 : 130);
                // }
                android.util.Log.d("MainActivity", "setLogoutEnabled: Logout menu item " + (enabled ? "enabled" : "disabled"));
            } else {
                android.util.Log.w("MainActivity", "setLogoutEnabled: Logout menu item (R.id.action_logout) not found.");
            }
        } else {
            android.util.Log.w("MainActivity", "setLogoutEnabled: OptionsMenu is null. Cannot enable/disable logout menu item.");
        }
    }

    // (새로추가됨) MainContract.View 인터페이스의 getContext() 메소드 구현
    @Override
    public Context getContext() {
        return this; // Activity Context 반환
    }
}
