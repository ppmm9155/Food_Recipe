package com.example.food_recipe.search;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.food_recipe.R;
import com.example.food_recipe.adapter.PantryImportAdapter;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.Locale; // [추가] Locale 임포트

// --- [변경] Adapter에 새로 정의한 리스너를 구현(implements)합니다. ---
public class PantryImportBottomSheetFragment extends BottomSheetDialogFragment implements PantryImportAdapter.OnSelectionChangedListener {

    private RecyclerView recyclerView;
    private Button selectAllButton;
    private MaterialButton confirmButton;
    private PantryImportAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.Theme_App_BottomSheetDialog);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_pantry_import, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.pantry_import_recycler_view);
        selectAllButton = view.findViewById(R.id.pantry_import_btn_select_all);
        confirmButton = view.findViewById(R.id.pantry_import_btn_confirm);

        setupRecyclerView();
        setupClickListeners();

        // [추가] 초기 버튼 텍스트를 설정합니다.
        updateConfirmButtonText(0);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        Bundle args = getArguments();
        ArrayList<String> pantryItems = (args != null) ? args.getStringArrayList(SearchFragment.BUNDLE_KEY_PANTRY_ITEMS) : new ArrayList<>();
        ArrayList<String> currentChips = (args != null) ? args.getStringArrayList(SearchFragment.BUNDLE_KEY_CURRENT_CHIPS) : new ArrayList<>();
        adapter = new PantryImportAdapter(pantryItems, currentChips);
        
        // --- [추가] Fragment(this)가 Adapter의 리스너 역할을 하겠다고 설정합니다. ---
        adapter.setOnSelectionChangedListener(this);
        
        recyclerView.setAdapter(adapter);
    }

    private void setupClickListeners() {
        selectAllButton.setOnClickListener(v -> {
            if (adapter != null) {
                adapter.selectAll();
            }
        });

        confirmButton.setOnClickListener(v -> {
            Bundle result = new Bundle();
            ArrayList<String> selectedIngredients = (adapter != null) ? adapter.getSelectedItems() : new ArrayList<>();
            result.putStringArrayList(SearchFragment.BUNDLE_KEY_SELECTED_INGREDIENTS, selectedIngredients);
            getParentFragmentManager().setFragmentResult(SearchFragment.REQUEST_KEY_PANTRY_IMPORT, result);
            dismiss();
        });
    }

    /**
     * [추가] Adapter로부터 선택된 아이템 개수를 받아 버튼 텍스트를 업데이트하는 메소드입니다.
     * OnSelectionChangedListener 인터페이스의 요구사항을 구현합니다.
     * @param count 현재 선택된 재료의 총 개수
     */
    @Override
    public void onSelectionChanged(int count) {
        updateConfirmButtonText(count);
    }

    /**
     * [추가] 선택 완료 버튼의 텍스트를 업데이트하는 Helper 메소드입니다.
     * @param count 표시할 재료의 개수
     */
    private void updateConfirmButtonText(int count) {
        String buttonText = String.format(Locale.getDefault(), "선택 완료 (%d)", count);
        confirmButton.setText(buttonText);
    }
}
