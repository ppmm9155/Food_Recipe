
package com.example.food_recipe.login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.food_recipe.findid.FindIdActivity;
import com.example.food_recipe.findps.FindPsActivity;
import com.example.food_recipe.join.JoinActivity;

import com.example.food_recipe.main.MainActivity;
import com.example.food_recipe.R;

import com.example.food_recipe.utils.AutoLoginManager;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import com.example.food_recipe.utils.SimpleWatcher;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.textview.MaterialTextView;

/**
 * 🎨 로그인 화면 (View)
 *
 * 이 클래스는 사용자가 보는 '로그인 화면' 그 자체를 담당합니다.
 * 사용자의 터치(클릭)를 감지하고, 입력된 텍스트를 가져오는 역할을 합니다.
 *
 * 하지만 '어떻게' 로그인 처리할지, '입력값이 올바른지' 등 복잡한 판단은 하지 않습니다.
 * 그런 똑똑한 일은 "매니저" 역할을 하는 'LoginPresenter'에게 모두 맡깁니다.
 *
 * 이 클래스는 LoginContract.View 인터페이스의 규칙을 따르겠다고 약속(implements)했습니다.
 * 그래서 Presenter가 "에러 메시지 보여줘!" 라고 하면, 그대로 보여주는 수동적인 역할만 합니다.
 */
public class LoginActivity extends AppCompatActivity implements LoginContract.View {

    // --- 뷰(View) 위젯 변수 선언 ---
    // 나중에 코드에서 사용하기 위해, XML 레이아웃에 있는 UI 요소들을 담을 그릇을 미리 만듭니다.

    // 구글 로그인 관련 도구들
    private Button btnGoogleLogin; // 구글 로그인 버튼
    private static final int RC_GOOGLE_SIGN_IN = 9001; // "구글 로그인"이라는 심부름에 붙이는 이름표(요청 코드)
    private GoogleSignInClient googleClient; // 구글 로그인 기능을 쉽게 사용하게 해주는 만능 리모컨

    // 게스트 로그인
    MaterialTextView guestLogin; // "게스트로 시작하기" 텍스트 버튼

    // 이메일, 비밀번호 입력 관련
    private TextInputLayout tilEmail, tilPassword; // 에러 메시지를 보여주는 기능이 있는 이메일/비번 입력창의 포장지
    private TextInputEditText etEmail, etPassword; // 실제 사용자가 글자를 입력하는 입력창
    private MaterialCheckBox cbAutoLogin; // "자동 로그인" 체크박스
    private Button btnLogin; // "로그인" 버튼

    // --- 로직(Logic) 처리 변수 선언 ---
    private LoginContract.Presenter presenter; // 이 화면의 모든 로직을 처리하는 "매니저(Presenter)"

    /**
     * 이 화면이 처음 만들어질 때 딱 한 번 호출되는 메소드입니다.
     * 화면에 필요한 모든 준비 작업을 여기서 합니다.
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 'Edge-to-Edge' 디자인 적용: 앱 화면이 상태바/네비게이션바 뒤까지 확장되어 더 넓어보이게 합니다.
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        // 1. 화면 설계도(XML) 연결: R.layout.activity_login 파일을 화면에 표시합니다.
        setContentView(R.layout.activity_login);

        // 2. 위젯 연결: 코드의 변수(그릇)와 XML의 UI 요소를 'id'로 연결합니다.
        tilEmail = findViewById(R.id.login_tilEmail);
        etEmail = findViewById(R.id.login_ETemail);
        tilPassword = findViewById(R.id.login_tilPassword);
        etPassword = findViewById(R.id.login_ETpassword);
        cbAutoLogin = findViewById(R.id.login_autoLoginCheckBox);
        btnLogin = findViewById(R.id.login_btn);
        btnGoogleLogin = findViewById(R.id.login_btn_googleLogin);
        //guestLogin = findViewById(R.id.login_guest);

        // Edge-to-Edge 디자인으로 인해 시스템 UI(상태바 등)와 겹치는 문제를 해결하는 코드
        View contentView = findViewById(R.id.login); // 화면의 최상위 레이아웃
        ViewCompat.setOnApplyWindowInsetsListener(contentView, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), insets.top, v.getPaddingRight(), insets.bottom);
            return WindowInsetsCompat.CONSUMED;
        });

        // 3. 매니저(Presenter) 생성: 이 화면의 로직을 담당할 매니저를 만들고, 화면(this)과 연결합니다.
        presenter = new LoginPresenter(this, new LoginModel());

        // 4. 이벤트 리스너(감시자) 설정: 사용자가 버튼을 클릭하는지 감시하고, 클릭하면 매니저에게 알립니다.
        // "로그인 버튼이 클릭되면, 입력된 이메일/비번/자동로그인 체크상태를 매니저에게 전달해줘"
        btnLogin.setOnClickListener(v -> presenter.attemptLogin(
                text(etEmail),
                text(etPassword),
                cbAutoLogin != null && cbAutoLogin.isChecked()
        ));

        // 입력창에 글자가 바뀔 때마다 매니저에게 "에러 메시지 지워줘" 라고 요청
        etEmail.addTextChangedListener(new SimpleWatcher(this::clearEmailError));
        etPassword.addTextChangedListener(new SimpleWatcher(this::clearPasswordError));

        // 5. 구글 로그인 설정: 구글 로그인을 사용하기 위한 준비 작업
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // Firebase와 안전하게 통신하기 위한 신분증(ID토큰) 요청
                .requestEmail() // 사용자 이메일 정보 요청
                .build();
        googleClient = GoogleSignIn.getClient(this, gso); // 위 설정으로 구글 로그인 리모컨 생성

        // "구글 로그인 버튼이 클릭되면, 구글 로그인 화면을 띄워줘"
        btnGoogleLogin.setOnClickListener(v -> {
            Intent signInIntent = googleClient.getSignInIntent();
            // RC_GOOGLE_SIGN_IN (9001) 이라는 이름표를 붙여서 심부름을 보냄
            startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN);
        });

        /*// "게스트 로그인 텍스트가 클릭되면, 자동로그인 체크상태를 매니저에게 전달해줘"
        guestLogin.setOnClickListener(v -> {
            presenter.attemptGusetLogin(cbAutoLogin.isChecked());
        });*/

        // 회원가입, 아이디/비밀번호 찾기 화면으로 이동하는 버튼들
        findViewById(R.id.login_joinT).setOnClickListener(v ->
                startActivity(new Intent(this, JoinActivity.class)));

        findViewById(R.id.login_Tfind_id).setOnClickListener(v ->
                startActivity(new Intent(this, FindIdActivity.class)));

        findViewById(R.id.login_Tfind_password).setOnClickListener(v ->
                startActivity(new Intent(this, FindPsActivity.class)));
    }

    /**
     * 다른 화면(예: 구글 로그인 화면)으로 보냈던 심부름의 결과가 도착했을 때 호출되는 메소드입니다.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // "내가 보냈던 심부름 이름표(9001)가 맞고, 결과물(data)이 있다면"
        if (requestCode == RC_GOOGLE_SIGN_IN && data != null) {
            boolean autoChecked = cbAutoLogin != null && cbAutoLogin.isChecked();
            // 결과물을 매니저(Presenter)에게 전달해서 처리해달라고 요청
            presenter.handleGoogleLoginResult(data, autoChecked);
        } else {
            toast("Google 로그인 취소됨");
        }
    }

    /**
     * 이 화면이 사라지기 직전에 호출됩니다.
     */
    @Override
    protected void onDestroy() {
        // 매니저(Presenter)와의 연결을 끊어서 메모리 누수를 방지합니다.
        if (presenter != null) {
            presenter.detachView();
        }
        super.onDestroy();
    }

    // ===================================================================
    // 🔹 아래부터는 LoginContract.View 인터페이스의 규칙을 실제로 구현하는 부분입니다.
    //    이 메소드들은 모두 매니저(Presenter)가 호출합니다.
    // ===================================================================

    @Override
    public void showEmailError(String msg) {
        tilEmail.setError(msg); // 이메일 입력창 포장지에 에러 메시지 표시
        etEmail.requestFocus(); // 이메일 입력창에 커서 깜빡이게 하기
    }

    @Override
    public void showPasswordError(String msg) {
        tilPassword.setError(msg);
        etPassword.requestFocus();
    }

    @Override
    public void showWrongPassword() {
        tilPassword.setError("비밀번호가 올바르지 않습니다.");
        tilPassword.setEndIconMode(TextInputLayout.END_ICON_NONE); // 비밀번호 숨김/보임 아이콘 잠시 제거
        etPassword.requestFocus();
    }

    @Override
    public void showAmbiguous() {
        tilEmail.setError("이메일 또는 비밀번호가 올바르지 않습니다.");
        etEmail.requestFocus();
    }

    @Override
    public void clearEmailError() {
        tilEmail.setError(null); // 에러 메시지 제거
    }



    @Override
    public void clearPasswordError() {
        tilPassword.setError(null);
        tilPassword.setEndIconMode(TextInputLayout.END_ICON_PASSWORD_TOGGLE); // 비밀번호 숨김/보임 아이콘 다시 보여주기
    }

    @Override
    public void toast(String msg) {
        // 화면에 잠깐 나타났다 사라지는 작은 메시지(토스트)를 띄웁니다.
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void setUiEnabled(boolean enabled) {
        // 로그인 시도 중 버튼이나 입력창을 누르지 못하도록 막거나, 끝나면 다시 풀 때 사용합니다.
        btnLogin.setEnabled(enabled);
        etEmail.setEnabled(enabled);
        etPassword.setEnabled(enabled);
        cbAutoLogin.setEnabled(enabled);
        btnLogin.setAlpha(enabled ? 1f : 0.5f); // 비활성화됐을 때 버튼을 반투명하게 만듦

        if (btnGoogleLogin != null) {
            btnGoogleLogin.setEnabled(enabled);
            btnGoogleLogin.setAlpha(enabled ? 1f : 0.5f);
        }
    }

    @Override
    public void navigateToHome() {
        // 메인 화면으로 이동합니다.
        startActivity(new Intent(this, MainActivity.class));
        finish(); // finish()를 호출해서, 메인 화면에서 뒤로가기 눌렀을 때 로그인 화면이 다시 나오지 않도록 함
    }

    /**
     * [리팩토링-변경] 이제 이메일, 구글, 게스트 로그인 모두 이 메서드를 통해 성공 처리를 합니다.
     * 주석: Presenter가 로그인 방식에 따라 AutoLoginManager에 상태 저장을 모두 처리해주므로,
     * View는 오직 성공 후의 UI 처리(플래그 해제, 토스트, 화면전환)에만 집중하면 됩니다.
     */
    @Override
    public void onLoginSuccess(boolean autoLoginChecked) {
        AutoLoginManager.clearForceReLoginOnce(this);
        Log.d("LoginFlow", "로그인 성공: auto=" + autoLoginChecked + ", force 플래그 해제됨");
        toast("로그인 되었습니다."); // [리팩토링-변경] 더 일반적인 메시지로 수정했습니다.
        navigateToHome();
    }

    /**
     * [리팩토링-삭제] onGuestLoginSuccess()
     * 주석: 이 메소드의 기능은 onLoginSuccess()로 완전히 통합되었습니다.
     */

    // 입력창(EditText)에서 글자를 편하게 가져오기 위한 작은 도우미 메소드
    private String text(TextInputEditText et) {
        return et != null && et.getText() != null ? et.getText().toString() : "";
    }

    /**
     * Presenter가 이 화면(Activity)의 Context가 필요할 때 호출하는 메소드입니다.
     * (예: SharedPreferences에 접근할 때)
     */
    @Override
    public Context getContext() {
        return this;
    }
}
