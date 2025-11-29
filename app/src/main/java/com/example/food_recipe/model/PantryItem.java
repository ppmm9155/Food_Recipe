package com.example.food_recipe.model;

import java.io.Serializable;
import java.util.Date;

/**
 * [기존 주석 유지] 냉장고(Pantry)에 보관된 개별 재료의 데이터를 표현하는 모델 클래스(Data Transfer Object, DTO)입니다.
 * [변경] Fragment 간에 객체를 전달할 수 있도록 Serializable 인터페이스를 구현합니다.
 */
public class PantryItem implements Serializable {

    /** [기존 주석 유지] Firestore 문서의 고유 ID입니다. */
    private String id;

    /** [기존 주석 유지] 재료의 이름입니다. (예: "돼지고기") */
    private String name;

    /** [기존 주석 유지] 재료의 카테고리 정보입니다. (예: "육류 🥩") */
    private String category;

    /** [기존 주석 유지] 재료의 수량입니다. (예: 500) */
    private double quantity;

    /** [기존 주석 유지] 재료의 단위입니다. (예: "g", "개") */
    private String unit;

    /** [기존 주석 유지] 재료의 보관 장소입니다. (예: "냉장", "냉동", "실온") */
    private String storage;

    /** [기존 주석 유지] 재료의 유통기한입니다. */
    private Date expirationDate;

    /**
     * [기존 주석 유지] Firestore가 데이터를 객체로 변환할 때 사용하는 기본 생성자입니다.
     */
    public PantryItem() {}

    /**
     * [기존 주석 유지] 모든 필드를 초기화하는 생성자입니다.
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

    // [기존 주석 유지] ===== Getters and Setters =====

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
