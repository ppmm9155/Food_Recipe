package com.example.food_recipe.home;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.food_recipe.model.Recipe;
import com.example.food_recipe.utils.RecentRecipeManager;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * [v4: 시연용 최종 안정화 버전]
 * HomeContract.Model 인터페이스의 구현체. 홈 화면의 모든 비즈니스 로직을 담당합니다.
 * - 3단계 개인화 추천 로직 (재료 -> 취향 -> 랜덤)
 * - 4순위 최종 Fallback (인기 레시피)
 * - Firestore 쿼리 제한 (10개) 핸들링 (Chunking)
 * - 네트워크 성능 최적화 (whereIn 쿼리 사용)
 * - 추천 순서 보장 (최근 본 목록)
 */
public class HomeModel implements HomeContract.Model {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final Context context;
    private static final String TAG = "HomeModel";

    /**
     * Firestore 쿼리 시 한 번에 요청할 수 있는 최대 ID 개수
     */
    private static final int FIRESTORE_QUERY_LIMIT = 10;

    public HomeModel(Context context) {
        this.context = context;
    }

    /**
     * 현재 로그인된 사용자의 'username'을 조회합니다.
     */
    @Override
    public void getUserName(OnFinishedListener<String> callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            callback.onSuccess(null);
            return;
        }
        db.collection("users").document(user.getUid()).get()
                .addOnSuccessListener(doc -> {
                    if (doc != null && doc.exists()) {
                        callback.onSuccess(doc.getString("username"));
                    } else {
                        callback.onSuccess(null);
                    }
                })
                .addOnFailureListener(callback::onError);
    }

    /**
     * '인기 레시피' (추천 수가 높은 순) 10개를 조회합니다.
     * [4순위 Fallback]으로도 사용됩니다.
     */
    @Override
    public void getPopularRecipes(OnFinishedListener<List<Recipe>> callback) {
        db.collection("recipes")
                .orderBy("recommend_count", Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Recipe> recipes = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        recipes.add(Recipe.fromDocumentSnapshot(document));
                    }
                    callback.onSuccess(recipes);
                })
                .addOnFailureListener(callback::onError);
    }

    /**
     * <h1>오늘의 추천 레시피 제공 메서드 (v4: 최종 안정화)</h1>
     * <p>사용자 상태에 따라 4단계 Fallback 로직을 수행합니다.</p>
     * <ol>
     * <li><b>1순위:</b> 냉장고 재료 기반 추천</li>
     * <li><b>2순위:</b> 즐겨찾기 카테고리(취향) 기반 추천</li>
     * <li><b>3순위:</b> 기본 랜덤 추천</li>
     * <li><b>4순위:</b> 인기 레시피 추천 (최종 안전장치)</li>
     * </ol>
     */
    @Override
    public void getRecommendedRecipes(OnFinishedListener<List<Recipe>> callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Log.d(TAG, "getRecommendedRecipes: 비로그인 상태. 3순위(랜덤) 추천을 시작합니다.");
            fetchRandomRecipes(callback, new ArrayList<>());
            return;
        }

        Log.d(TAG, "getRecommendedRecipes: 로그인 상태. 맞춤형 추천 로직을 시작합니다. (UID: " + user.getUid() + ")");
        db.collection("users").document(user.getUid()).get()
                .addOnSuccessListener(userDoc -> {
                    if (!userDoc.exists()) {
                        Log.w(TAG, "getRecommendedRecipes: 사용자 문서를 찾을 수 없습니다. 3순위(랜덤) 추천으로 전환합니다.");
                        fetchRandomRecipes(callback, new ArrayList<>());
                        return;
                    }

                    // [성능 개선] 즐겨찾기 목록을 여기서 한 번만 가져옵니다.
                    List<String> bookmarkedIds = new ArrayList<>();
                    if (userDoc.contains("bookmarked_recipes")) {
                        List<String> ids = (List<String>) userDoc.get("bookmarked_recipes");
                        if (ids != null) {
                            bookmarkedIds.addAll(ids);
                        }
                    }

                    // 1순위: 냉장고 재료 기반 추천 로직 (버그 수정 완료)
                    List<Map<String, Object>> myIngredientsData = (List<Map<String, Object>>) userDoc.get("myIngredients");
                    List<String> myIngredientNames = new ArrayList<>();
                    if (myIngredientsData != null && !myIngredientsData.isEmpty()) {
                        for (Map<String, Object> itemMap : myIngredientsData) {
                            if (itemMap.containsKey("name")) {
                                myIngredientNames.add((String) itemMap.get("name"));
                            }
                        }
                    }

                    if (!myIngredientNames.isEmpty()) {
                        Log.d(TAG, "getRecommendedRecipes: [1순위] 냉장고 재료 기반 추천을 시작합니다. (재료 " + myIngredientNames.size() + "개)");
                        fetchRecipesByIngredients(myIngredientNames, bookmarkedIds, callback);
                        return;
                    }

                    // 2순위: 즐겨찾기 카테고리 기반 추천 로직
                    if (!bookmarkedIds.isEmpty()) {
                        Log.d(TAG, "getRecommendedRecipes: [2순위] 즐겨찾기 카테고리 기반 추천을 시작합니다. (즐겨찾기 " + bookmarkedIds.size() + "개)");
                        fetchRecipesByFavoriteCategory(bookmarkedIds, callback);
                        return;
                    }

                    // 3순위: 기본 랜덤 추천
                    Log.d(TAG, "getRecommendedRecipes: [3순위] 냉장고 재료와 즐겨찾기가 모두 비어있어, 기본 랜덤 추천을 시작합니다.");
                    fetchRandomRecipes(callback, bookmarkedIds);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "getRecommendedRecipes: 사용자 문서 조회 중 오류 발생. 안전하게 3순위(랜덤) 추천으로 전환합니다.", e);
                    fetchRandomRecipes(callback, new ArrayList<>());
                });
    }

    /**
     * [수정] 1순위: 재료 기반 레시피 조회 (Firestore 10개 제한 버그 해결)
     */
    private void fetchRecipesByIngredients(List<String> ingredients, List<String> bookmarkedIds, OnFinishedListener<List<Recipe>> callback) {
        
        List<Task<QuerySnapshot>> tasks = new ArrayList<>();

        // 1. 재료 리스트를 10개씩 '청크'로 나누어 여러 개의 쿼리 Task 생성
        for (int i = 0; i < ingredients.size(); i += FIRESTORE_QUERY_LIMIT) {
            List<String> chunk = ingredients.subList(i, Math.min(i + FIRESTORE_QUERY_LIMIT, ingredients.size()));
            Log.d(TAG, "fetchRecipesByIngredients: 쿼리 청크 생성 " + (i/ FIRESTORE_QUERY_LIMIT + 1) + "/" + ((ingredients.size() + FIRESTORE_QUERY_LIMIT - 1) / FIRESTORE_QUERY_LIMIT));
            Query query = db.collection("recipes")
                    .whereArrayContainsAny("ingredients", chunk)
                    .limit(30);
            tasks.add(query.get());
        }

        // 2. 모든 쿼리 Task가 병렬로 완료될 때까지 기다림
        Log.d(TAG, "fetchRecipesByIngredients: 총 " + tasks.size() + "개의 병렬 쿼리를 실행합니다.");
        Tasks.whenAllSuccess(tasks).addOnSuccessListener(results -> {
            Log.d(TAG, "fetchRecipesByIngredients: 병렬 쿼리 성공. 결과 병합 및 필터링을 시작합니다.");
            
            List<Recipe> finalRecipes = processQueryResults(results, bookmarkedIds);
            
            // 3. Fallback 로직은 여기서 담당
            if (finalRecipes.isEmpty()) {
                Log.d(TAG, "fetchRecipesByIngredients: 1순위 추천 결과가 없습니다. 2순위(즐겨찾기) 추천으로 전환합니다.");
                fetchRecipesByFavoriteCategory(bookmarkedIds, callback); // 2순위로 전환
            } else {
                Log.d(TAG, "fetchRecipesByIngredients: 1순위 추천 최종 레시피 " + finalRecipes.size() + "개를 반환합니다.");
                callback.onSuccess(finalRecipes);
            }

        }).addOnFailureListener(e -> {
            Log.e(TAG, "fetchRecipesByIngredients: 재료 기반 쿼리 실패. 2순위(즐겨찾기) 추천으로 전환합니다.", e);
            fetchRecipesByFavoriteCategory(bookmarkedIds, callback);
        });
    }

    /**
     * [수정] 2순위: 즐겨찾기 카테고리 기반 레시피 조회 (무한 루프 버그 해결)
     */
    private void fetchRecipesByFavoriteCategory(List<String> bookmarkedIds, OnFinishedListener<List<Recipe>> callback) {
        
        // 1. 즐겨찾기한 레시피들의 상세 정보를 가져옴
        fetchRecipesByIds(bookmarkedIds, bookmarkedIds, new OnFinishedListener<List<Recipe>>() {
            @Override
            public void onSuccess(List<Recipe> bookmarkedRecipes) {
                if (bookmarkedRecipes.isEmpty()) {
                    Log.d(TAG, "fetchRecipesByFavoriteCategory: 즐겨찾기 레시피 정보가 없습니다. 3순위(랜덤) 추천으로 전환합니다.");
                    fetchRandomRecipes(callback, bookmarkedIds); // 3순위로 전환
                    return;
                }

                // 2. 가장 많이 등장한 카테고리를 찾는 로직
                Map<String, Integer> categoryCounts = new HashMap<>();
                for (Recipe recipe : bookmarkedRecipes) {
                    String category = recipe.getCategoryKind();
                    if (category != null && !category.isEmpty()) {
                        categoryCounts.put(category, categoryCounts.getOrDefault(category, 0) + 1);
                    }
                }

                if (categoryCounts.isEmpty()) {
                    Log.d(TAG, "fetchRecipesByFavoriteCategory: 유효한 카테고리를 찾지 못했습니다. 3순위(랜덤) 추천으로 전환합니다.");
                    fetchRandomRecipes(callback, bookmarkedIds); // 3순위로 전환
                    return;
                }

                // 3. 가장 빈도수가 높은 '선호 카테고리'를 찾음
                String favoriteCategory = Collections.max(categoryCounts.entrySet(), Map.Entry.comparingByValue()).getKey();
                Log.d(TAG, "fetchRecipesByFavoriteCategory: 사용자의 선호 카테고리 '" + favoriteCategory + "'를 기반으로 레시피를 검색합니다.");

                // 4. 해당 카테고리의 다른 레시피들을 검색
                db.collection("recipes")
                        .whereEqualTo("category_kind", favoriteCategory)
                        .limit(30)
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            List<Object> results = new ArrayList<>();
                            if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                                results.add(queryDocumentSnapshots);
                            }
                            
                            List<Recipe> finalRecipes = processQueryResults(results, bookmarkedIds);
                            
                            // 5. Fallback 로직은 여기서 담당
                            if (finalRecipes.isEmpty()) {
                                Log.d(TAG, "fetchRecipesByFavoriteCategory: 2순위 추천 결과가 없습니다. 3순위(랜덤) 추천으로 전환합니다.");
                                fetchRandomRecipes(callback, bookmarkedIds); // 3순위로 전환
                            } else {
                                Log.d(TAG, "fetchRecipesByFavoriteCategory: 2순위 추천 최종 레시피 " + finalRecipes.size() + "개를 반환합니다.");
                                callback.onSuccess(finalRecipes);
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "fetchRecipesByFavoriteCategory: 카테고리 기반 검색 오류. 3순위(랜덤) 추천으로 전환합니다.", e);
                            fetchRandomRecipes(callback, bookmarkedIds); // 3순위로 전환
                        });
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "fetchRecipesByFavoriteCategory: 즐겨찾기 레시피 정보 조회 오류. 3순위(랜덤) 추천으로 전환합니다.", e);
                fetchRandomRecipes(callback, bookmarkedIds); // 3순위로 전환
            }
        });
    }

    /**
     * [수정] 3순위: 기본 랜덤 레시피 조회 (최종 Fallback 추가)
     */
    private void fetchRandomRecipes(OnFinishedListener<List<Recipe>> callback, List<String> bookmarkedIds) {
        db.collection("recipes")
                .limit(100) // 랜덤성 문제는 있으나 시연용으로는 OK
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Object> results = new ArrayList<>();
                    if (queryDocumentSnapshots != null) {
                        results.add(queryDocumentSnapshots);
                    }
                    
                    List<Recipe> finalRecipes = processQueryResults(results, bookmarkedIds);
                    
                    // [수정] 3순위가 마지막이 아님. 결과 0개일 시 4순위로 전환.
                    if (!finalRecipes.isEmpty()) {
                        Log.d(TAG, "fetchRandomRecipes: 3순위 추천 최종 레시피 " + finalRecipes.size() + "개를 반환합니다.");
                        callback.onSuccess(finalRecipes);
                    } else {
                        Log.w(TAG, "fetchRandomRecipes: 3순위 추천 결과가 0개입니다. [4순위 Fallback] '인기 레시피' 추천으로 전환합니다.");
                        getPopularRecipes(callback); // 4순위(인기 레시피)로 최종 전환
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "fetchRandomRecipes: 3순위 랜덤 쿼리 실패. 4순위(인기 레시피)로 전환합니다.", e);
                    getPopularRecipes(callback); // 4순위(인기 레시피)로 최종 전환
                });
    }
    
    /**
     * [신규] 쿼리 결과 병합 및 필터링 헬퍼 (무한 루프 해결)
     * @param results 병렬 쿼리 결과 (List&lt;Object&gt; -> List&lt;QuerySnapshot&gt;)
     * @param bookmarkedIds 필터링에 사용할 즐겨찾기 ID 목록
     * @return 최종 추천 레시피 List&lt;Recipe&gt; (최대 10개)
     */
    @NonNull
    private List<Recipe> processQueryResults(@NonNull List<Object> results, @NonNull List<String> bookmarkedIds) {
        Map<String, Recipe> validRecipesMap = new HashMap<>();

        for (Object result : results) {
            QuerySnapshot snapshot = (QuerySnapshot) result;
            if (snapshot == null || snapshot.isEmpty()) {
                continue;
            }

            for (QueryDocumentSnapshot document : snapshot) {
                String docId = document.getId();
                
                // 필터링 1 (중복) + 2 (즐찾)
                if (validRecipesMap.containsKey(docId) || bookmarkedIds.contains(docId)) {
                    continue;
                }

                // 필터링 3: 재료 정보 유효성 검사
                Recipe recipe = Recipe.fromDocumentSnapshot(document);
                List<String> ingredientsList = recipe.getIngredients();
                String ingredientsRaw = document.getString("ingredients_raw");
                boolean hasIngredientsList = (ingredientsList != null && !ingredientsList.isEmpty());
                boolean hasIngredientsRaw = (ingredientsRaw != null && !ingredientsRaw.isEmpty() && !"null".equalsIgnoreCase(ingredientsRaw));

                if (hasIngredientsList || hasIngredientsRaw) {
                    validRecipesMap.put(docId, recipe);
                }
            }
        }

        // 최종 결과 셔플 및 10개 선택
        List<Recipe> validRecipes = new ArrayList<>(validRecipesMap.values());
        Collections.shuffle(validRecipes);
        return new ArrayList<>(validRecipes.subList(0, Math.min(10, validRecipes.size())));
    }

    /**
     * '최근 본/즐겨찾기' 목록 (홈 화면 중간)
     */
    @Override
    public void getRecentAndFavoriteRecipes(OnFinishedListener<List<Recipe>> callback) {
        FirebaseUser user = auth.getCurrentUser();
        List<String> recentIds = RecentRecipeManager.getRecentRecipeIds(context); // 1. 최근 본 ID (순서 중요)
        
        if (user == null) {
            // 비로그인 시: 최근 본 5개만 조회
            List<String> finalIds = recentIds.stream().limit(5).collect(Collectors.toList());
            fetchRecipesByIds(finalIds, finalIds, callback);
            return;
        }

        db.collection("users").document(user.getUid()).get()
                .addOnSuccessListener(userDoc -> {
                    List<String> favoriteIds = new ArrayList<>();
                    if (userDoc.exists() && userDoc.contains("bookmarked_recipes")) {
                        favoriteIds = (List<String>) userDoc.get("bookmarked_recipes");
                    }

                    // [수정] 순서 보장을 위해 Set이 아닌 List로 직접 병합
                    List<String> combinedIds = new ArrayList<>(recentIds);
                    if (favoriteIds != null) {
                        for (String favId : favoriteIds) {
                            if (!combinedIds.contains(favId)) {
                                combinedIds.add(favId);
                            }
                        }
                    }
                    
                    // 4. 미리보기이므로 최대 5개로 제한
                    List<String> finalIds = combinedIds.stream().limit(5).collect(Collectors.toList());

                    if (finalIds.isEmpty()) {
                        callback.onSuccess(new ArrayList<>());
                    } else {
                        // [수정] 순서 보장을 위해 원본 ID 리스트(finalIds)를 함께 전달
                        fetchRecipesByIds(finalIds, finalIds, callback);
                    }
                })
                .addOnFailureListener(e -> {
                    // 즐겨찾기 로드 실패 시, 최근 본 레시피만이라도 로드
                    List<String> finalIds = recentIds.stream().limit(5).collect(Collectors.toList());
                    fetchRecipesByIds(finalIds, finalIds, callback);
                });
    }

    /**
     * [수정] ID 목록으로 레시피 정보를 가져오는 헬퍼 메서드 (성능 및 순서 보장)
     * @param ids 조회할 ID 목록
     * @param originalOrder [신규] 최종 결과를 정렬할 기준이 되는 ID 목록
     * @param callback 결과 콜백
     */
    private void fetchRecipesByIds(List<String> ids, List<String> originalOrder, OnFinishedListener<List<Recipe>> callback) {
        if (ids == null || ids.isEmpty()) {
            callback.onSuccess(new ArrayList<>());
            return;
        }

        // [수정] Firestore 10개 제한을 해결하기 위해 병렬 청크 쿼리 사용
        List<Task<QuerySnapshot>> tasks = new ArrayList<>();
        for (int i = 0; i < ids.size(); i += FIRESTORE_QUERY_LIMIT) {
            List<String> chunk = ids.subList(i, Math.min(i + FIRESTORE_QUERY_LIMIT, ids.size()));
            // [수정] document(id).get() 대신 whereIn 사용
            tasks.add(db.collection("recipes").whereIn(FieldPath.documentId(), chunk).get());
        }

        Tasks.whenAllSuccess(tasks).addOnSuccessListener(results -> {
            Map<String, Recipe> recipeMap = new HashMap<>();
            
            for (Object snapshotObject : results) {
                QuerySnapshot snapshot = (QuerySnapshot) snapshotObject;
                for (DocumentSnapshot document : snapshot.getDocuments()) {
                    if (document.exists()) {
                        Recipe recipe = Recipe.fromDocumentSnapshot(document);
                        recipeMap.put(recipe.getId(), recipe);
                    }
                }
            }
            
            // [수정] 원본 ID 리스트(originalOrder)의 순서대로 결과를 재정렬 (위험 2 해결)
            List<Recipe> orderedRecipes = originalOrder.stream()
                    .map(recipeMap::get) // ID에 해당하는 Recipe 객체를 찾음
                    .filter(Objects::nonNull) // Map에 없는 (삭제된) 레시피는 제외
                    .collect(Collectors.toList());

            callback.onSuccess(orderedRecipes);

        }).addOnFailureListener(e -> {
            Log.e(TAG, "fetchRecipesByIds: ID 목록으로 레시피 조회 실패", e);
            callback.onError(e);
        });
    }
}
