# 랜덤 추천 기능 구현 요약

이 문서는 기존의 인기 레시피 목록과 별개로, 무작위로 레시피를 추천하는 기능을 추가하기 위해 진행된 코드 변경 사항을 요약합니다.

**주요 목표:**
- '오늘의 추천 레시피' 섹션(`fmain_rv_recommended`)에 랜덤 레시피 목록 표시
  - '지금 인기 있는 레시피' 섹션(`fmain_rv_popular`)은 기존의 인기순 정렬 유지
  - MVP 아키텍처를 유지하며 기능 확장

---

## 변경된 파일 및 내용

### 1. `HomeContract.java` - 계약(Interface) 수정

Presenter, View, Model 간의 약속을 정의하는 `HomeContract`를 수정하여, 인기 레시피와 추천 레시피를 명확히 구분했습니다.

- **View 인터페이스**: 하나의 레시피 목록을 보여주던 `showRecipes`를 `showPopularRecipes`와 `showRecommendedRecipes`로 분리했습니다.
  - **Model 인터페이스**: 랜덤 추천 레시피를 가져오는 `fetchRecommendedRecipes` 메소드를 새로 추가했습니다.

```java
public interface HomeContract {

    interface View {
        void showPopularRecipes(List<Recipe> recipes); // 변경
        void showRecommendedRecipes(List<Recipe> recipes); // 추가
        // ... 기존 코드 ...
    }

    interface Presenter {
        void start();
    }

    interface Model {
        // ... 기존 코드 ...
        void fetchPopularRecipes(RecipesCallback cb);
        void fetchRecommendedRecipes(RecipesCallback cb); // 추가

        // ... 콜백 인터페이스들 ...
    }
}
```

### 2. `HomeModel.java` - 랜덤 추천 로직 구현

`fetchRecommendedRecipes` 메소드를 실제로 구현하여 랜덤 추천 로직을 추가했습니다. 

**로직 설명:**
1. Firestore `recipes` 컬렉션에서 50개의 레시피를 가져옵니다. (`limit(50)`)
   2. 가져온 레시피 리스트의 순서를 무작위로 섞습니다. (`Collections.shuffle()`)
   3. 섞인 리스트에서 상위 10개의 레시피만 선택하여 최종 결과로 반환합니다.

```java
@Override
public void fetchRecommendedRecipes(RecipesCallback cb) {
    db.collection("recipes")
            .limit(50) // 충분히 많은 레시피를 가져와서 섞습니다.
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<Recipe> recipes = new ArrayList<>();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    // Firestore 문서를 Recipe 객체로 변환
                    recipes.add(/* ... Recipe 객체 생성 ... */);
                }
                // [핵심] 레시피 목록을 무작위로 섞습니다.
                Collections.shuffle(recipes);

                // [핵심] 섞인 목록에서 10개만 선택합니다.
                List<Recipe> recommendedRecipes = new ArrayList<>(recipes.subList(0, Math.min(10, recipes.size())));

                cb.onSuccess(recommendedRecipes);
            })
            .addOnFailureListener(cb::onError);
}
```

### 3. `HomePresenter.java` - 데이터 흐름 제어

Presenter가 Model로부터 두 종류의 데이터를 모두 요청하고, 각각의 데이터를 View의 올바른 메서드에 전달하도록 수정했습니다.

- `start()` 시 `fetchPopularRecipes()` 와 `fetchRecommendedRecipes()`를 모두 호출합니다.
  - 각 Model의 콜백 결과에 따라 View의 `showPopularRecipes()` 또는 `showRecommendedRecipes()`를 호출합니다.

```java
public class HomePresenter implements HomeContract.Presenter {
    // ...
    @Override
    public void start() {
        fetchUsername();
        fetchPopularRecipes();
        fetchRecommendedRecipes(); // 추가
    }

    private void fetchPopularRecipes() {
        // ...
        model.fetchPopularRecipes(new HomeContract.Model.RecipesCallback() {
            @Override
            public void onSuccess(List<Recipe> recipes) {
                // ...
                view.showPopularRecipes(recipes); // 변경
            }
            // ...
        });
    }

    private void fetchRecommendedRecipes() { // 추가
        // ...
        model.fetchRecommendedRecipes(new HomeContract.Model.RecipesCallback() {
            @Override
            public void onSuccess(List<Recipe> recipes) {
                // ...
                view.showRecommendedRecipes(recipes);
            }
            // ...
        });
    }
}
```

### 4. `HomeFragment.java` - UI 업데이트

Presenter로부터 받은 두 종류의 레시피 리스트를 각각 올바른 `RecyclerView`에 표시하도록 `View`를 구현했습니다.

- 기존 `showRecipes` 메서드를 삭제하고 `HomeContract.View` 인터페이스에 맞게 `showPopularRecipes`와 `showRecommendedRecipes`를 구현했습니다.

```java
public class HomeFragment extends Fragment implements HomeContract.View {
    // ...
    private RecipeAdapter recommendedAdapter;
    private RecipeAdapter popularAdapter;
    // ...

    @Override
    public void showPopularRecipes(List<Recipe> recipes) {
        popularAdapter.setRecipes(recipes);
    }

    @Override
    public void showRecommendedRecipes(List<Recipe> recipes) {
        recommendedAdapter.setRecipes(recipes);
    }
    // ...
}
```
