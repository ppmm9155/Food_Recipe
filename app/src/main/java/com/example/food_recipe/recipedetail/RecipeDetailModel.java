package com.example.food_recipe.recipedetail;

import com.example.food_recipe.model.CookingStep;
import com.example.food_recipe.model.Recipe;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 레시피 상세 정보의 데이터 처리를 담당하는 모델 클래스입니다.
 * MVP 패턴에서 'Model'의 역할을 수행하며, Firestore와 직접 통신하여 특정 레시피의 상세 데이터를 가져옵니다.
 */
public class RecipeDetailModel {

    /**
     * Firestore 데이터베이스에 접근하기 위한 인스턴스입니다.
     */
    private final FirebaseFirestore db;

    /**
     * RecipeDetailModel의 생성자입니다.
     * Firestore 인스턴스를 초기화합니다.
     */
    public RecipeDetailModel() {
        db = FirebaseFirestore.getInstance();
    }

    /**
     * 데이터 조회 완료 후 Presenter에게 결과를 비동기적으로 전달하기 위한 콜백 인터페이스입니다.
     */
    public interface OnRecipeListener {
        /**
         * 데이터 조회를 성공했을 때 호출됩니다.
         *
         * @param recipe 조회된 {@link Recipe} 객체.
         */
        void onSuccess(Recipe recipe);

        /**
         * 데이터 조회 중 오류가 발생했을 때 호출됩니다.
         *
         * @param message 발생한 오류에 대한 설명 메시지.
         */
        void onError(String message);
    }

    /**
     * Firestore에서 특정 레시피의 상세 정보를 비동기적으로 가져옵니다.
     * `rcpSno`를 기준으로 'recipes' 컬렉션을 쿼리합니다.
     *
     * @param rcpSno   조회할 레시피의 고유 식별 번호.
     * @param listener 데이터 조회 결과를 전달받을 콜백 리스너.
     */
    public void getRecipe(String rcpSno, OnRecipeListener listener) {
        db.collection("recipes")
                .whereEqualTo("RCP_SNO", rcpSno)
                .limit(1) // rcpSno는 고유하므로, 1개의 결과만 가져옵니다.
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        listener.onError("레시피를 찾을 수 없습니다.");
                        return;
                    }

                    // 쿼리 결과를 순회하며 첫 번째 문서를 Recipe 객체로 변환합니다.
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Recipe recipe = new Recipe();

                        // Firestore 문서의 각 필드를 Recipe 객체에 설정합니다.
                        recipe.setRcpSno(document.getString("RCP_SNO"));
                        recipe.setTitle(document.getString("title"));
                        recipe.setImageUrl(document.getString("imageUrl"));
                        recipe.setServings(document.getString("servings"));
                        recipe.setDifficulty(document.getString("difficulty"));
                        recipe.setCookingTime(document.getString("cooking_time"));

                        // 재료 문자열에서 불필요한 "[재료]" 접두사를 제거합니다.
                        String ingredientsRaw = document.getString("ingredients_raw");
                        if (ingredientsRaw != null) {
                            ingredientsRaw = ingredientsRaw.replace("[재료]", "").trim();
                        }
                        recipe.setIngredientsRaw(ingredientsRaw);

                        // 'cooking_steps' 필드는 복잡한 구조(Map의 리스트)이므로 수동으로 파싱합니다.
                        try {
                            List<Map<String, Object>> stepsMapList = (List<Map<String, Object>>) document.get("cooking_steps");
                            if (stepsMapList != null) {
                                List<CookingStep> cookingSteps = new ArrayList<>();
                                for (Map<String, Object> stepMap : stepsMapList) {
                                    CookingStep cookingStep = new CookingStep();
                                    // Firestore는 숫자를 Long으로 저장하므로, 타입 확인 후 캐스팅합니다.
                                    Object stepNumber = stepMap.get("step");
                                    if (stepNumber instanceof Long) {
                                        cookingStep.setStep((Long) stepNumber);
                                    }
                                    cookingStep.setDescription((String) stepMap.get("description"));
                                    cookingStep.setImageUrl((String) stepMap.get("imageUrl"));
                                    cookingSteps.add(cookingStep);
                                }
                                recipe.setCookingSteps(cookingSteps);
                            }
                        } catch (Exception e) {
                            // 데이터 파싱 중 발생할 수 있는 예외를 처리하여 앱의 안정성을 높입니다.
                            // 예: ClassCastException 등
                        }

                        listener.onSuccess(recipe);
                        return; // 첫 번째 문서만 처리하고 종료합니다.
                    }
                })
                .addOnFailureListener(e -> listener.onError("데이터를 불러오는 데 실패했습니다: " + e.getMessage()));
    }
}
