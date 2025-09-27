package com.example.food_recipe.home;

import androidx.annotation.Nullable;

public class HomePresenter implements HomeContract.Presenter {

    private final HomeContract.Model model;
    private @Nullable HomeContract.View view;

    public HomePresenter(HomeContract.Model model) {
        this.model = model;
    }

    @Override
    public void attach(HomeContract.View v) {
        this.view = v;
    }

    @Override
    public void detach() {
        this.view = null;
    }

    @Override
    public void loadGreeting() {
        final HomeContract.View v0 = this.view;
        if (v0 == null) return;

        model.fetchUsername(new HomeContract.Model.UsernameCallback() {
            @Override
            public void onSuccess(@Nullable String username) {
                HomeContract.View v = view; if (v == null) return;
                if (username == null) {
                    v.showDefaultGreeting();
                } else {
                    v.showGreeting("오늘 뭐 먹을까, " + username + "님?");
                }
            }

            @Override
            public void onError(Exception e) {
                HomeContract.View v = view; if (v == null) return;
                v.showDefaultGreeting();
                // 필요 시 사용자에게 피드백 노출 (과도한 토스트 방지 위해 기본 문구 유지)
                v.showError("사용자 정보 불러오기 실패: " + e.getLocalizedMessage());
            }
        });
    }
}
