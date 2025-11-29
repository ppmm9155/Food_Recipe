package com.example.food_recipe.pantry;

import android.app.DatePickerDialog;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import com.example.food_recipe.R;
import com.example.food_recipe.main.AuthViewModel;
import com.example.food_recipe.model.PantryItem;
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
 * [기존 주석 유지] 재료 추가 기능을 담당하는 BottomSheet 형태의 프래그먼트입니다.
 * [변경] 재료 편집 기능이 추가되어, '추가 모드'와 '편집 모드' 두 가지로 동작합니다.
 */
public class AddIngredientBottomSheetFragment extends BottomSheetDialogFragment implements AddIngredientContract.View {

    public static final String REQUEST_KEY_INGREDIENT_ADDED = "request_key_ingredient_added";
    public static final String BUNDLE_KEY_INGREDIENT_ADDED = "bundle_key_ingredient_added";
    public static final String ARG_PANTRY_ITEM = "pantry_item_to_edit";

    private TextInputEditText etName;
    private ChipGroup chipGroupCategory;
    private TextInputEditText etQuantity;
    private Spinner spinnerUnit;
    private RadioGroup radioGroupStorage;
    private Button btnExpiration;
    private Button btnSave;
    private TextView tvTitle;

    private Calendar selectedExpirationDate;
    private AddIngredientContract.Presenter mPresenter;
    private AuthViewModel authViewModel;

    private PantryItem itemToEdit;
    private boolean isEditMode = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPresenter = new AddIngredientPresenter(PantryRepository.getInstance());

        if (getArguments() != null) {
            // [기존 주석 유지] getSerializable의 불안전한 사용 경고를 해결하기 위해 버전별로 분기 처리합니다.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                itemToEdit = getArguments().getSerializable(ARG_PANTRY_ITEM, PantryItem.class);
            } else {
                // [기존 주석 유지] 구버전 API를 사용하되, 경고를 명시적으로 무시합니다.
                itemToEdit = (PantryItem) getArguments().getSerializable(ARG_PANTRY_ITEM);
            }
            isEditMode = itemToEdit != null;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_add_ingredient, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mPresenter.attachView(this);
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        tvTitle = view.findViewById(R.id.add_ingredient_title);
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

        if (isEditMode) {
            populateUiWithData();
        }

        btnSave.setOnClickListener(v -> {
            if (authViewModel.user.getValue() == null) {
                Toast.makeText(getContext(), "로그인 후 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                dismiss();
                return;
            }

            String name = StringUtils.normalizeIngredientName(etName.getText().toString());
            String quantityStr = etQuantity.getText().toString().trim();

            // [추가] 수량이 비어있을 경우 NumberFormatException을 방지하기 위한 방어 코드
            if (quantityStr.isEmpty()) {
                showQuantityEmptyError();
                return;
            }

            String unit = spinnerUnit.getSelectedItem().toString();

            Chip selectedChip = getView().findViewById(chipGroupCategory.getCheckedChipId());
            String category = selectedChip != null ? selectedChip.getText().toString() : "기타 ✨";

            int selectedStorageId = radioGroupStorage.getCheckedRadioButtonId();
            RadioButton selectedRadioButton = getView().findViewById(selectedStorageId);
            String storage = selectedRadioButton != null ? selectedRadioButton.getText().toString() : "냉장";

            if (isEditMode) {
                itemToEdit.setName(name);
                itemToEdit.setQuantity(Double.parseDouble(quantityStr));
                itemToEdit.setUnit(unit);
                itemToEdit.setCategory(category);
                itemToEdit.setStorage(storage);
                if (selectedExpirationDate != null) {
                    itemToEdit.setExpirationDate(selectedExpirationDate.getTime());
                }
                mPresenter.updateIngredient(itemToEdit);
            } else {
                mPresenter.saveIngredient(name, quantityStr, category, unit, storage, selectedExpirationDate);
            }
        });
    }

    /**
     * [기존 주석 유지] 편집 모드일 때, 전달받은 PantryItem 데이터로 UI 필드를 채우는 메서드입니다.
     * [기존 주석 유지] SpinnerAdapter의 불안전한 캐스팅 경고를 무시하도록 어노테이션을 추가합니다.
     */
    @SuppressWarnings("unchecked")
    private void populateUiWithData() {
        tvTitle.setText("재료 편집");
        btnSave.setText("수정");

        etName.setText(itemToEdit.getName());
        etQuantity.setText(String.valueOf(itemToEdit.getQuantity()));

        for (int i = 0; i < chipGroupCategory.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupCategory.getChildAt(i);
            if (chip.getText().toString().equals(itemToEdit.getCategory())) {
                chip.setChecked(true);
                break;
            }
        }

        ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinnerUnit.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).equals(itemToEdit.getUnit())) {
                spinnerUnit.setSelection(i);
                break;
            }
        }

        switch (itemToEdit.getStorage()) {
            case "냉동":
                radioGroupStorage.check(R.id.add_ingredient_radio_frozen);
                break;
            case "실온":
                radioGroupStorage.check(R.id.add_ingredient_radio_room_temp);
                break;
            case "냉장":
            default:
                radioGroupStorage.check(R.id.add_ingredient_radio_refrigerated);
                break;
        }

        if (itemToEdit.getExpirationDate() != null) {
            selectedExpirationDate = Calendar.getInstance();
            selectedExpirationDate.setTime(itemToEdit.getExpirationDate());
            updateExpirationDateButtonText();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
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
        if (!isEditMode && chipGroupCategory.getChildCount() > 0) {
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
        if (selectedExpirationDate == null) {
            selectedExpirationDate = Calendar.getInstance();
        }
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
        String message = isEditMode ? ingredientName + " 수정 완료!" : ingredientName + " 추가 완료!";
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
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
