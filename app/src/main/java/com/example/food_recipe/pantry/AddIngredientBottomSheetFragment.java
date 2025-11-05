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
import com.example.food_recipe.R;
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
 * 사용자는 이 화면에서 재료의 이름, 카테고리, 수량, 단위, 보관 장소, 유통기한을 입력할 수 있습니다.
 * MVP 패턴의 View 역할을 하며, 사용자의 입력을 Presenter에 전달하고 결과를 화면에 표시합니다.
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_add_ingredient, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Presenter를 초기화합니다.
        mPresenter = new AddIngredientPresenter(this, PantryRepository.getInstance());

        // XML 레이아웃의 View들을 멤버 변수와 연결합니다.
        etName = view.findViewById(R.id.add_ingredient_et_name);
        chipGroupCategory = view.findViewById(R.id.add_ingredient_chip_group_category);
        etQuantity = view.findViewById(R.id.add_ingredient_et_quantity);
        spinnerUnit = view.findViewById(R.id.add_ingredient_spinner_unit);
        radioGroupStorage = view.findViewById(R.id.add_ingredient_radio_group_storage);
        btnExpiration = view.findViewById(R.id.add_ingredient_btn_expiration);
        btnSave = view.findViewById(R.id.add_ingredient_btn_save);

        // 초기 UI 설정을 수행하는 메서드들을 호출합니다.
        setupCategoryChips();
        setupUnitSpinner();
        setupExpirationDateButton();

        // '저장' 버튼 클릭 리스너를 설정합니다.
        btnSave.setOnClickListener(v -> {
            // [변경] 사용자가 입력한 재료 이름을 StringUtils를 사용해 정규화합니다.
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

    /**
     * [변경] 재료 카테고리 Chip 생성 시, 이모지를 포함한 텍스트로 설정합니다.
     */
    private void setupCategoryChips() {
        List<String> categories = Arrays.asList("채소 🥦", "과일 🍎", "육류 🥩", "수산물 🐟", "유제품 🥛", "기타 ✨");
        for (String category : categories) {
            Chip chip = new Chip(getContext());
            chip.setText(category);
            chip.setCheckable(true);
            chipGroupCategory.addView(chip);
        }
        // 첫 번째 카테고리를 기본 선택 값으로 설정합니다.
        if (chipGroupCategory.getChildCount() > 0) {
            ((Chip) chipGroupCategory.getChildAt(0)).setChecked(true);
        }
    }
    /**
     * 재료의 단위를 선택할 수 있는 Spinner를 설정합니다.
     */
    private void setupUnitSpinner() {
        String[] units = new String[]{"g", "kg", "개", "mL", "L", "조각"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, units);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUnit.setAdapter(adapter);
    }
    /**
     * 유통기한 선택 버튼의 초기 값 설정 및 클릭 이벤트를 처리합니다.
     * 클릭 시 DatePickerDialog를 표시합니다.
     */
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
    /**
     * 선택된 유통기한 날짜를 버튼의 텍스트에 업데이트합니다.
     */
    private void updateExpirationDateButtonText() {
        String dateFormat = "yyyy-MM-dd";
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat, Locale.getDefault());
        String formattedDate = sdf.format(selectedExpirationDate.getTime());
        btnExpiration.setText("유통기한: " + formattedDate);
    }
    // ===== AddIngredientContract.View 구현부 =====
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
    /**
     * 재료 추가 성공 시, 부모 프래그먼트(PantryFragment)에 결과를 전달합니다.
     * Fragment Result API를 사용하여 데이터가 추가되었음을 알립니다.
     */
    @Override
    public void sendSuccessResult() {
        Bundle result = new Bundle();
        result.putBoolean(BUNDLE_KEY_INGREDIENT_ADDED, true);
        // 부모 FragmentManager에 정의된 키(REQUEST_KEY)로 결과(result)를 설정합니다.
        getParentFragmentManager().setFragmentResult(REQUEST_KEY_INGREDIENT_ADDED, result);
    }
}
