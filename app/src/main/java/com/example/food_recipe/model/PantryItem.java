package com.example.food_recipe.model;

import java.util.Date;

/**
 * 냉장고(Pantry)에 보관된 개별 재료의 데이터를 표현하는 모델 클래스(Data Transfer Object, DTO)입니다.
 * 이 클래스의 객체는 Firestore 데이터베이스의 문서(document)와 매핑됩니다.
 */
public class PantryItem {

    /** Firestore 문서의 고유 ID입니다. */
    private String id;

    /** 재료의 이름입니다. (예: "돼지고기") */
    private String name;

    /** 재료의 카테고리 정보입니다. (예: "육류 🥩") */
    private String category;

    /** 재료의 수량입니다. (예: 500) */
    private double quantity;

    /** 재료의 단위입니다. (예: "g", "개") */
    private String unit;

    /** 재료의 보관 장소입니다. (예: "냉장", "냉동", "실온") */
    private String storage;

    /** 재료의 유통기한입니다. */
    private Date expirationDate;

    /**
     * Firestore가 데이터를 객체로 변환할 때 사용하는 기본 생성자입니다.
     * Firestore의 데이터 매핑을 위해 반드시 비어있는 상태로 존재해야 합니다.
     */
    public PantryItem() {}

    /**
     * 모든 필드를 초기화하는 생성자입니다.
     *
     * @param id Firestore 문서 ID
     * @param name 재료 이름
     * @param category 재료 카테고리
     * @param quantity 재료 수량
     * @param unit 재료 단위
     * @param storage 재료 보관 장소
     * @param expirationDate 재료 유통기한
     */
    public PantryItem(String id, String name, String category, double quantity, String unit, String storage, Date expirationDate) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.quantity = quantity;
        this.unit = unit;
        this.storage = storage;
        this.expirationDate = expirationDate;
    }

    // ===== Getters and Setters =====
    // 각 필드에 대한 접근자(Getter)와 설정자(Setter) 메서드들입니다.
    // 이 메서드들은 Firestore 데이터 매핑 및 앱의 다른 부분에서 데이터에 접근할 때 사용됩니다.

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getStorage() {
        return storage;
    }

    public void setStorage(String storage) {
        this.storage = storage;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }
}
