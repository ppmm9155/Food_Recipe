package com.example.food_recipe.search;

import android.content.DialogInterface;
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
import java.util.Locale;

public class PantryImportBottomSheetFragment extends BottomSheetDialogFragment implements PantryImportAdapter.OnSelectionChangedListener {

    // [추가] Fragment Result API를 위한 키와 번들 키 상수. SearchFragment와 값을 맞춰야 합니다.
    public static final String REQUEST_KEY = "pantry_import_request";
    public static final String BUNDLE_KEY_SELECTED_INGREDIENTS = "selected_ingredients";
    public static final String BUNDLE_KEY_PANTRY_ITEMS = "pantry_items";
    public static final String BUNDLE_KEY_CURRENT_CHIPS = "current_chips";

    private RecyclerView recyclerView;
    private Button selectAllButton;
    private MaterialButton confirmButton;
    private PantryImportAdapter adapter;

    // [추가] 사용자가 '선택 완료' 버튼을 눌렀는지 여부를 추적하는 플래그입니다.
    private boolean isConfirmed = false;

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

        updateConfirmButtonText(0);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        Bundle args = getArguments();
        // [변경] 클래스 내부에 정의된 상수를 사용하도록 변경합니다.
        ArrayList<String> pantryItems = (args != null) ? args.getStringArrayList(BUNDLE_KEY_PANTRY_ITEMS) : new ArrayList<>();
        ArrayList<String> currentChips = (args != null) ? args.getStringArrayList(BUNDLE_KEY_CURRENT_CHIPS) : new ArrayList<>();
        adapter = new PantryImportAdapter(pantryItems, currentChips);
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
            // [추가] 사용자가 버튼을 눌렀음을 기록합니다.
            isConfirmed = true;

            Bundle result = new Bundle();
            ArrayList<String> selectedIngredients = (adapter != null) ? adapter.getSelectedItems() : new ArrayList<>();
            // [변경] 클래스 내부에 정의된 상수를 사용하도록 변경합니다.
            result.putStringArrayList(BUNDLE_KEY_SELECTED_INGREDIENTS, selectedIngredients);
            getParentFragmentManager().setFragmentResult(REQUEST_KEY, result);
            dismiss();
        });
    }

    @Override
    public void onSelectionChanged(int count) {
        updateConfirmButtonText(count);
    }

    private void updateConfirmButtonText(int count) {
        String buttonText = String.format(Locale.getDefault(), "선택 완료 (%d)", count);
        confirmButton.setText(buttonText);
    }

    // [추가] SearchFragment에서 이 BottomSheet가 닫혔을 때, 사용자가 '선택 완료'를 눌렀는지 확인할 수 있는 public 메소드를 제공합니다.
    public boolean isConfirmed() {
        return isConfirmed;
    }
}
