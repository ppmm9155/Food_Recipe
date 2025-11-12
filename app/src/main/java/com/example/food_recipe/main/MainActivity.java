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
import androidx.lifecycle.ViewModelProvider; // [추가]
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.food_recipe.R;
import com.example.food_recipe.login.LoginActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth; // [추가]
import com.google.firebase.auth.FirebaseUser; // [추가]


import java.util.HashSet;
import java.util.Set;

/**
 * [변경] 중앙 인증 관리 로직을 추가합니다.
 * 이제 이 Activity는 모든 하위 프래그먼트의 인증 상태를 책임지는 중앙 관제탑 역할을 합니다.
 */
public class MainActivity extends AppCompatActivity implements MainContract.View {

    private MainContract.Presenter presenter;
    private Menu optionsMenu;

    // [추가] 중앙 인증 관리를 위한 멤버 변수 선언
    private AuthViewModel authViewModel;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener authStateListener;


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

        // [추가] AuthViewModel 초기화
        // ViewModelProvider를 통해 Activity 생명주기에 올바르게 연결된 ViewModel 인스턴스를 가져옵니다.
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // [추가] FirebaseAuth 인스턴스 초기화
        mAuth = FirebaseAuth.getInstance();

        // [추가] AuthStateListener 구현
        // 로그인 상태가 변경될 때마다(로그인, 로그아웃, 토큰 갱신 등) 호출됩니다.
        authStateListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            // 공유 ViewModel에 현재 사용자 정보를 업데이트합니다.
            // user가 null이면 로그아웃 상태, null이 아니면 로그인 상태임을 의미합니다.
            authViewModel.setUser(user);
        };


        // [기존 코드 유지] 네비게이션 변경 리스너
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (destination.getLabel() != null) {
                getSupportActionBar().setTitle(destination.getLabel());
            }

            Set<Integer> topLevelDestinations = new HashSet<>();
            topLevelDestinations.add(R.id.home_fragment);
            topLevelDestinations.add(R.id.nav_search);
            topLevelDestinations.add(R.id.nav_favorites);
            topLevelDestinations.add(R.id.nav_pantry);

            if (topLevelDestinations.contains(destination.getId())) {
                bottomNav.setVisibility(View.VISIBLE);
            } else {
                bottomNav.setVisibility(View.GONE);
            }

            // [삭제] '홈' 메뉴가 공식적으로 추가되었으므로, HomeFragment 진입 시 다른 메뉴의 선택 상태를 강제로 해제하는 코드를 삭제했습니다.
            // 이 코드가 있으면 '홈' 버튼의 활성화 표시가 사라지는 문제가 발생하기 때문입니다.
        });
    }

    private void showCustomToast(String message) {
        LayoutInflater inflater = getLayoutInflater();
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
        // [추가] Activity가 화면에 나타날 때 AuthStateListener를 등록합니다.
        mAuth.addAuthStateListener(authStateListener);
    }

    // [추가] onStop 추가
    @Override
    protected void onStop() {
        super.onStop();
        // [추가] Activity가 화면에서 사라질 때 AuthStateListener를 등록 해제하여 메모리 누수를 방지합니다.
        if (authStateListener != null) {
            mAuth.removeAuthStateListener(authStateListener);
        }
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
