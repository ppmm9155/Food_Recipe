package com.example.food_recipe.main;

import android.content.Context;
import android.util.Log;

import com.example.food_recipe.utils.AutoLoginManager;

public class MainModel {

    public static void logout(Context context) {
        // ✅ AutoLoginManager를 통해 일관되게 처리

        Log.d("MainModel", "MainModel.logout() 호출");
        AutoLoginManager.logout(context);

        // (필요시) 추가 로컬 캐시/DB 정리:
        // AppDatabase.getInstance(context).clearAllTables();
        // 파일 캐시 등도 여기서 정리 가능
    }
}
