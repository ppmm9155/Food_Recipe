package com.example.food_recipe;

import android.text.Editable;
import android.text.TextWatcher;

/**
 * TextInputLayout 에러 자동 해제를 위한 간단한 TextWatcher
 */
public class SimpleWatcher implements TextWatcher {
    private final Runnable onChange;

    public SimpleWatcher(Runnable onChange) {
        this.onChange = onChange;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        onChange.run();
    }

    @Override
    public void afterTextChanged(Editable s) {}
}
