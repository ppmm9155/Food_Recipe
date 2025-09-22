package com.example.food_recipe.main;

import java.util.List;

public interface MainContract {
    interface View {
        void showLogoutMessage(String message);
        void navigateToLogin();
        void setLogoutEnabled(boolean enabled);
    }

    interface Presenter {
        void onLogoutClicked();
    }
}
