package com.example.food_recipe.search;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.food_recipe.model.Recipe;
import java.util.List;
import java.util.ArrayList;

/**
 * [추가] SearchFragment의 UI 상태를 저장하고 관리하는 ViewModel 클래스입니다.
 * 이 클래스는 화면 회전이나 프래그먼트 재생성 시에도 데이터를 안전하게 보존합니다.
 */
public class SearchViewModel extends ViewModel {

    // 검색 결과 레시피 목록을 저장하는 LiveData
    // 외부에서는 수정 불가능한 LiveData로 노출하고, 내부에서는 MutableLiveData로 값을 변경합니다.
    private final MutableLiveData<List<Recipe>> _searchResult = new MutableLiveData<>();
    public LiveData<List<Recipe>> searchResult = _searchResult;

    // 현재 검색어 칩 목록을 저장하는 LiveData
    private final MutableLiveData<List<String>> _searchChips = new MutableLiveData<>(new ArrayList<>());
    public LiveData<List<String>> searchChips = _searchChips;

    /**
     * Presenter가 검색 결과를 ViewModel에 저장하기 위해 호출하는 메소드입니다.
     * @param recipes 새로 검색된 레시피 목록
     */
    public void setSearchResult(List<Recipe> recipes) {
        _searchResult.setValue(recipes);
    }

    /**
     * Presenter가 검색어 칩 목록을 ViewModel에 저장하기 위해 호출하는 메소드입니다.
     * @param chips 현재 화면에 표시된 칩 목록
     */
    public void setSearchChips(List<String> chips) {
        _searchChips.setValue(chips);
    }
}
