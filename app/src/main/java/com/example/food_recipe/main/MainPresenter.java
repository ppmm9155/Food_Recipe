package com.example.food_recipe.main;

import android.content.Context;

import java.lang.ref.WeakReference;

/**
 * 메인 화면의 비즈니스 로직을 처리하는 Presenter 입니다.
 * MVP 패턴에서 Presenter 역할을 수행하며, MainContract.Presenter 인터페이스를 구현합니다.
 * View와 Model 사이의 '중재자' 또는 '두뇌' 역할을 담당합니다.
 * Presenter는 안드로이드 SDK(Activity, Context 등)에 대한 의존성을 최소화해야 테스트하기 좋은 코드가 됩니다.
 */
public class MainPresenter implements MainContract.Presenter {

    // 데이터 처리를 담당하는 Model
    private final MainContract.Model model;
    // 화면 UI를 담당하는 View (메모리 누수를 방지하기 위해 WeakReference로 감싸줍니다)
    private WeakReference<MainContract.View> viewRef;

    /**
     * Presenter가 생성될 때, 자신에게 필요한 Model을 함께 생성합니다.
     * @param context Model을 생성하는 데 필요한 Context
     */
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

    /**
     * View(Activity)가 생성될 때, Presenter에게 View가 누구인지 알려주기 위해 호출됩니다.
     * WeakReference를 사용해 View를 직접 참조하지 않음으로써, Activity가 파괴될 때 메모리 누수가 발생하는 것을 방지합니다.
     */
    @Override public void attach(MainContract.View v) { viewRef = new WeakReference<>(v); }

    /**
     * View(Activity)가 파괴될 때, View와의 연결을 끊습니다.
     */
    @Override public void detach() { viewRef = null; }

    /**
     * View가 "나 이제 화면에 보여!"라고 알려주면 호출되는 메서드입니다.
     * 앱의 시작 로직을 담당합니다. (예: 로그인 상태 확인)
     */
    @Override
    public void start() {
        // Model에게 "로그인 상태 확인해줘" 라고 요청합니다.
        model.checkLoginStatus(new MainContract.Model.LoginStatusCallback() {
            @Override
            public void onLoggedIn() {
                // Model이 "로그인 되어있어" 라고 알려주면,
                // View에게 "로그아웃 버튼 활성화해" 라고 지시합니다.
                MainContract.View v = getView();
                if (v != null) v.setLogoutEnabled(true);
            }

            @Override
            public void onLoggedOut() {
                // Model이 "로그아웃 상태야" 라고 알려주면,
                // View에게 "로그인 화면으로 이동해" 라고 지시합니다.
                MainContract.View v = getView();
                if (v != null) v.navigateToLogin();
            }
        });
    }

    /**
     * View가 "사용자가 로그아웃 버튼을 눌렀어!" 라고 알려주면 호출되는 메서드입니다.
     */
    @Override
    public void onLogoutClicked() {
        // 먼저 View에게 "로그아웃 처리 중이니까 로그아웃 버튼 비활성화해" 라고 지시합니다.
        MainContract.View v = getView();
        if (v != null) v.setLogoutEnabled(false);

        // Model에게 "로그아웃 처리해줘" 라고 요청합니다.
        model.logout(new MainContract.Model.LogoutCallback() {
            @Override public void onSuccess() {
                // Model이 "로그아웃 성공했어" 라고 알려주면,
                // View에게 "로그아웃 되었다는 메시지 보여주고, 로그인 화면으로 이동해" 라고 지시합니다.
                MainContract.View v = getView();
                if (v != null) {
                    v.showLogoutMessage("로그아웃 되었습니다");
                    v.navigateToLogin();
                }
            }
            @Override public void onError(Exception e) {
                // Model이 "로그아웃 실패했어" 라고 알려주면,
                // View에게 "실패 메시지 보여주고, 로그아웃 버튼 다시 활성화해" 라고 지시합니다.
                MainContract.View v = getView();
                if (v != null) {
                    v.setLogoutEnabled(true);
                    v.showLogoutMessage("로그아웃 실패: " +
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
