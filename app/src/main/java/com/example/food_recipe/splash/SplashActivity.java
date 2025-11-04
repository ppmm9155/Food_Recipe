
package com.example.food_recipe.splash;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.example.food_recipe.login.LoginActivity;
import com.example.food_recipe.main.MainActivity;
import com.example.food_recipe.utils.AutoLoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 *  스플래시 화면 (앱의 첫인상)
 *
 * 이 화면은 앱이 시작될 때 가장 먼저 사용자에게 보여지는 로딩 화면입니다.
 * 이 화면의 가장 중요한 임무는 "사용자가 현재 로그인 상태인지 아닌지"를 확인해서,
 * 로그인 상태라면 메인 화면으로, 아니라면 로그인 화면으로 보내주는 것입니다.
 */
@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    // Logcat에서 "SplashActivity" 태그로 로그를 필터링해서 보기 위한 이름표입니다.
    private static final String TAG = "SplashActivity";

    // Firebase의 로그인 상태 변경을 실시간으로 감지하는 "알람 장치"입니다.
    private FirebaseAuth.AuthStateListener authStateListener;

    // Firebase 인증 기능(로그인, 로그아웃 등)을 사용하기 위한 "만능 도구"입니다.
    private FirebaseAuth firebaseAuth;

    // 화면 전환이 두 번 이상 실행되는 것을 막기 위한 "안전 스위치"입니다. (true가 되면 더 이상 작동 안함)
    private boolean isUserNavigated = false;

    // [추가] 스플래시 화면을 계속 보여줄지 결정하는 스위치입니다.
    private boolean keepSplashScreenOn = true;

    /**
     * Activity가 처음 생성될 때 호출되는 가장 중요한 메서드입니다.
     * (앱 실행 시 가장 먼저 실행되는 코드 블록)
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // [변경] installSplashScreen의 반환 객체를 받아 스플래시 화면을 제어합니다.
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);

        // [추가] 스플래시 화면이 계속 보여질 조건을 설정합니다.
        // keepSplashScreenOn이 true인 동안은 스플래시 아이콘이 사라지지 않습니다.
        splashScreen.setKeepOnScreenCondition(() -> keepSplashScreenOn);

        // Firebase 인증 도구를 초기화합니다. (사용할 준비)
        firebaseAuth = FirebaseAuth.getInstance();

        // "알람 장치"를 설정합니다.
        // 이 코드는 로그인 상태가 바뀔 때마다 (로그인, 로그아웃, 게스트 세션 확인 등) 자동으로 실행됩니다.
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth auth) {
                // 안전 스위치가 켜져있으면(이미 화면 전환을 했다면), 아무것도 하지 않고 즉시 종료합니다.
                if (isUserNavigated) {
                    return;
                }

                // 현재 로그인된 사용자 정보를 가져옵니다. 없으면 null이 됩니다.
                FirebaseUser user = auth.getCurrentUser();

                // [변경] 스플래시 화면 표시 시간을 1.5초로 늘립니다.
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    // 1.5초가 지난 후, 이 코드가 실행됩니다.

                    // [추가] 이제 스플래시를 닫아도 좋다고 시스템에 알립니다.
                    keepSplashScreenOn = false;

                    // 화면 전환 직전에, 이 Activity가 아직 살아있는지(파괴되지 않았는지) 최종 확인합니다.
                    // (앱 종료 버튼을 누르는 등 예외적인 상황에서 오류를 방지하는 안전 코드)
                    if (!isDestroyed() && !isFinishing()) {
                        // 기존의 화면 전환 로직은 그대로 유지합니다.
                        if (shouldAutoLogin(user)) {
                            Log.d(TAG, "상태 변경 감지: 자동 로그인 조건 만족 -> 메인으로 이동");
                            isUserNavigated = true;
                            navigateToMain();
                        } else {
                            Log.d(TAG, "상태 변경 감지: 자동 로그인 조건 불만족 -> 로그인으로 이동");
                            isUserNavigated = true;
                            navigateToLogin();
                        }
                    }
                }, 1000); // 1000 밀리초 = 1초
            }
        };
    }

    /**
     * [변경] 자동 로그인을 해야 할지 최종 판단하는 매우 중요한 메서드입니다.
     * 모든 사용자(이메일, 구글, 게스트)에 대해 일관된 자동 로그인 정책을 적용하도록 로직이 단순화되었습니다.
     * @param user 현재 Firebase에 로그인된 사용자 정보 (null일 수 있음)
     * @return 메인 화면으로 바로 이동해도 되면 true, 아니면 false
     */
    private boolean shouldAutoLogin(FirebaseUser user) {
        // [변경] 이 메서드의 로직 전체가 변경되었습니다.

        // 1단계: Firebase에 로그인된 사용자가 아예 없으면, 당연히 자동 로그인이 불가능합니다.
        if (user == null) {
            Log.d(TAG, "shouldAutoLogin: Firebase 유저가 없으므로 false");
            return false;
        }

        // 2단계: 사용자가 존재한다면, '자동 로그인' 옵션이 켜져 있는지 확인합니다.
        // 이 한 줄의 코드가 모든 사용자 유형(이메일, 구글, 게스트)에 대한 분기 처리를 대신합니다.
        // 사용자가 어떤 유형이든, '자동 로그인'을 체크했을 때만 true를 반환합니다.
        boolean isAutoLoginEnabled = AutoLoginManager.isAutoLoginEnabled(this);
        Log.d(TAG, "shouldAutoLogin: 유저 존재. 자동로그인 체크 상태 = " + isAutoLoginEnabled);
        return isAutoLoginEnabled;
    }


    /**
     * Activity가 사용자에게 보이기 시작할 때 호출됩니다. (onCreate 다음)
     */
    @Override
    protected void onStart() {
        super.onStart();
        // 화면이 다시 보일 때마다 안전 스위치를 초기화합니다.
        isUserNavigated = false;
        // 위에서 설정한 "알람 장치"를 Firebase에 등록합니다. 이제부터 로그인 상태 변경을 감지하기 시작합니다.
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    /**
     * Activity가 사용자에게 보이지 않게 될 때 호출됩니다. (다른 화면으로 이동하거나, 앱 종료 시)
     */
    @Override
    protected void onStop() {
        super.onStop();
        // "알람 장치"를 해제합니다.
        // 왜 해제할까요? 이 화면이 꺼졌는데도 계속 알람이 울리면, 배터리 낭비와 메모리 누수의 원인이 됩니다.
        // 반드시 짝을 맞춰 등록/해제를 해주어야 합니다.
        if (authStateListener != null) {
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }

    /**

     * 메인 화면으로 이동하는 책임을 맡은 작은 로봇(메서드)입니다.
     */
    private void navigateToMain() {
        Log.d(TAG, "메서드 호출: navigateToMain");
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(intent);
        // finish(): "내 임무는 끝났으니, 나 자신(스플래시 화면)을 닫아줘!"
        // 이걸 호출해야 뒤로가기 버튼을 눌렀을 때 스플래시 화면이 다시 나타나지 않습니다.
        finish();
    }

    /**
     * 로그인 화면으로 이동하는 책임을 맡은 작은 로봇(메서드)입니다.
     */
    private void navigateToLogin() {
        Log.d(TAG, "메서드 호출: navigateToLogin");
        Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
        startActivity(intent);
        finish(); // 마찬가지로, 스플래시 화면은 닫습니다.
    }
}
