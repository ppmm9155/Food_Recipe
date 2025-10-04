package com.example.food_recipe.search;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.food_recipe.R;

public class SearchFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // fragment_search.xml 레이아웃을 인플레이트하여 반환합니다.
        return inflater.inflate(R.layout.fragment_search, container, false);
    }
}
