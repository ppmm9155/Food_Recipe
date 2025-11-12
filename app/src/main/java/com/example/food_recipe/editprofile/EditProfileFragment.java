package com.example.food_recipe.editprofile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.example.food_recipe.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

// [변경] 불필요한 Toolbar 관련 import 문 삭제

/**
 * [기존 주석 유지] 프로필 수정 화면을 표시하는 프래그먼트. MVP 패턴의 View 역할을 합니다.
 */
public class EditProfileFragment extends Fragment implements EditProfileContract.View {

    private EditProfileContract.Presenter presenter;
    private TextInputLayout tilUsername;
    private TextInputEditText etUsername;
    private Button btnSave;
    private ProgressBar progressBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        presenter = new EditProfilePresenter();
        presenter.attachView(this);

        tilUsername = view.findViewById(R.id.til_username);
        etUsername = view.findViewById(R.id.et_username);
        btnSave = view.findViewById(R.id.btn_save);
        progressBar = view.findViewById(R.id.progress_bar);

        // [삭제] 중복 툴바를 설정하는 setupToolbar() 호출 코드를 삭제합니다.

        btnSave.setOnClickListener(v -> {
            tilUsername.setError(null);
            String newUsername = etUsername.getText().toString().trim();
            presenter.saveUsername(newUsername);
        });

        presenter.loadCurrentUsername();
    }

    // [삭제] Fragment 내에서 중복 툴바를 설정하던 setupToolbar() 메서드를 완전히 삭제합니다.

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        presenter.detachView();
    }

    // ===== View Contract 구현 =====

    @Override
    public void showCurrentUsername(String username) {
        etUsername.setText(username);
    }

    @Override
    public void showProgress() {
        progressBar.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false);
    }

    @Override
    public void hideProgress() {
        progressBar.setVisibility(View.GONE);
        btnSave.setEnabled(true);
    }

    @Override
    public void showSuccessAndClose(String message, String newUsername) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        Bundle result = new Bundle();
        result.putString("newUsername", newUsername);
        getParentFragmentManager().setFragmentResult("editProfileResult", result);
        // [변경] NavController는 이제 MainActivity의 공용 NavController를 사용하므로, popBackStack()만 호출하면 됩니다.
        NavHostFragment.findNavController(this).popBackStack();
    }

    @Override
    public void showError(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void setUsernameError(String message) {
        tilUsername.setError(message);
    }
}
