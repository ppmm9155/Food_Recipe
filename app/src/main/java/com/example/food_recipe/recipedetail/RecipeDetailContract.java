package com.example.food_recipe.recipedetail;

import com.example.food_recipe.model.Recipe;

/**
 * [변경] 즐겨찾기 기능 관련 인터페이스를 추가하여 계약을 확장합니다.
 */
public interface RecipeDetailContract {

    /**
     * View가 반드시 구현해야 하는 기능 목록을 정의합니다.
     */
    interface View {
        void showRecipe(Recipe recipe);
        void showLoading();
        void hideLoading();
        void showError(String message);

        /**
         * [추가] Presenter가 즐겨찾기 상태를 확인한 후, View에게 UI를 업데이트하라고 지시합니다.
         * @param isBookmarked true일 경우 '채워진 하트', false일 경우 '빈 하트'로 UI를 설정합니다.
         */
        void setBookmarkState(boolean isBookmarked);

        /**
         * [추가] 즐겨찾기 추가/삭제 작업의 결과를 사용자에게 간단한 메시지(Toast 등)로 보여줍니다.
         * @param message 표시할 메시지 (예: "즐겨찾기에 추가되었습니다.")
         */
        void showBookmarkResult(String message);
    }

    /**
     * Presenter가 반드시 구현해야 하는 기능 목록을 정의합니다.
     */
    interface Presenter {
        void loadRecipe(String rcpSno);
        void detachView();

        /**
         * [추가] View에서 즐겨찾기 아이콘이 클릭되었을 때 호출됩니다.
         * Presenter는 이 요청을 받아 Model에게 즐겨찾기 상태 변경을 지시합니다.
         */
        void onBookmarkClicked();
    }

    /**
     * [추가] Model이 반드시 구현해야 하는 기능 목록을 정의합니다.
     * 데이터 처리 로직을 담당합니다.
     */
    interface Model {
        /**
         * [추가] 특정 레시피의 상세 정보를 Firestore에서 가져옵니다.
         * @param rcpSno 조회할 레시피의 ID
         * @param callback 결과를 비동기적으로 전달받을 콜백
         */
        void getRecipeDetails(String rcpSno, OnFinishedListener<Recipe> callback);

        /**
         * [추가] 현재 사용자가 해당 레시피를 즐겨찾기 했는지 상태를 확인합니다.
         * @param recipeId 확인할 레시피의 ID
         * @param callback 결과를 비동기적으로 전달받을 콜백
         */
        void checkBookmarkState(String recipeId, OnFinishedListener<Boolean> callback);

        /**
         * [추가] 즐겨찾기 상태를 변경합니다. (추가 또는 삭제)
         * @param recipeId 상태를 변경할 레시피의 ID
         * @param callback 작업 완료 후 결과를 비동기적으로 전달받을 콜백
         */
        void toggleBookmark(String recipeId, OnFinishedListener<Boolean> callback);

        /**
         * [추가] Model의 비동기 작업 결과를 Presenter에게 전달하기 위한 범용 콜백 인터페이스입니다.
         * @param <T> 성공 시 전달받을 데이터의 타입
         */
        interface OnFinishedListener<T> {
            void onSuccess(T result);
            void onError(Exception e);
        }
    }
}
