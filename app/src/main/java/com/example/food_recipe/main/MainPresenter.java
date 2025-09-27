package com.example.food_recipe.main;

import android.content.Context;
import com.example.food_recipe.utils.AutoLoginManager; // AutoLoginManager 사용을 위해 import

import java.lang.ref.WeakReference;

/**
 * 메인 화면의 비즈니스 로직을 처리하는 Presenter 입니다.
 * MVP 패턴에서 Presenter 역할을 수행하며, MainContract.Presenter 인터페이스를 구현합니다.
 * View와 Model 사이의 '중재자' 또는 '두뇌' 역할을 담당합니다.
 * Presenter는 안드로이드 SDK(Activity, Context 등)에 대한 의존성을 최소화해야 테스트하기 좋은 코드가 됩니다.
 */
public class MainPresenter implements MainContract.Presenter {

    private final MainContract.Model model;
    private WeakReference<MainContract.View> viewRef;

    public MainPresenter(Context context) {
        this.model = new MainModel(context.getApplicationContext());
    }

    /**
     * 단위 테스트를 할 때 가짜(Mock) Model을 주입하기 위한 보조 생성자입니다.
     * @param model 테스트용으로 주입될 Model 객체
     */
    public MainPresenter(MainContract.Model model) {
        this.model = model;
    }

    @Override public void attach(MainContract.View v) { viewRef = new WeakReference<>(v); }
    @Override public void detach() { viewRef = null; }

    @Override
    public void start() {
        model.checkLoginStatus(new MainContract.Model.LoginStatusCallback() {
            @Override
            public void onLoggedIn() {
                MainContract.View v = getView();
                if (v != null) v.setLogoutEnabled(true);
            }

            @Override
            public void onLoggedOut() {
                MainContract.View v = getView();
                if (v != null) v.navigateToLogin();
            }
        });
    }


    /**
     * View가 "사용자가 로그아웃 버튼을 눌렀어!" 라고 알려주면 호출되는 메서드입니다. (변경된부분)
     */
    @Override
    public void onLogoutClicked() {
        MainContract.View v = getView();
        if (v == null) {
            // View가 없는 경우 (매우 드문 상황) 로그아웃 처리 중단
            android.util.Log.e("MainPresenter", "onLogoutClicked: View is null, cannot proceed with logout.");
            return;
        }
        // 로그아웃 처리 중에는 버튼을 비활성화하여 중복 클릭을 방지합니다.
        v.setLogoutEnabled(false);

        // View로부터 Context를 가져와 AutoLoginManager를 사용합니다.
        Context context = v.getContext();
        String loginProvider = AutoLoginManager.PROVIDER_UNKNOWN; // 기본값 설정

        if (context != null) {
            // AutoLoginManager를 통해 현재 저장된 로그인 제공자 정보를 가져옵니다.
            loginProvider = AutoLoginManager.getCurrentLoginProvider(context);
        } else {
            // Context가 null인 드문 경우에 대한 로그를 남깁니다.
            android.util.Log.w("MainPresenter", "onLogoutClicked: Context from View is null, using UNKNOWN provider.");
        }

        // Model에게 로그아웃을 요청하면서, '어떤 방식'으로 로그인했는지(loginProvider) 알려줍니다.
        model.logout(loginProvider, new MainContract.Model.LogoutCallback() {
            @Override public void onSuccess() {
                // Model이 "로그아웃 성공했어" 라고 알려주면,
                // View에게 "로그아웃 되었다는 메시지 보여주고, 로그인 화면으로 이동해" 라고 지시합니다.
                MainContract.View currentView = getView(); // 콜백 시점의 View 다시 가져오기
                if (currentView != null) {
                    currentView.showLogoutMessage("로그아웃 되었습니다");
                    currentView.navigateToLogin();
                }
            }
            @Override public void onError(Exception e) {
                // Model이 "로그아웃 실패했어" 라고 알려주면,
                // View에게 "실패 메시지 보여주고, 로그아웃 버튼 다시 활성화해" 라고 지시합니다.
                MainContract.View currentView = getView(); // 콜백 시점의 View 다시 가져오기
                if (currentView != null) {
                    currentView.setLogoutEnabled(true); // 로그아웃 실패 시 버튼 다시 활성화
                    currentView.showLogoutMessage("로그아웃 실패: " +
                            (e != null && e.getMessage() != null ? e.getMessage() : "알 수 없는 오류"));
                }
            }
        });
    }

    /**
     * WeakReference로 감싸진 View 객체를 안전하게 가져오기 위한 도우미 메서드입니다.
     * @return 현재 연결된 View 객체 (없으면 null)
     */
    private MainContract.View getView() {
        return viewRef != null ? viewRef.get() : null;
    }
}
