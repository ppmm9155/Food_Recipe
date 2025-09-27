package com.example.food_recipe.login;
import com.google.firebase.auth.FirebaseUser;     // 👉 추가
import java.util.List;

// ✅ Contract 인터페이스
// - MVP 패턴의 "약속서(Contract)" 같은 역할
// - View, Presenter, Model이 각각 어떤 메서드를 가져야 하는지 정의만 함
// - 실제 구현은 각 클래스(LoginActivity, LoginPresenter, LoginModel)에서 담당
public interface LoginContract {

    // =============================
    // 🔹 View (화면/UI 계층)
    // - Activity/Fragment가 구현
    // - Presenter가 View를 호출해서 UI를 갱신
    // =============================
    interface View {
        void showEmailError(String msg);       // 이메일 입력 오류 표시
        void showPasswordError(String msg);    // 비밀번호 입력 오류 표시
        void showWrongPassword();              // "비밀번호 틀림" 전용 처리
        void showAmbiguous();                  // 이메일/비번 모호할 때 메시지 표시
        void clearEmailError();                // 이메일 입력 오류 해제
        void clearPasswordError();             // 비밀번호 입력 오류 해제
        void toast(String msg);                // Toast 메시지 표시
        void setUiEnabled(boolean enabled);    // 버튼/입력창 활성화 or 비활성화
        void navigateToHome();                 // 로그인 성공 후 홈 화면 이동



        // ✅ 추가: 로그인 성공 시 AutoLogin 처리까지 View가 담당
        // - Presenter는 "성공했다"만 알리고, 실제 AutoLoginManager 호출은 View에서 함
        void onLoginSuccess(boolean autoLoginChecked);
    }


    // =============================
    // 🔹 Presenter (중재자/로직 계층)
    // - View에서 요청을 받아 Model을 호출
    // - Model 결과를 받아 View에게 전달
    // - UI 로직/유효성 검사/비즈니스 흐름 제어 담당
    // =============================
    interface Presenter {
        void attemptLogin(String rawEmail, String password, boolean autoLoginChecked);
        // View에서 로그인 버튼 클릭 시 호출
        // rawEmail: 사용자가 입력한 이메일(가공 전)
        // password: 입력한 비밀번호
        // autoLoginChecked: 자동 로그인 체크 여부

        boolean isAmbiguous(String code, Exception e); // Firebase 에러코드가 모호한 상황인지 판별
        void refineAmbiguousWithFetch(String email);   // 모호할 경우, fetchSignInMethods로 재확인
        // 👉 추가
        void handleGoogleLoginResult(android.content.Intent data, boolean autoLoginChecked);
        void detachView();                             // View 참조 해제 (메모리 누수 방지용)
    }

    // =============================
    // 🔹 Model (데이터 계층)
    // - FirebaseAuth 같은 외부 서비스와 직접 통신
    // - View나 Presenter를 전혀 몰라야 함 (의존X)
    // - 결과는 Callback으로 Presenter에 전달
    // =============================
    interface Model {
        //콜백 AuthCallback 인터페이스를 Contract로 승격
        interface AuthCallback {
            void onSuccess(FirebaseUser user);
            void onFailure(Exception e);
        }
        // 🔹 이메일 로그인 방식 조회 결과 콜백
        public interface FetchCallback {
            void onResult(List<String> methods);
        }
        void signInWithEmail(String email, String password, AuthCallback callback);
        // Firebase 로그인 요청

        void fetchSignInMethods(String email, FetchCallback callback);
        // Firebase에서 이메일 로그인 방식(비밀번호/구글 등) 조회
        // 👉 추가
        void signInWithGoogle(String idToken, AuthCallback callback);

    }
}
