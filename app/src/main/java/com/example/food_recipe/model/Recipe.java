package com.example.food_recipe.model;

/**
 * [수정] 데이터 원본('파이어베이스 레시피 컬렉션.txt')에 맞춰 필드명을 수정한 최종 모델입니다.
 */
public class Recipe {

    private String id;          // Algolia objectID
    private String title;       // [수정] 'recipeName' -> 'title' (데이터 원본과 일치)
    private String ingredients;
    private String imageUrl;
    private String cookingTime;
    private long viewCount;
    private long recommendCount;

    public Recipe() {
        // Firestore용 기본 생성자
    }

    /**
     * [수정] 데이터 원본에 맞춘 최종 생성자
     */
    public Recipe(String id, String title, String ingredients, String imageUrl, String cookingTime, long viewCount, long recommendCount) {
        this.id = id;
        this.title = title;
        this.ingredients = ingredients;
        this.imageUrl = imageUrl;
        this.cookingTime = cookingTime;
        this.viewCount = viewCount;
        this.recommendCount = recommendCount;
    }

    // --- Getters ---

    public String getId() {
        return id;
    }

    public String getTitle() { // [수정] getRecipeName() -> getTitle()
        return title;
    }

    public String getIngredients() {
        return ingredients;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getCookingTime() {
        return cookingTime;
    }

    public long getViewCount() {
        return viewCount;
    }

    public long getRecommendCount() {
        return recommendCount;
    }
}
