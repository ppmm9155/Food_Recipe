package com.example.food_recipe.home;

import androidx.annotation.Nullable;

public interface HomeContract {

    interface View {
        void showGreeting(String text);
        void showDefaultGreeting();
        void showError(String message);
    }

    interface Presenter {
        void attach(View v);
        void detach();
        void loadGreeting();
    }

    interface Model {
        interface UsernameCallback {
            void onSuccess(@Nullable String username);
            void onError(Exception e);
        }
        void fetchUsername(UsernameCallback cb);
    }
}
