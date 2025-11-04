
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

    /**
     * Activity가 처음 생성될 때 호출되는 가장 중요한 메서드입니다.
     * (앱 실행 시 가장 먼저 실행되는 코드 블록)
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // Android 12 버전부터 공식적으로 지원하는 스플래시 스크린 API를 설치합니다.
        // 이걸 호출해야 `res/values/themes.xml`에 정의한 스플래시 스타일이 적용됩니다.
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);

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

                // Handler: "나 이 작업 1초 뒤에 실행해줘" 라고 예약하는 비서입니다.
                // 왜 딜레이를 줄까요?
                // 1. 스플래시 화면이 너무 빨리 '휙'하고 사라지는 것을 방지합니다.
                // 2. 게스트 로그인처럼 Firebase가 상태를 확인하는 데 아주 약간의 시간이 걸릴 수 있는데,
                //    안정적으로 기다려주기 위함입니다.
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    // 1초 뒤에 이 코드가 실행됩니다.

                    // [변경] 로그인 판단 로직이 변경되었습니다.
                    // 이전에는 user가 null이 아니면 무조건 메인으로 갔지만,
                    // 이제는 게스트 사용자의 경우 '자동 로그인' 체크 여부를 추가로 확인합니다.
                    if (shouldAutoLogin(user)) {
                        // [변경] user 객체가 있고, 자동 로그인 조건도 만족!
                        // (이메일, 구글 사용자는 항상 통과, 게스트는 자동로그인 체크해야 통과)
                        Log.d(TAG, "상태 변경 감지: 자동 로그인 조건 만족 -> 메인으로 이동");

                        // 화면 전환 직전에, 이 Activity가 아직 살아있는지(파괴되지 않았는지) 최종 확인합니다.
                        // (앱 종료 버튼을 누르는 등 예외적인 상황에서 오류를 방지하는 안전 코드)
                        if (!isDestroyed() && !isFinishing()) {
                            isUserNavigated = true; // 안전 스위치를 켜서 중복 실행을 막습니다.
                            navigateToMain();       // 메인 화면으로 이동!
                        }
                    } else {
                        // [변경] user 객체가 없거나, 자동 로그인 조건을 만족하지 못함!
                        // (예: 게스트 사용자가 '자동 로그인'을 체크하지 않은 경우)
                        Log.d(TAG, "상태 변경 감지: 자동 로그인 조건 불만족 -> 로그인으로 이동");

                        if (!isDestroyed() && !isFinishing()) {
                            isUserNavigated = true; // 안전 스위치를 켭니다.
                            navigateToLogin();      // 로그인 화면으로 이동!
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
