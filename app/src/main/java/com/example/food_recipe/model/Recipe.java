package com.example.food_recipe.model;

import com.google.firebase.firestore.PropertyName;
import java.util.List;

/**
 * 레시피 한 개에 대한 모든 정보를 담는 데이터 모델 클래스 (POJO) 입니다.
 * 이 클래스는 Firestore 문서의 데이터를 객체로 매핑하거나, UI 레이어에 데이터를 전달하는 데 사용됩니다.
 */
public class Recipe {

    /**
     * 레시피의 고유 식별 번호입니다.
     * Firestore 문서의 필드 이름 'RCP_SNO'와 매핑됩니다.
     */
    @PropertyName("RCP_SNO")
    private String rcpSno;

    /**
     * 레시피의 제목입니다.
     */
    @PropertyName("title")
    private String title;

    /**
     * 요리의 양 (예: "2인분").
     */
    @PropertyName("servings")
    private String servings;

    /**
     * 재료 목록을 나타내는 단일 문자열입니다. (주로 Algolia 검색 결과에서 사용)
     */
    @PropertyName("ingredients_raw")
    private String ingredientsRaw;

    /**
     * 각 재료를 문자열로 담고 있는 리스트입니다. (주로 Firestore에서 사용)
     */
    @PropertyName("ingredients")
    private List<String> ingredients;

    /**
     * 레시피의 대표 이미지 URL입니다.
     */
    @PropertyName("imageUrl")
    private String imageUrl;

    /**
     * 요리의 난이도 (예: "초급").
     */
    @PropertyName("difficulty")
    private String difficulty;

    /**
     * 예상 조리 시간 (예: "30분").
     */
    @PropertyName("cooking_time")
    private String cookingTime;

    /**
     * 요리 단계별 정보를 담고 있는 {@link CookingStep} 객체의 리스트입니다.
     */
    @PropertyName("cooking_steps")
    private List<CookingStep> cookingSteps;

    /**
     * 요리 카테고리의 종류 (예: "한식").
     */
    @PropertyName("category_kind")
    private String categoryKind;

    /**
     * 레시피의 조회수입니다.
     */
    @PropertyName("view_count")
    private long viewCount;

    /**
     * 레시피의 추천수입니다.
     */
    @PropertyName("recommend_count")
    private long recommendCount;

    /**
     * 레시피가 스크랩된 횟수입니다.
     */
    @PropertyName("scrap_count")
    private long scrapCount;

    /**
     * Firestore의 자동 데이터 매핑을 위한 기본 생성자입니다.
     */
    public Recipe() {
        // Firestore를 위한 기본 생성자
    }

    // --- Getters: 필드 값을 외부로 반환하는 메소드 ---

    public String getRcpSno() { return rcpSno; }
    public String getTitle() { return title; }
    public String getServings() { return servings; }
    public String getIngredientsRaw() { return ingredientsRaw; }
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

    // --- Setters: 필드 값을 외부에서 설정하는 메소드 ---

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
