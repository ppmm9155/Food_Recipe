package com.example.food_recipe.main;

// [추가] 권한 요청 관련 클래스를 가져옵니다.
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.food_recipe.R;
import com.example.food_recipe.login.LoginActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


import java.util.HashSet;
import java.util.Set;

/**
 * [변경] 중앙 인증 관리 로직을 추가합니다.
 * 이제 이 Activity는 모든 하위 프래그먼트의 인증 상태를 책임지는 중앙 관제탑 역할을 합니다.
 */
public class MainActivity extends AppCompatActivity implements MainContract.View {

    private MainContract.Presenter presenter;
    // [삭제] 툴바 메뉴 관련 코드를 모두 삭제합니다.

    private AuthViewModel authViewModel;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener authStateListener;

    // [추가] 알림 권한 요청을 처리하기 위한 ActivityResultLauncher를 등록합니다.
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // 사용자가 권한을 허용한 경우
                    Log.d("Permission", "알림 권한이 허용되었습니다.");
                } else {
                    // 사용자가 권한을 거부한 경우
                    Log.d("Permission", "알림 권한이 거부되었습니다.");
                    // (선택사항) 사용자에게 왜 알림이 필요한지 설명하는 UI를 보여줄 수 있습니다.
                }
            });


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

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        mAuth = FirebaseAuth.getInstance();

        authStateListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            authViewModel.setUser(user);
        };

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (getSupportActionBar() != null && destination.getLabel() != null) {
                getSupportActionBar().setTitle(destination.getLabel());
            }

            Set<Integer> topLevelDestinations = new HashSet<>();
            topLevelDestinations.add(R.id.home_fragment);
            topLevelDestinations.add(R.id.nav_search);
            topLevelDestinations.add(R.id.nav_favorites);
            topLevelDestinations.add(R.id.nav_pantry);
            topLevelDestinations.add(R.id.nav_mypage);

            if (topLevelDestinations.contains(destination.getId())) {
                bottomNav.setVisibility(View.VISIBLE);
            } else {
                bottomNav.setVisibility(View.GONE);
            }
        });

        // [추가] 알림 권한 요청 로직을 호출합니다.
        requestNotificationPermission();
    }

    /**
     * [추가] Android 13 (API 33) 이상에서 POST_NOTIFICATIONS 권한을 요청합니다.
     */
    private void requestNotificationPermission() {
        // 안드로이드 13 이상 버전에서만 권한 요청을 진행합니다.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // 이미 권한이 부여되었는지 확인합니다.
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED) {
                // 권한이 이미 있으면 아무것도 하지 않습니다.
                Log.d("Permission", "알림 권한이 이미 부여되어 있습니다.");
            } else {
                // 권한이 없으면 사용자에게 권한을 요청합니다.
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
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
        mAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (authStateListener != null) {
            mAuth.removeAuthStateListener(authStateListener);
        }
    }


    @Override
    protected void onDestroy() {
        if (presenter != null) presenter.detach();
        super.onDestroy();
    }

    // [삭제] 툴바 메뉴 관련 코드를 모두 삭제했으며, Contract 변경에 따라 불필요해진 메서드를 완전히 삭제합니다.

    @Override
    public void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public Context getContext() {
        return this;
    }
}
