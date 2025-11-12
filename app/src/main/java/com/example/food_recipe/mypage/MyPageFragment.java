package com.example.food_recipe.mypage;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.food_recipe.R;
import com.example.food_recipe.adapter.MyPageMenuAdapter;
import com.example.food_recipe.findps.FindPsActivity;
import com.example.food_recipe.login.LoginActivity;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseUser;
import java.util.Arrays;
import java.util.List;

/**
 * [기존 주석 유지]
 */
public class MyPageFragment extends Fragment implements MyPageContract.View {

    private MyPageContract.Presenter presenter;
    private ShapeableImageView ivUserProfile;
    private TextView tvGreeting;
    private RecyclerView mypageMenuRecyclerView;
    private final List<String> menuItems = Arrays.asList("프로필 수정", "비밀번호 변경", "로그아웃", "계정 탈퇴");

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupFragmentResultListener();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_page, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        presenter = new MyPagePresenter();
        presenter.attachView(this);

        ivUserProfile = view.findViewById(R.id.iv_user_profile);
        tvGreeting = view.findViewById(R.id.tv_greeting);
        mypageMenuRecyclerView = view.findViewById(R.id.mypage_menu_recycler_view);

        setupRecyclerView();
        presenter.loadUserData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        presenter.detachView();
    }

    private void setupRecyclerView() {
        MyPageMenuAdapter adapter = new MyPageMenuAdapter(menuItems);
        mypageMenuRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mypageMenuRecyclerView.setAdapter(adapter);
        mypageMenuRecyclerView.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));

        adapter.setOnItemClickListener(position -> {
            String selectedMenu = menuItems.get(position);
            presenter.handleMenuClick(selectedMenu);
        });
    }

    private void setupFragmentResultListener() {
        getParentFragmentManager().setFragmentResultListener("editProfileResult", this, (requestKey, bundle) -> {
            String newUsername = bundle.getString("newUsername");
            if (newUsername != null) {
                tvGreeting.setText(String.format("%s님, 안녕하세요!", newUsername));
            }
        });
    }

    @Override
    public void showUserInfo(FirebaseUser user) {
        String displayName = user.getDisplayName();
        if (displayName == null || displayName.isEmpty()) {
            tvGreeting.setText(String.format("%s님, 안녕하세요!", user.getEmail()));
        } else {
            tvGreeting.setText(String.format("%s님, 안녕하세요!", displayName));
        }

        Glide.with(this)
                .load(user.getPhotoUrl())
                .placeholder(R.drawable.ic_default_profile)
                .error(R.drawable.ic_default_profile)
                .into(ivUserProfile);
    }

    @Override
    public void showLogoutDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("로그아웃")
                .setMessage("정말 로그아웃 하시겠습니까?")
                .setPositiveButton("예", (dialog, which) -> presenter.logout())
                .setNegativeButton("아니요", null)
                .show();
    }

    @Override
    public void showDeleteAccountDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("계정 탈퇴")
                .setMessage("정말 계정을 탈퇴하시겠습니까? 모든 정보가 영구적으로 삭제됩니다.")
                .setPositiveButton("탈퇴", (dialog, which) -> presenter.deleteAccount())
                .setNegativeButton("취소", null)
                .show();
    }

    @Override
    public void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void navigateToLogin() {
        if (getActivity() == null) return;
        
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    public void navigateToFindPassword() {
        if (getActivity() != null) {
            Intent intent = new Intent(getActivity(), FindPsActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void navigateToEditProfile() {
        NavHostFragment.findNavController(this).navigate(R.id.action_myPageFragment_to_editProfileFragment);
    }
    
    /**
     * [수정] 무한 재귀 호출을 방지하기 위해 super.getContext()를 사용합니다.
     */
    @Override
    public Context getContext() {
        return super.getContext();
    }
}
