package com.example.food_recipe.pantry;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.food_recipe.R;
import com.example.food_recipe.adapter.PantryAdapter;
import com.example.food_recipe.main.AuthViewModel;
import com.example.food_recipe.model.PantryItem;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import java.util.List;

/**
 * [기존 주석 유지] 중앙 인증 관리(AuthViewModel) 시스템을 사용하도록 리팩토링합니다.
 * [변경] 재료 아이템 클릭을 처리하기 위해 PantryAdapter.OnItemClickListener를 구현합니다.
 */
public class PantryFragment extends Fragment implements PantryContract.View, PantryAdapter.OnItemClickListener {

    private static final String PREFS_NAME = "PantryPrefs";
    private static final String KEY_SWIPE_TO_DELETE_SHOWN = "swipeToDeleteShown";

    private RecyclerView recyclerView;
    private LinearLayout emptyView;
    private FloatingActionButton fabAdd;
    private ProgressBar progressBar;

    private PantryContract.Presenter mPresenter;
    private PantryAdapter mAdapter;
    private AuthViewModel authViewModel;
    private boolean isVibrationTriggered = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPresenter = new PantryPresenter(PantryRepository.getInstance());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pantry, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mPresenter.attachView(this);

        recyclerView = view.findViewById(R.id.pantry_recyclerView);
        emptyView = view.findViewById(R.id.pantry_empty_view_container);
        fabAdd = view.findViewById(R.id.pantry_fab_add);
        progressBar = view.findViewById(R.id.pantry_progressBar);

        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        setupRecyclerView();

        fabAdd.setOnClickListener(v -> {
            AddIngredientBottomSheetFragment bottomSheet = new AddIngredientBottomSheetFragment();
            bottomSheet.show(getParentFragmentManager(), bottomSheet.getTag());
        });

        setupFragmentResultListener();
        observeAuthState();
        showSwipeToDeleteHelpDialog();
    }

    private void observeAuthState() {
        authViewModel.user.observe(getViewLifecycleOwner(), firebaseUser -> {
            if (firebaseUser != null) {
                mPresenter.loadPantryItems();
                fabAdd.show();
            } else {
                hideLoading();
                showEmptyView();
                if (mAdapter != null) {
                    mAdapter.setPantryItems(List.of());
                }
                fabAdd.hide();
            }
        });
    }

    private void showSwipeToDeleteHelpDialog() {
        Context context = getContext();
        if (context == null) return;

        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean hasBeenShown = prefs.getBoolean(KEY_SWIPE_TO_DELETE_SHOWN, false);

        if (!hasBeenShown) {
            new AlertDialog.Builder(context)
                .setTitle("삭제 기능 안내")
                .setMessage("재료 목록을 옆으로 밀어서(스와이프) 간편하게 삭제할 수 있습니다.")
                .setPositiveButton("확인", (dialog, which) -> {
                    prefs.edit().putBoolean(KEY_SWIPE_TO_DELETE_SHOWN, true).apply();
                })
                .show();
        }
    }

    private void setupFragmentResultListener() {
        getParentFragmentManager().setFragmentResultListener(
                AddIngredientBottomSheetFragment.REQUEST_KEY_INGREDIENT_ADDED,
                this,
                (requestKey, result) -> {
                    boolean isAdded = result.getBoolean(AddIngredientBottomSheetFragment.BUNDLE_KEY_INGREDIENT_ADDED);
                    if (isAdded) {
                        mPresenter.loadPantryItems();
                    }
                }
        );
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new PantryAdapter();
        mAdapter.setOnItemClickListener(this); // [추가] 어댑터에 클릭 리스너를 설정합니다.
        recyclerView.setAdapter(mAdapter);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                PantryItem itemToDelete = mAdapter.getItemAt(position);
                mAdapter.removeItem(position);
                Snackbar snackbar = Snackbar.make(requireView(), "재료를 삭제했습니다.", Snackbar.LENGTH_LONG);
                snackbar.setAction("실행 취소", v -> {
                    mAdapter.restoreItem(itemToDelete, position);
                });
                snackbar.addCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar transientBottomBar, int event) {
                        super.onDismissed(transientBottomBar, event);
                        if (event != DISMISS_EVENT_ACTION) {
                            mPresenter.deletePantryItem(itemToDelete);
                        }
                    }
                });
                snackbar.show();
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                // ... (기존 onChildDraw 로직과 동일)
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        });
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    private void triggerVibration() {
        // ... (기존 triggerVibration 로직과 동일)
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mPresenter.detachView();
    }

    @Override
    public void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLoading() {
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void showPantryItems(List<PantryItem> pantryItems) {
        emptyView.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        mAdapter.setPantryItems(pantryItems);
    }

    @Override
    public void showEmptyView() {
        recyclerView.setVisibility(View.GONE);
        emptyView.setVisibility(View.VISIBLE);
    }

    @Override
    public void showError(String message) {
        if(getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * [추가] PantryAdapter.OnItemClickListener 인터페이스의 구현 메서드입니다.
     * RecyclerView의 재료 아이템이 클릭되었을 때 호출됩니다.
     * @param pantryItem 사용자가 클릭한 재료 아이템의 데이터 객체
     */
    @Override
    public void onItemClick(PantryItem pantryItem) {
        // [추가] '편집 모드'로 동작하도록, 클릭된 재료 정보를 담아 바텀 시트를 띄웁니다.
        AddIngredientBottomSheetFragment bottomSheet = new AddIngredientBottomSheetFragment();

        // [추가] 클릭된 PantryItem 객체를 전달하기 위해 Bundle을 생성합니다.
        Bundle args = new Bundle();
        args.putSerializable("pantry_item_to_edit", pantryItem); // 직렬화된 객체 저장
        bottomSheet.setArguments(args);

        // [추가] 바텀 시트를 화면에 표시합니다.
        bottomSheet.show(getParentFragmentManager(), bottomSheet.getTag());
    }
}
