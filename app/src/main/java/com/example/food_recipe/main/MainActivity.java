package com.example.food_recipe.main;

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
import androidx.annotation.IdRes; // [추가] @IdRes 어노테이션을 위해
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.food_recipe.FoodRecipeApplication;
import com.example.food_recipe.R;
import com.example.food_recipe.login.LoginActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements MainContract.View {

    private MainContract.Presenter presenter;
    private AuthViewModel authViewModel;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private BottomNavigationView bottomNav; // [추가] 다른 메서드에서 접근할 수 있도록 멤버 변수로 변경

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Log.d("ExpirationCheckWorker", "알림 권한이 허용되었습니다. 작업을 예약합니다.");
                    scheduleWorkWithUid();
                } else {
                    Log.d("ExpirationCheckWorker", "알림 권한이 거부되었습니다. 작업을 취소합니다.");
                    FoodRecipeApplication application = (FoodRecipeApplication) getApplication();
                    application.cancelExpirationCheck();
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

        presenter = new MainPresenter();
        presenter.attachView(this);

        MaterialToolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        NavController navController = navHostFragment.getNavController();

        bottomNav = findViewById(R.id.main_bottom_nav); // [수정] 멤버 변수에 할당
        NavigationUI.setupWithNavController(bottomNav, navController);

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        mAuth = FirebaseAuth.getInstance();

        authStateListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            authViewModel.setUser(user);
            // [추가] 로그인 상태가 변경될 때마다 알림 권한 및 작업 예약을 다시 확인합니다.
            if (user != null) {
                handleNotificationPermission();
            }
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
    }

    /**
     * [추가] HomeFragment와 같은 자식 Fragment가 호출할 수 있는 탭 전환 메서드
     * @param tabId 이동할 탭의 메뉴 아이템 ID (e.g., R.id.nav_favorites)
     */
    public void navigateToTab(@IdRes int tabId) {
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(tabId);
        }
    }

    private void handleNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED) {
                Log.d("ExpirationCheckWorker", "알림 권한이 이미 허용되어 있습니다. 작업을 예약합니다.");
                scheduleWorkWithUid();
            } else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        } else {
            Log.d("ExpirationCheckWorker", "하위 버전 OS이므로 알림 권한 없이 작업을 예약합니다.");
            scheduleWorkWithUid();
        }
    }

    /**
     * [추가] 현재 사용자의 UID를 가져와 WorkManager 작업을 예약하는 헬퍼 메서드입니다.
     */
    private void scheduleWorkWithUid() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            FoodRecipeApplication application = (FoodRecipeApplication) getApplication();
            application.scheduleExpirationCheck(uid);
            Log.d("ExpirationCheckWorker", "사용자(" + uid + ")에 대한 작업이 예약되었습니다.");
        } else {
            Log.w("ExpirationCheckWorker", "로그인한 사용자가 없어 작업을 예약할 수 없습니다.");
        }
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
        if (presenter != null) presenter.detachView();
        super.onDestroy();
    }

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
