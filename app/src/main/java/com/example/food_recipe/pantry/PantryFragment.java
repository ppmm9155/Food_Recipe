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
        mAdapter.setOnItemClickListener(this);
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
                // [추가] 안정성 강화: 스와이프된 아이템의 위치가 유효한지 확인합니다.
                if (position == RecyclerView.NO_POSITION) {
                    return; // 위치가 유효하지 않으면 아무 작업도 하지 않고 종료합니다.
                }
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
                // [추가] 스와이프 시각 효과(빨간 배경, 아이콘)를 그리는 로직을 복원합니다.
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    View itemView = viewHolder.itemView;
                    Paint paint = new Paint();
                    float swipeAmount = Math.abs(dX) / (float) itemView.getWidth();
                    int alpha = Math.min(255, (int) (swipeAmount * 2 * 255));
                    paint.setColor(Color.RED);
                    paint.setAlpha(alpha);
                    if (dX > 0) {
                        c.drawRect((float) itemView.getLeft(), (float) itemView.getTop(), dX, (float) itemView.getBottom(), paint);
                    } else {
                        c.drawRect((float) itemView.getRight() + dX, (float) itemView.getTop(), (float) itemView.getRight(), (float) itemView.getBottom(), paint);
                    }
                    if (getContext() != null) {
                        Drawable icon = ContextCompat.getDrawable(getContext(), R.drawable.outline_delete_24);
                        if (icon != null) {
                            icon.setTint(Color.WHITE);
                            int iconMargin = (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
                            int iconTop = itemView.getTop() + (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
                            int iconBottom = iconTop + icon.getIntrinsicHeight();
                            int iconLeft, iconRight;
                            if (dX > 0) {
                                iconLeft = itemView.getLeft() + iconMargin;
                                iconRight = itemView.getLeft() + iconMargin + icon.getIntrinsicWidth();
                            } else {
                                iconLeft = itemView.getRight() - iconMargin - icon.getIntrinsicWidth();
                                iconRight = itemView.getRight() - iconMargin;
                            }
                            icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                            icon.draw(c);
                        }
                    }
                    if (swipeAmount >= 0.5f && !isVibrationTriggered) {
                        triggerVibration();
                        isVibrationTriggered = true;
                    }
                    if (swipeAmount == 0f) {
                        isVibrationTriggered = false;
                    }
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        });
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    private void triggerVibration() {
        // [추가] 스와이프 시 진동을 발생시키는 로직을 복원합니다.
        if (getContext() == null) return;
        Vibrator vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(50);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mPresenter.detachView();
    }

    /**
     * [수정] 데이터 로딩 시작 시 호출됩니다.
     * ProgressBar를 보여주는 동시에, 콘텐츠 영역(RecyclerView와 EmptyView)을 모두 숨겨
     * 데이터 로딩 중 불필요한 UI가 깜빡이는 현상을 방지합니다.
     */
    @Override
    public void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);
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

    @Override
    public void onItemClick(PantryItem pantryItem) {
        AddIngredientBottomSheetFragment bottomSheet = new AddIngredientBottomSheetFragment();
        Bundle args = new Bundle();
        args.putSerializable("pantry_item_to_edit", pantryItem);
        bottomSheet.setArguments(args);
        bottomSheet.show(getParentFragmentManager(), bottomSheet.getTag());
    }
}
