package com.example.food_recipe.main;

import com.example.food_recipe.base.BasePresenter;

/**
 * [기존 주석 유지] 메인 화면의 비즈니스 로직을 처리하는 Presenter 입니다.
 */
// [수정] BasePresenter를 상속받아 View의 생명주기를 안전하고 일관되게 관리합니다.
public class MainPresenter extends BasePresenter<MainContract.View> implements MainContract.Presenter {

    private MainContract.Model model;

    // [수정] 생성자에서 Context 주입을 제거하여 메모리 누수 위험을 방지합니다.
    public MainPresenter() {
        // Model 초기화는 View가 연결되는 시점에 안전하게 처리됩니다.
    }

    /**
     * [기존 주석 유지] 단위 테스트를 할 때 가짜(Mock) Model을 주입하기 위한 보조 생성자입니다.
     */
    public MainPresenter(MainContract.Model model) {
        this.model = model;
    }

    /**
     * [추가] View가 연결될 때 Model을 초기화합니다.
     * ApplicationContext를 사용하므로 Activity의 생명주기에 영향을 받지 않습니다.
     */
    @Override
    public void attachView(MainContract.View view) {
        super.attachView(view);
        if (model == null) {
            // ApplicationContext를 사용하여 Model을 생성함으로써 메모리 누수를 방지합니다.
            model = new MainModel(getView().getContext().getApplicationContext());
        }
    }

    @Override
    public void start() {
        model.checkLoginStatus(new MainContract.Model.LoginStatusCallback() {
            @Override
            public void onLoggedIn() {
                // [기존 로직 유지] 비어 있음
            }

            @Override
            public void onLoggedOut() {
                // [수정] isViewAttached()를 사용하여 NullPointerException을 방지합니다.
                if (isViewAttached()) {
                    getView().navigateToLogin();
                }
            }
        });
    }
}
