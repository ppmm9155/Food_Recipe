package com.example.food_recipe.pantry;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import com.example.food_recipe.R;
import com.example.food_recipe.main.AuthViewModel;
import com.example.food_recipe.utils.StringUtils;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * 재료 추가 기능을 담당하는 BottomSheet 형태의 프래그먼트입니다.
 */
public class AddIngredientBottomSheetFragment extends BottomSheetDialogFragment implements AddIngredientContract.View {

    public static final String REQUEST_KEY_INGREDIENT_ADDED = "request_key_ingredient_added";
    public static final String BUNDLE_KEY_INGREDIENT_ADDED = "bundle_key_ingredient_added";

    private TextInputEditText etName;
    private ChipGroup chipGroupCategory;
    private TextInputEditText etQuantity;
    private Spinner spinnerUnit;
    private RadioGroup radioGroupStorage;
    private Button btnExpiration;
    private Button btnSave;

    private Calendar selectedExpirationDate;
    private AddIngredientContract.Presenter mPresenter;
    private AuthViewModel authViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // [추가] Presenter를 onCreate에서 생성
        mPresenter = new AddIngredientPresenter(PantryRepository.getInstance());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_add_ingredient, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // [추가] Presenter에 View를 연결
        mPresenter.attachView(this);

        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        // [삭제] Presenter 생성 로직을 onCreate로 이동

        etName = view.findViewById(R.id.add_ingredient_et_name);
        chipGroupCategory = view.findViewById(R.id.add_ingredient_chip_group_category);
        etQuantity = view.findViewById(R.id.add_ingredient_et_quantity);
        spinnerUnit = view.findViewById(R.id.add_ingredient_spinner_unit);
        radioGroupStorage = view.findViewById(R.id.add_ingredient_radio_group_storage);
        btnExpiration = view.findViewById(R.id.add_ingredient_btn_expiration);
        btnSave = view.findViewById(R.id.add_ingredient_btn_save);

        setupCategoryChips();
        setupUnitSpinner();
        setupExpirationDateButton();

        btnSave.setOnClickListener(v -> {
            if (authViewModel.user.getValue() == null) {
                Toast.makeText(getContext(), "로그인 후 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                dismiss();
                return;
            }

            String name = StringUtils.normalizeIngredientName(etName.getText().toString());
            String quantityStr = etQuantity.getText().toString().trim();
            String unit = spinnerUnit.getSelectedItem().toString();

            Chip selectedChip = getView().findViewById(chipGroupCategory.getCheckedChipId());
            String category = selectedChip != null ? selectedChip.getText().toString() : "기타 ✨";

            int selectedStorageId = radioGroupStorage.getCheckedRadioButtonId();
            RadioButton selectedRadioButton = getView().findViewById(selectedStorageId);
            String storage = selectedRadioButton != null ? selectedRadioButton.getText().toString() : "냉장";

            mPresenter.saveIngredient(name, quantityStr, category, unit, storage, selectedExpirationDate);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // [추가] Presenter와의 연결을 끊어 메모리 누수를 방지
        mPresenter.detachView();
    }

    private void setupCategoryChips() {
        List<String> categories = Arrays.asList("채소 🥦", "과일 🍎", "육류 🥩", "수산물 🐟", "유제품 🥛", "기타 ✨");
        for (String category : categories) {
            Chip chip = new Chip(getContext());
            chip.setText(category);
            chip.setCheckable(true);
            chipGroupCategory.addView(chip);
        }
        if (chipGroupCategory.getChildCount() > 0) {
            ((Chip) chipGroupCategory.getChildAt(0)).setChecked(true);
        }
    }

    private void setupUnitSpinner() {
        String[] units = new String[]{"g", "kg", "개", "mL", "L", "조각"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, units);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUnit.setAdapter(adapter);
    }

    private void setupExpirationDateButton() {
        selectedExpirationDate = Calendar.getInstance();
        updateExpirationDateButtonText();

        btnExpiration.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    getContext(),
                    (dView, year, month, dayOfMonth) -> {
                        selectedExpirationDate.set(year, month, dayOfMonth);
                        updateExpirationDateButtonText();
                    },
                    selectedExpirationDate.get(Calendar.YEAR),
                    selectedExpirationDate.get(Calendar.MONTH),
                    selectedExpirationDate.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
            datePickerDialog.show();
        });
    }

    private void updateExpirationDateButtonText() {
        String dateFormat = "yyyy-MM-dd";
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat, Locale.getDefault());
        String formattedDate = sdf.format(selectedExpirationDate.getTime());
        btnExpiration.setText("유통기한: " + formattedDate);
    }

    @Override
    public void showNameEmptyError() {
        Toast.makeText(getContext(), "재료 이름을 입력해주세요.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showQuantityEmptyError() {
        Toast.makeText(getContext(), "수량을 입력해주세요.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSaveSuccess(String ingredientName) {
        Toast.makeText(getContext(), ingredientName + " 추가 완료!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void closeBottomSheet() {
        dismiss();
    }

    @Override
    public void sendSuccessResult() {
        Bundle result = new Bundle();
        result.putBoolean(BUNDLE_KEY_INGREDIENT_ADDED, true);
        getParentFragmentManager().setFragmentResult(REQUEST_KEY_INGREDIENT_ADDED, result);
    }
}
