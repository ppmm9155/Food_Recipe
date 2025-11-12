package com.example.food_recipe.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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
