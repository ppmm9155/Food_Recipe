# 아키텍처 개선 계획: 인터페이스를 통한 역할 분리

## 1. 현황 및 문제점

현재 우리 앱은 두 개의 다른 데이터 소스(Firebase, Algolia)로부터 데이터를 받아, 하나의 `Recipe.java` 모델을 공유하여 화면에 표시하고 있다. 이 구조는 초기 개발에는 편리하지만, 프로젝트가 확장됨에 따라 다음과 같은 잠재적인 위험을 내포하고 있다.

- **역할의 모호성**: `Recipe` 객체 하나가 '검색 결과', '홈 화면 추천', '상세 정보' 등 여러 역할을 겸하고 있어, 특정 시점에 어떤 데이터가 채워져 있고 어떤 데이터가 `null`인지 예측하기 어렵다.
- **데이터 과적(Over-fetching)**: 목록 화면에서는 `title`, `imageUrl` 등 최소한의 정보만 필요함에도 불구하고, 상세 정보에나 필요한 `cooking_steps` 같은 필드까지 모델에 포함되어 있어 메모리 낭비와 혼란을 야기한다.
- **NullPointerException 위험**: 검색 결과로부터 생성된 `Recipe` 객체에서 홈 화면용 데이터인 `getRecommendCount()`를 호출하거나, 상세 화면용 데이터인 `getCookingSteps()`를 호출할 경우, 해당 필드는 `null`이므로 `NullPointerException`이 발생하여 앱이 중단될 수 있다.

## 2. 해결책: 역할(Role) 기반 아키텍처 도입

> **"실제 데이터가 어떻게 생겼든 상관없이, 우리는 각 화면의 '역할'에 맞는 '명찰(Interface)'만 보고 코딩한다."**

이 원칙에 따라, 데이터의 구체적인 구현(Concrete Class)과 화면에 필요한 역할(Interface)을 명확하게 분리하여, 코드의 안정성과 유연성을 극대화한다.

---

## 3. 상세 설계 계획

### 3.1. 1단계: 3개의 '역할(Interface)' 정의

`model` 패키지 안에, 각 화면의 역할에 맞는 3개의 인터페이스를 설계한다.

#### `ISearchResult.java` (검색 결과의 역할)
```java
// "나는 검색 결과 목록에 표시될 수 있는 존재다."
public interface ISearchResult {
    String getId();
    String getTitle();
    String getImageUrl();
}
```

#### `IHomeRecipe.java` (홈 화면 레시피의 역할)
```java
// "나는 홈 화면에 추천/정렬될 수 있는 존재다."
public interface IHomeRecipe {
    String getId(); // 상세 화면으로 넘어가기 위한 ID
    String getTitle();
    String getImageUrl();
    long getRecommendCount();
}
```

#### `IRecipeDetail.java` (상세 정보의 역할)
```java
// "나는 레시피의 모든 상세 정보를 보여줄 수 있는 존재다."
public interface IRecipeDetail {
    String getTitle();
    String getImageUrl();
    String getServings();
    String getCooking_time();
    String getDifficulty();
    List<String> getIngredients();
    List<CookingStep> getCooking_steps();
    long getView_count();
    long getRecommend_count();
    long getScrap_count();
}
```

### 3.2. 2단계: '역할'을 수행하는 '구현체(Concrete Class)' 생성

실제 데이터를 담는 클래스들은 위에서 정의한 역할을 수행하도록 `implements` 한다.

#### `Recipe.java` (목록용 모델)
이 클래스는 **'검색 결과'**와 **'홈 레시피'** 역할을 모두 수행할 수 있다.
```java
// '검색 결과'와 '홈 레시피' 역할을 모두 수행하는, 목록용 데이터 모델
public class Recipe implements ISearchResult, IHomeRecipe {
    private String id;
    private String title;
    private String imageUrl;
    private long recommendCount;

    // Getters
    @Override public String getId() { return id; }
    @Override public String getTitle() { return title; }
    @Override public String getImageUrl() { return imageUrl; }
    @Override public long getRecommendCount() { return recommendCount; }
}
```

#### `RecipeDetail.java` (상세 정보용 모델)
이 클래스는 오직 **'상세 정보'** 역할만 수행한다.
```java
// '상세 정보' 역할만 수행하는, 상세 화면 전용 데이터 모델
public class RecipeDetail implements IRecipeDetail {
    // Firestore 문서의 모든 필드를 멤버 변수로 가짐
    private String title;
    private String imageUrl;
    private String servings;
    private String cooking_time;
    private String difficulty;
    private List<String> ingredients;
    private List<CookingStep> cooking_steps;
    private long view_count;
    private long recommend_count;
    private long scrap_count;
    
    // 모든 필드에 대한 Getters 구현...
}
```

#### `CookingStep.java`
`RecipeDetail`에 포함될 조리 순서 전용 데이터 클래스이다.
```java
public class CookingStep {
    private int step;
    private String description;
    private String imageUrl;

    // Getters...
}
```

### 3.3. 3단계: '역할(Interface)' 기반으로 소통

이제부터 앱의 모든 부분(Presenter, View 등)은 구체적인 클래스(`Recipe`, `RecipeDetail`)를 직접 참조하지 않고, 오직 역할(`ISearchResult`, `IHomeRecipe` 등)만 보고 소통한다.

- **`SearchPresenter` -> `SearchFragment`**: `List<ISearchResult>`를 전달한다.
  - `SearchFragment`는 `getTitle()`, `getImageUrl()`만 호출할 수 있다. `getRecommendCount()` 호출 시 **컴파일 에러**가 발생하여 실수를 원천적으로 방지한다.

- **`HomePresenter` -> `HomeFragment`**: `List<IHomeRecipe>`를 전달한다.
  - `HomeFragment`는 `getTitle()`, `getImageUrl()`, `getRecommendCount()`를 모두 안전하게 호출할 수 있다.

- **`RecipeDetailPresenter` -> `RecipeDetailFragment`**: `IRecipeDetail`을 전달한다.
  - `RecipeDetailFragment`는 상세 정보에 필요한 모든 getter를 안전하게 호출할 수 있다.

## 4. 기대 효과

1.  **완벽한 일관성 및 안전성**: 각 화면은 자신의 역할에 필요한 데이터만 접근할 수 있어, `NullPointerException`과 같은 런타임 오류가 **컴파일 시점에 원천적으로 차단된다.**
2.  **데이터 과적 문제 해결**: `SearchFragment`는 검색에 불필요한 `recommendCount` 필드의 존재 자체를 모르게 되어, 명확한 코드 작성이 가능하다.
3.  **최고 수준의 유연성 및 유지보수성**: 향후 Algolia에만 있는 새로운 검색 관련 필드를 추가해야 할 경우, `ISearchResult` 인터페이스와 `Recipe` 클래스에만 수정하면 된다. `HomeFragment` 코드는 전혀 영향을 받지 않는다. 이것이 **느슨한 결합(Loose Coupling)**이며, 대규모 앱을 안정적으로 유지하는 핵심 비결이다.
