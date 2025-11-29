package com.example.food_recipe.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;

/**
 * [추가] SharedPreferences를 사용하여 최근 본 레시피 ID 목록을 관리하는 유틸리티 클래스입니다.
 * 단일 책임 원칙에 따라, SharedPreferences 관련 로직은 이 클래스가 전담합니다.
 */
public class RecentRecipeManager {

    private static final String PREFS_NAME = "FoodRecipePrefs";
    private static final String KEY_RECENT_RECIPES = "recent_recipe_ids";
    private static final int MAX_RECENT_LIST_SIZE = 10; // 최근 본 목록 최대 크기

    /**
     * 최근 본 레시피 ID를 목록의 가장 앞에 추가합니다.
     * - 이미 목록에 있는 ID라면 가장 앞으로 이동시킵니다.
     * - 목록의 최대 크기(MAX_RECENT_LIST_SIZE)를 초과하면 가장 오래된 항목을 제거합니다.
     *
     * @param context Context 객체
     * @param recipeId 추가할 레시피의 Firestore 문서 ID
     */
    public static void addRecentRecipe(Context context, String recipeId) {
        if (context == null || recipeId == null || recipeId.isEmpty()) {
            return;
        }

        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_RECENT_RECIPES, null);
        Gson gson = new Gson();
        Type type = new TypeToken<LinkedList<String>>() {}.getType();

        LinkedList<String> recentList = gson.fromJson(json, type);
        if (recentList == null) {
            recentList = new LinkedList<>();
        }

        // 기존에 있던 ID 제거 (중복 방지 및 순서 최신화)
        recentList.remove(recipeId);

        // 가장 앞에 ID 추가
        recentList.addFirst(recipeId);

        // 최대 크기 유지
        while (recentList.size() > MAX_RECENT_LIST_SIZE) {
            recentList.removeLast();
        }

        // SharedPreferences에 다시 저장
        SharedPreferences.Editor editor = prefs.edit();
        String updatedJson = gson.toJson(recentList);
        editor.putString(KEY_RECENT_RECIPES, updatedJson);
        editor.apply();
    }

    /**
     * SharedPreferences에 저장된 최근 본 레시피 ID 목록을 반환합니다.
     *
     * @param context Context 객체
     * @return 최근 본 레시피 ID의 List (저장된 값이 없으면 빈 리스트 반환)
     */
    public static List<String> getRecentRecipeIds(Context context) {
        if (context == null) {
            return new LinkedList<>();
        }

        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_RECENT_RECIPES, null);
        Gson gson = new Gson();
        Type type = new TypeToken<LinkedList<String>>() {}.getType();

        List<String> recentList = gson.fromJson(json, type);
        if (recentList == null) {
            return new LinkedList<>();
        }
        return recentList;
    }
}
