package com.example.food_recipe.model;

import android.util.Log;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Exclude;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Recipe {
    private static final String TAG = "Recipe";

    @Exclude
    private String id;
    private String rcpSno;
    private String title;
    private String servings;
    private String ingredientsRaw;
    private List<String> ingredients;
    private String imageUrl;
    private String difficulty;
    private String cookingTime;
    private List<CookingStep> cookingSteps;
    private String categoryKind;
    private long viewCount;
    private long recommendCount;
    private long scrapCount;

    public Recipe() {
        // Firestore를 위한 기본 생성자
    }

    public static Recipe fromDocumentSnapshot(DocumentSnapshot doc) {
        Recipe recipe = new Recipe();
        try {
            recipe.setId(doc.getId());
            recipe.setRcpSno(doc.getString("RCP_SNO"));
            recipe.setTitle(doc.getString("title"));
            recipe.setServings(doc.getString("servings"));
            recipe.setIngredientsRaw(doc.getString("ingredients_raw"));
            recipe.setImageUrl(doc.getString("imageUrl"));
            recipe.setDifficulty(doc.getString("difficulty"));
            recipe.setCookingTime(doc.getString("cooking_time"));
            recipe.setCategoryKind(doc.getString("category_kind"));

            List<String> ingredientsList = (List<String>) doc.get("ingredients");
            if (ingredientsList != null) {
                recipe.setIngredients(ingredientsList);
            } else {
                recipe.setIngredients(new ArrayList<>());
            }

            Long viewCount = doc.getLong("view_count");
            recipe.setViewCount(viewCount != null ? viewCount : 0);
            Long recommendCount = doc.getLong("recommend_count");
            recipe.setRecommendCount(recommendCount != null ? recommendCount : 0);
            Long scrapCount = doc.getLong("scrap_count");
            recipe.setScrapCount(scrapCount != null ? scrapCount : 0);

            List<CookingStep> steps = new ArrayList<>();
            List<Map<String, Object>> stepsMapList = (List<Map<String, Object>>) doc.get("cooking_steps");
            if (stepsMapList != null) {
                for (Map<String, Object> stepMap : stepsMapList) {
                    CookingStep step = new CookingStep();
                    Object stepNumber = stepMap.get("step");
                    if (stepNumber instanceof Long) {
                        step.setStep((Long) stepNumber);
                    }
                    step.setDescription((String) stepMap.get("description"));
                    step.setImageUrl((String) stepMap.get("imageUrl"));
                    steps.add(step);
                }
            }
            recipe.setCookingSteps(steps);

        } catch (Exception e) {
            Log.e(TAG, "Error parsing recipe document: " + doc.getId(), e);
            if (recipe.title == null) recipe.setTitle("데이터 변환 오류");
        }
        return recipe;
    }

    // --- Getters ---

    @Exclude
    public String getId() { return id; }
    public String getRcpSno() { return rcpSno; }
    public String getTitle() { return title; }
    public String getServings() { return servings; }
    
    /**
     * [UI 개선] 재료 정보가 null일 경우 "정보 없음"을 반환하고,
     * "[재료] " 접두사가 있을 경우 이를 제거하여 반환합니다.
     */
    public String getIngredientsRaw() {
        if (ingredientsRaw == null || ingredientsRaw.isEmpty()) {
            return "정보 없음";
        }
        if (ingredientsRaw.startsWith("[재료] ")) {
            return ingredientsRaw.substring(5); // "[재료] " 다음부터의 문자열을 반환
        }
        return ingredientsRaw;
    }
    
    public List<String> getIngredients() { return ingredients; }
    public String getImageUrl() { return imageUrl; }
    public String getDifficulty() { return difficulty; }
    public String getCookingTime() {
        if (cookingTime == null || cookingTime.isEmpty()) {
            return "정보 없음";
        }
        return cookingTime;
    }
    public List<CookingStep> getCookingSteps() { return cookingSteps; }
    public String getCategoryKind() { return categoryKind; }
    public long getViewCount() { return viewCount; }
    public long getRecommendCount() { return recommendCount; }
    public long getScrapCount() { return scrapCount; }

    // --- Setters ---

    @Exclude
    public void setId(String id) { this.id = id; }
    public void setRcpSno(String rcpSno) { this.rcpSno = rcpSno; }
    public void setTitle(String title) { this.title = title; }
    public void setServings(String servings) { this.servings = servings; }
    public void setIngredientsRaw(String ingredientsRaw) { this.ingredientsRaw = ingredientsRaw; }
    public void setIngredients(List<String> ingredients) { this.ingredients = ingredients; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    public void setCookingTime(String cookingTime) { this.cookingTime = cookingTime; }
    public void setCookingSteps(List<CookingStep> cookingSteps) { this.cookingSteps = cookingSteps; }
    public void setCategoryKind(String categoryKind) { this.categoryKind = categoryKind; }
    public void setViewCount(long viewCount) { this.viewCount = viewCount; }
    public void setRecommendCount(long recommendCount) { this.recommendCount = recommendCount; }
    public void setScrapCount(long scrapCount) { this.scrapCount = scrapCount; }
}
