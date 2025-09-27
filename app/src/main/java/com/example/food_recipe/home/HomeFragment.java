package com.example.food_recipe.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.food_recipe.R;

public class HomeFragment extends Fragment implements HomeContract.View {

    private TextView title;
    private HomeContract.Presenter presenter;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View root = inflater.inflate(R.layout.fragment_main, container, false);
        title = root.findViewById(R.id.home_title);

        // 간단 DI (필요 시 Hilt로 대체 가능)
        presenter = new HomePresenter(new HomeModel());
        presenter.attach(this);
        presenter.loadGreeting();

        return root;
    }

    @Override
    public void onDestroyView() {
        if (presenter != null) presenter.detach();
        super.onDestroyView();
    }

    // === HomeContract.View 구현 ===

    @Override
    public void showGreeting(String text) {
        if (title != null) title.setText(text);
    }

    @Override
    public void showDefaultGreeting() {
        if (title != null) title.setText("오늘 뭐 먹을까?");
    }

    @Override
    public void showError(String message) {
        // 과도한 알림을 피하기 위해 로그/토스트는 선택 사항
        // Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }
}
