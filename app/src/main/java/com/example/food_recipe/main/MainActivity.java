package com.example.food_recipe.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;
// [핵심 Import] Jetpack Navigation Component의 핵심 클래스들입니다.
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.food_recipe.R;
import com.example.food_recipe.login.LoginActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.appbar.MaterialToolbar;

/**
 * MainActivity는 앱의 메인 화면을 담당하는 유일한 액티비티입니다.
 * 이 액티비티는 상단 툴바, 하단 네비게이션 바, 그리고 그 사이의 프래그먼트 콘텐츠 영역을 포함합니다.
 * MVP 패턴의 'View' 역할을 수행하며, 사용자 입력을 Presenter에 전달하고 Presenter의 지시에 따라 UI를 업데이트합니다.
 */
public class MainActivity extends AppCompatActivity implements MainContract.View {

    // MVP 패턴의 Presenter 인터페이스입니다. View는 Presenter를 통해 비즈니스 로직을 처리합니다.
    private MainContract.Presenter presenter;
    // 툴바의 메뉴 아이템(예: 로그아웃 버튼)에 접근하기 위한 변수입니다.
    private Menu optionsMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // [UI 설정] Edge-to-Edge UI를 구현합니다.
        // 이 코드는 앱 콘텐츠가 상태바나 네비게이션바 영역 뒤까지 확장될 수 있도록 허용하여
        // 더 현대적이고 몰입감 있는 UI를 제공합니다.
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        WindowInsetsControllerCompat windowInsetsController = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (windowInsetsController != null) {
            // 상태바 아이콘들을 어둡게 설정합니다. (배경이 밝을 경우 사용)
            // 만약 배경이 어둡다면 false로 설정해야 아이콘이 흰색으로 보입니다.
            windowInsetsController.setAppearanceLightStatusBars(false);
        }

        // activity_main.xml 레이아웃 파일을 화면에 표시합니다.
        setContentView(R.layout.activity_main);

        // [MVP 초기화] Presenter를 생성하고 View(자기 자신)를 연결합니다.
        presenter = new MainPresenter(this);
        presenter.attach(this);

        // [Toolbar 설정] activity_main.xml에 있는 툴바를 찾아 액션바로 설정합니다.
        MaterialToolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);


        // =================================================================
        // [Jetpack Navigation Component 설정] - 모든 화면 전환 로직의 핵심
        // =================================================================

        // 1. NavHostFragment 가져오기
        // activity_main.xml에 정의된 FragmentContainerView(ID: nav_host_fragment)를 찾습니다.
        // 이 NavHostFragment는 모든 프래그먼트들이 교체되며 보여지는 '무대' 역할을 합니다.
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        // 2. NavController 가져오기
        // 실제 화면 전환을 담당하는 '총 감독'인 NavController를 NavHostFragment로부터 얻습니다.
        // 앞으로 모든 화면 이동 명령은 이 NavController를 통해 이루어집니다.
        NavController navController = navHostFragment.getNavController();

        // 3. BottomNavigationView와 NavController 자동 연결
        // activity_main.xml의 BottomNavigationView(ID: main_bottom_nav)를 찾습니다.
        BottomNavigationView bottomNav = findViewById(R.id.main_bottom_nav);
        // [자동화의 핵심] 이 한 줄의 코드는 BottomNavigationView의 메뉴 아이템 ID와
        // navgraph.xml에 정의된 destination(프래그먼트)의 ID를 비교하여,
        // 탭 클릭 시 자동으로 해당 프래그먼트로 이동시키고 탭 활성 상태를 관리해줍니다.
        NavigationUI.setupWithNavController(bottomNav, navController);


        // -----------------------------------------------------------------
        // [추가된 코드] 홈 화면으로 돌아올 때 탭 활성 상태를 수동으로 초기화하는 로직
        // -----------------------------------------------------------------
        // Navigation Component의 '자동 연결'만으로는 해결되지 않는 미묘한 UI 문제를 처리하기 위해
        // 화면 전환 이벤트를 직접 감지하는 리스너('개인 비서')를 추가합니다.
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            // [문제 상황] '냉장고' 탭을 눌렀다가 뒤로가기를 눌러 '홈' 화면으로 돌아오면,
            //            화면은 홈으로 바뀌었지만 하단 탭은 여전히 '냉장고'가 선택된 상태로 남아있습니다.
            // [해결 로직] 화면이 전환될 때마다, 새로 표시된 화면의 ID를 확인합니다.
            if (destination.getId() == R.id.home_fragment) {
                // 만약 새로 표시된 화면이 '홈' 프래그먼트라면,
                // 현재 BottomNavigationView에서 선택된 아이템의 ID를 가져옵니다.
                int selectedItemId = bottomNav.getSelectedItemId();

                // 만약 선택된 아이템이 있다면 (ID가 0이 아니라면),
                if (selectedItemId != 0) {
                    // 메뉴에서 해당 아이템을 찾아, 체크된 상태를 '강제로' 해제합니다.
                    // 이를 통해 홈 화면에서는 모든 탭이 비활성화된 것처럼 보이게 됩니다.
                    bottomNav.getMenu().findItem(selectedItemId).setChecked(false);
                }
            }
        });
    }

    // [MVP 연동] Activity의 생명주기(Lifecycle) 이벤트를 Presenter에게 전달합니다.
    @Override
    protected void onStart() {
        super.onStart();
        presenter.start(); // Presenter에게 시작을 알립니다. (예: 로그인 상태 확인 등)
    }

    // [MVP 연동] Activity가 소멸될 때 Presenter와의 연결을 끊어 메모리 누수를 방지합니다.
    @Override
    protected void onDestroy() {
        if (presenter != null) presenter.detach();
        super.onDestroy();
    }

    // [Toolbar 메뉴 설정] 툴바에 표시될 메뉴(main_toolbar_menu.xml)를 생성합니다.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_toolbar_menu, menu);
        this.optionsMenu = menu;
        return true;
    }

    // [Toolbar 메뉴 클릭 처리] 툴바의 메뉴 아이템(로그아웃 등)이 클릭되었을 때 호출됩니다.
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            // 로그아웃 버튼이 클릭되면, 직접 로직을 처리하지 않고 Presenter에게 위임합니다.
            presenter.onLogoutClicked();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    // =================================================================
    // [MainContract.View 인터페이스 구현] - Presenter가 View를 제어하는 메서드들
    // =================================================================

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
            }
        }
    }

    @Override
    public Context getContext() {
        return this;
    }
}
