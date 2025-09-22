package com.example.food_recipe.main;

import android.content.Context;

public class MainModel {

    public static void logout(Context context) {
        // SharedPreferences 등 자동로그인 정보 삭제
        context.getSharedPreferences("auto_login", Context.MODE_PRIVATE)
                .edit()
                .clear()
                .apply();

        // Firebase 등 다른 서비스 로그아웃 처리 가능
        // FirebaseAuth.getInstance().signOut();
    }
}
