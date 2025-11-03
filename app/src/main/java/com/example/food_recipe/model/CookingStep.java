package com.example.food_recipe.model;

import com.google.firebase.firestore.PropertyName;

/**
 * 레시피의 각 요리 단계를 나타내는 데이터 모델 클래스 (POJO - Plain Old Java Object) 입니다.
 * 이 클래스는 Firestore 문서의 데이터를 객체로 매핑하는 데 사용됩니다.
 */
public class CookingStep {

    /**
     * [규칙 수정] Firestore 필드명('step')과 매핑합니다.
     */
    @PropertyName("step")
    private long step;

    /**
     * [규칙 수정] Firestore 필드명('description')과 매핑합니다.
     */
    @PropertyName("description")
    private String description;

    /**
     * 해당 단계를 시각적으로 설명하는 이미지의 URL입니다.
     * Firestore 문서의 필드 이름 'imageUrl'과 매핑됩니다.
     */
    @PropertyName("imageUrl")
    private String imageUrl;

    /**
     * Firestore의 자동 데이터 매핑을 위한 기본 생성자입니다.
     * 객체 생성 시 Firestore 라이브러리에 의해 호출됩니다.
     */
    public CookingStep() {
        // Firestore를 위한 기본 생성자
    }

    // --- Getters: 필드 값을 외부로 반환하는 메소드 ---

    /**
     * 요리 단계 번호를 반환합니다.
     *
     * @return 단계 번호 (long 타입).
     */
    public long getStep() {
        return step;
    }

    /**
     * 요리 단계 설명을 반환합니다.
     *
     * @return 단계 설명 문자열.
     */
    public String getDescription() {
        return description;
    }

    /**
     * 요리 단계 이미지의 URL을 반환합니다.
     *
     * @return 이미지 URL 문자열.
     */
    public String getImageUrl() {
        return imageUrl;
    }

    // --- Setters: 필드 값을 외부에서 설정하는 메소드 ---

    /**
     * 요리 단계 번호를 설정합니다.
     *
     * @param step 설정할 단계 번호 (long 타입).
     */
    public void setStep(long step) {
        this.step = step;
    }

    /**
     * 요리 단계 설명을 설정합니다.
     *
     * @param description 설정할 단계 설명 문자열.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * 요리 단계 이미지의 URL을 설정합니다.
     *
     * @param imageUrl 설정할 이미지 URL 문자열.
     */
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
