package com.example.food_recipe.main;

import android.content.Context;
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
    private Menu optionsMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        WindowInsetsControllerCompat windowInsetsController = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (windowInsetsController != null) {
            windowInsetsController.setAppearanceLightStatusBars(false);
        }

        setContentView(R.layout.activity_main);

        presenter = new MainPresenter(this);
        presenter.attach(this);

        MaterialToolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        BottomNavigationView bottomNav = findViewById(R.id.main_bottom_nav);
        View mainContainer = findViewById(R.id.main_container);

        ViewCompat.setOnApplyWindowInsetsListener(mainContainer, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            bottomNav.setPadding(bottomNav.getPaddingLeft(), bottomNav.getPaddingTop(), bottomNav.getPaddingRight(), insets.bottom);
            return WindowInsetsCompat.CONSUMED;
        });


        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selected = null;
            int id = item.getItemId();
            if (id == R.id.nav_pantry) {
                Toast.makeText(this, "냉장고 화면 (추후 구현)", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_search) {
                Toast.makeText(this, "레시피 검색 화면 (추후 구현)", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_favorites) {
                // [변경] HomeFragment와 즐겨찾기 탭의 연결을 끊었습니다.
                // [주석 처리] selected = new HomeFragment();
                // [추가] 즐겨찾기 프래그먼트는 나중에 별도로 추가될 예정이므로, 현재는 토스트 메시지만 띄웁니다.
                Toast.makeText(this, "즐겨찾기 화면 (추후 구현)", Toast.LENGTH_SHORT).show();
            }

            if (selected != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main_container, selected)
                        .commit();
            }
            return true;
        });

        // [삭제] 이전에 이 위치에 있던 'bottomNav.setSelectedItemId(R.id.nav_favorites);' 코드를 삭제했습니다.
        //      해당 코드는 MainActivity 진입 시 '즐겨찾기' 탭을 강제로 선택하는 역할을 했습니다.

        // [추가] MainActivity가 처음 생성되었을 때, 기본으로 HomeFragment를 표시하는 코드입니다.
        //      이 코드는 하단 네비게이션 탭의 '선택 상태'와는 완전히 독립적으로 동작하여,
        //      HomeFragment(메인 UI)를 화면에 보여주면서도 하단 탭은 아무것도 선택되지 않은 상태로 만들 수 있습니다.
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_container, new HomeFragment())
                    .commit();
          
            // [추가] BottomNavigationView의 자동선택 동작을 무효화하기 위해, 메뉴에 추가한 보이지 않는 더미 아이템을 선택합니다.
            //      이렇게 하면 시각적으로는 아무 탭도 선택되지 않은 상태가 됩니다.
            //      (복구 방법: 만약 이 기능을 제거하고 싶다면, 이 아래 한 줄의 코드만 주석 처리하거나 삭제하면 됩니다.)

            bottomNav.setSelectedItemId(R.id.nav_none);
        }
    }

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_toolbar_menu, menu);
        this.optionsMenu = menu;

        MenuItem profileItem = menu.findItem(R.id.action_profile);
        if (profileItem != null) {
            View actionView = profileItem.getActionView();
            if (actionView != null) {
                actionView.setOnClickListener(v -> {
                    Toast.makeText(MainActivity.this, "프로필 화면(추후 연결)", Toast.LENGTH_SHORT).show();
                });
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_profile) {
            return true;
        } else if (id == R.id.action_logout) {
            if (item.isEnabled()) {
                presenter.onLogoutClicked();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

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
        if (optionsMenu != null) {
            MenuItem logoutItem = optionsMenu.findItem(R.id.action_logout);
            if (logoutItem != null) {
                logoutItem.setEnabled(enabled);
                android.util.Log.d("MainActivity", "setLogoutEnabled: Logout menu item " + (enabled ? "enabled" : "disabled"));
            } else {
                android.util.Log.w("MainActivity", "setLogoutEnabled: Logout menu item (R.id.action_logout) not found.");
            }
        } else {
            android.util.Log.w("MainActivity", "setLogoutEnabled: OptionsMenu is null. Cannot enable/disable logout menu item.");
        }
    }

    @Override
    public Context getContext() {
        return this;
    }
}
