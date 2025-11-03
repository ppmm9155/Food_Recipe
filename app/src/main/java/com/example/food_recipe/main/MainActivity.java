package com.example.food_recipe.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.food_recipe.R;
import com.example.food_recipe.login.LoginActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.HashSet;
import java.util.Set;

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

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        NavController navController = navHostFragment.getNavController();

        BottomNavigationView bottomNav = findViewById(R.id.main_bottom_nav);
        NavigationUI.setupWithNavController(bottomNav, navController);

        // [변경] 기존의 addOnDestinationChangedListener를 확장하여 하단 탭 가시성 및 툴바 제목을 제어합니다.
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            // [추가] navgraph.xml에 정의된 label을 가져와 툴바의 제목으로 설정합니다.
            if (destination.getLabel() != null) {
                getSupportActionBar().setTitle(destination.getLabel());
            }

            // [추가] 하단 탭을 보여줄 최상위 레벨의 화면 ID들을 Set으로 정의합니다.
            // Set을 사용하면 ID를 효율적으로 확인할 수 있습니다.
            Set<Integer> topLevelDestinations = new HashSet<>();
            topLevelDestinations.add(R.id.home_fragment);
            topLevelDestinations.add(R.id.nav_search);
            topLevelDestinations.add(R.id.nav_favorites);
            topLevelDestinations.add(R.id.nav_pantry);

            // [추가] 현재 화면의 ID가 최상위 레벨 Set에 포함되어 있는지 확인합니다.
            if (topLevelDestinations.contains(destination.getId())) {
                // 포함되어 있다면 하단 네비게이션 바를 보여줍니다.
                bottomNav.setVisibility(View.VISIBLE);
            } else {
                // 포함되어 있지 않다면 (예: 레시피 상세 화면) 하단 네비게이션 바를 숨깁니다.
                bottomNav.setVisibility(View.GONE);
            }

            // [기존 로직 유지] ID를 다시 home_fragment로 되돌려 navgraph.xml과 완벽하게 일치시킵니다.
            if (destination.getId() == R.id.home_fragment) {
                int selectedItemId = bottomNav.getSelectedItemId();
                if (selectedItemId != 0) {
                    bottomNav.getMenu().findItem(selectedItemId).setChecked(false);
                }
            }
        });
    }

    // [추가] 아이콘이 포함된 커스텀 토스트 메시지를 표시하는 함수입니다.
    private void showCustomToast(String message) {
        LayoutInflater inflater = getLayoutInflater();
        // [수정] 두 번째 인자를 null로 변경하여 특정 부모 뷰를 찾지 않도록 합니다.
        View layout = inflater.inflate(R.layout.custom_toast, null);

        TextView text = layout.findViewById(R.id.toast_text);
        text.setText(message);

        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.BOTTOM, 0, 200);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
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
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            presenter.onLogoutClicked();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void showLogoutMessage(String message) {
        // [변경] 기본 토스트 대신 새로 만든 커스텀 토스트 함수를 사용합니다.
        showCustomToast(message);
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
            }
        }
    }

    @Override
    public Context getContext() {
        return this;
    }
}
