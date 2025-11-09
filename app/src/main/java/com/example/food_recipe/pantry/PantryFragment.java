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
import androidx.lifecycle.ViewModelProvider; // [추가]
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.food_recipe.R;
import com.example.food_recipe.adapter.PantryAdapter;
import com.example.food_recipe.main.AuthViewModel; // [추가]
import com.example.food_recipe.model.PantryItem;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
// [삭제] import com.google.firebase.auth.FirebaseAuth;
import java.util.List;

/**
 * [변경] 중앙 인증 관리(AuthViewModel) 시스템을 사용하도록 리팩토링합니다.
 * 이제 이 프래그먼트는 더 이상 자체적으로 인증 상태를 확인하지 않고,
 * MainActivity로부터 공유되는 ViewModel의 상태 변화에만 반응합니다.
 */
public class PantryFragment extends Fragment implements PantryContract.View {

    // SharedPreferences에서 스와이프 삭제 안내 다이얼로그 표시 여부를 저장하기 위한 상수
    private static final String PREFS_NAME = "PantryPrefs";
    private static final String KEY_SWIPE_TO_DELETE_SHOWN = "swipeToDeleteShown";

    // View 컴포넌트
    private RecyclerView recyclerView;
    private LinearLayout emptyView;
    private FloatingActionButton fabAdd;
    private ProgressBar progressBar;

    // MVP 및 어댑터
    private PantryContract.Presenter mPresenter;
    private PantryAdapter mAdapter;

    // [추가] 공유 ViewModel
    private AuthViewModel authViewModel;

    // [삭제] private FirebaseAuth mAuth;
    // [삭제] private FirebaseAuth.AuthStateListener mAuthListener;

    /** 스와이프 시 햅틱 피드백(진동)이 한 번만 발생하도록 제어하기 위한 플래그입니다. */
    private boolean isVibrationTriggered = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 프래그먼트의 UI 레이아웃(fragment_pantry.xml)을 인플레이트합니다.
        return inflater.inflate(R.layout.fragment_pantry, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // XML 레이아웃의 View들을 멤버 변수와 연결합니다.
        recyclerView = view.findViewById(R.id.pantry_recyclerView);
        emptyView = view.findViewById(R.id.pantry_empty_view_container);
        fabAdd = view.findViewById(R.id.pantry_fab_add);
        progressBar = view.findViewById(R.id.pantry_progressBar);

        // Presenter와 Repository를 초기화하고 연결합니다.
        mPresenter = new PantryPresenter(this, PantryRepository.getInstance());

        // [추가] Activity 범위의 AuthViewModel 인스턴스를 가져옵니다.
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        // [삭제] Firebase 인증 리스너를 설정하는 코드를 삭제합니다.
        // mAuth = FirebaseAuth.getInstance();
        // mAuthListener = firebaseAuth -> { ... };

        // RecyclerView 관련 설정을 초기화합니다.
        setupRecyclerView();

        // 재료 추가 FAB(플로팅 액션 버튼)의 클릭 리스너를 설정합니다.
        fabAdd.setOnClickListener(v -> {
            AddIngredientBottomSheetFragment bottomSheet = new AddIngredientBottomSheetFragment();
            bottomSheet.show(getParentFragmentManager(), bottomSheet.getTag());
        });

        // 다른 프래그먼트(BottomSheet)로부터 결과를 받기 위한 리스너를 설정합니다.
        setupFragmentResultListener();

        // [추가] AuthViewModel의 사용자 상태 변화를 관찰합니다.
        observeAuthState();

        // 사용자에게 스와이프 삭제 기능을 안내하는 다이얼로그를 표시합니다.
        showSwipeToDeleteHelpDialog();
    }

    /**
     * [추가] AuthViewModel의 LiveData를 관찰하여 로그인 상태 변화에 따라 UI를 업데이트합니다.
     * 이것이 중앙 인증 관리 시스템의 핵심입니다.
     */
    private void observeAuthState() {
        authViewModel.user.observe(getViewLifecycleOwner(), firebaseUser -> {
            if (firebaseUser != null) {
                // 사용자가 로그인 상태이면, 재료 목록을 불러옵니다.
                mPresenter.loadPantryItems();
                fabAdd.show(); // [추가] 로그인 시 FAB 버튼 보이기
            } else {
                // 사용자가 로그아웃 상태이면, 빈 화면을 표시하고 관련 UI를 처리합니다.
                hideLoading();
                showEmptyView(); // 빈 화면 표시
                mAdapter.setPantryItems(List.of()); // [추가] 어댑터의 데이터도 비워줍니다.
                fabAdd.hide(); // [추가] 로그아웃 시 FAB 버튼 숨기기
            }
        });
    }


    /**
     * 앱 설치 후 한 번만 스와이프 삭제 기능 안내 다이얼로그를 표시합니다.
     * SharedPreferences를 사용하여 표시 여부를 확인하고 기록합니다.
     */
    private void showSwipeToDeleteHelpDialog() {
        Context context = getContext();
        if (context == null) return;

        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean hasBeenShown = prefs.getBoolean(KEY_SWIPE_TO_DELETE_SHOWN, false);

        // 아직 표시된 적이 없다면 다이얼로그를 생성하고 보여줍니다.
        if (!hasBeenShown) {
            new AlertDialog.Builder(context)
                .setTitle("삭제 기능 안내")
                .setMessage("재료 목록을 옆으로 밀어서(스와이프) 간편하게 삭제할 수 있습니다.")
                .setPositiveButton("확인", (dialog, which) -> {
                    // "확인" 버튼 클릭 시, 다시 표시되지 않도록 SharedPreferences에 기록합니다.
                    prefs.edit().putBoolean(KEY_SWIPE_TO_DELETE_SHOWN, true).apply();
                })
                .show();
        }
    }

    /**
     * AddIngredientBottomSheetFragment에서 재료 추가가 완료되었을 때 결과를 받기 위한 리스너입니다.
     * Fragment Result API를 사용하며, 추가 성공 시 목록을 새로고침하도록 Presenter에 요청합니다.
     */
    private void setupFragmentResultListener() {
        getParentFragmentManager().setFragmentResultListener(
                AddIngredientBottomSheetFragment.REQUEST_KEY_INGREDIENT_ADDED,
                this,
                (requestKey, result) -> {
                    boolean isAdded = result.getBoolean(AddIngredientBottomSheetFragment.BUNDLE_KEY_INGREDIENT_ADDED);
                    if (isAdded) {
                        mPresenter.loadPantryItems(); // 목록 새로고침
                    }
                }
        );
    }

    /**
     * RecyclerView의 LayoutManager, Adapter, ItemTouchHelper를 설정합니다.
     * ItemTouchHelper는 스와이프 동작을 감지하고 처리하는 역할을 합니다.
     */
    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new PantryAdapter();
        recyclerView.setAdapter(mAdapter);

        // 스와이프 동작을 처리할 ItemTouchHelper를 생성하고 RecyclerView에 연결합니다.
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                // 드래그 앤 드롭 기능은 사용하지 않으므로 false를 반환합니다.
                return false;
            }

            /**
             * 아이템이 완전히 스와이프되었을 때 호출됩니다.
             * 여기서는 아이템을 즉시 삭제하지 않고, '실행 취소' 기능이 있는 스낵바를 표시합니다.
             */
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                PantryItem itemToDelete = mAdapter.getItemAt(position);

                // 1. Adapter에서 아이템을 임시로 제거하여 화면에서만 사라지게 합니다.
                mAdapter.removeItem(position);

                // 2. '실행 취소' 액션이 포함된 스낵바를 생성합니다.
                Snackbar snackbar = Snackbar.make(requireView(), "재료를 삭제했습니다.", Snackbar.LENGTH_LONG);
                snackbar.setAction("실행 취소", v -> {
                    // 사용자가 '실행 취소'를 클릭하면 Adapter에 아이템을 다시 복원합니다.
                    mAdapter.restoreItem(itemToDelete, position);
                });

                // 3. 스낵바의 상태 변화(사짐)를 감지하는 콜백을 추가합니다.
                snackbar.addCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar transientBottomBar, int event) {
                        super.onDismissed(transientBottomBar, event);
                        // 스낵바가 '실행 취소'가 아닌 다른 이유(시간 초과 등)로 사라졌을 때만
                        // Presenter에 최종 삭제를 요청합니다.
                        if (event != DISMISS_EVENT_ACTION) {
                            mPresenter.deletePantryItem(itemToDelete);
                        }
                    }
                });

                // 4. 스낵바를 표시합니다.
                snackbar.show();
            }

            /**
             * 아이템이 스와이프되는 동안 매 프레임마다 호출되어 커스텀 드로잉을 수행합니다.
             * 여기서는 배경색과 삭제 아이콘을 그립니다.
             */
            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    View itemView = viewHolder.itemView;
                    Paint paint = new Paint();

                    // 스와이프 거리에 비례하여 배경색의 투명도를 계산합니다.
                    float swipeAmount = Math.abs(dX) / (float) itemView.getWidth();
                    int alpha = Math.min(255, (int) (swipeAmount * 2 * 255));
                    paint.setColor(Color.RED);
                    paint.setAlpha(alpha);

                    // 스와이프 방향에 따라 빨간색 배경을 그립니다.
                    if (dX > 0) { // 오른쪽으로 스와이프
                        c.drawRect((float) itemView.getLeft(), (float) itemView.getTop(), dX, (float) itemView.getBottom(), paint);
                    } else { // 왼쪽으로 스와이프
                        c.drawRect((float) itemView.getRight() + dX, (float) itemView.getTop(), (float) itemView.getRight(), (float) itemView.getBottom(), paint);
                    }

                    // 삭제 아이콘을 로드하고 그립니다.
                    if (getContext() != null) {
                        Drawable icon = ContextCompat.getDrawable(getContext(), R.drawable.outline_delete_24);
                        if (icon != null) {
                            icon.setTint(Color.WHITE);
                            int iconMargin = (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
                            int iconTop = itemView.getTop() + (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
                            int iconBottom = iconTop + icon.getIntrinsicHeight();
                            int iconLeft, iconRight;
                            if (dX > 0) { // 오른쪽으로 스와이프
                                iconLeft = itemView.getLeft() + iconMargin;
                                iconRight = itemView.getLeft() + iconMargin + icon.getIntrinsicWidth();
                            } else { // 왼쪽으로 스와이프
                                iconLeft = itemView.getRight() - iconMargin - icon.getIntrinsicWidth();
                                iconRight = itemView.getRight() - iconMargin;
                            }
                            icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                            icon.draw(c);
                        }
                    }

                    // 스와이프 거리가 50%를 넘고, 아직 진동이 울리지 않았을 때 햅틱 피드백을 발생시킵니다.
                    if (swipeAmount >= 0.5f && !isVibrationTriggered) {
                        triggerVibration();
                        isVibrationTriggered = true; // 중복 진동 방지
                    }

                    // 스와이프 동작이 끝나면 진동 플래그를 리셋합니다.
                    if (swipeAmount == 0f) {
                        isVibrationTriggered = false;
                    }
                }
                // ItemTouchHelper의 기본 그리기 동작(아이템 뷰를 움직이는 등)을 마지막에 호출합니다.
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        });
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    /**
     * 햅틱 피드백(진동)을 발생시키는 메서드입니다.
     * SDK 버전에 따라 분기 처리하여 최신 방식과 이전 방식을 모두 지원합니다.
     */
    private void triggerVibration() {
        if (getContext() == null) return;

        Vibrator vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(50); // Deprecated in API 26
            }
        }
    }

    // [삭제] onStart() 메소드를 삭제합니다.
    /*
    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }
    */

    // [삭제] onStop() 메소드를 삭제합니다.
    /*
    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
    */

    /**
     * 프래그먼트의 뷰가 파괴될 때 호출됩니다.
     * Presenter와의 연결을 끊어 메모리 누수를 방지합니다.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mPresenter.detachView();
    }

    // ===== PantryContract.View 구현부 =====

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
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}
