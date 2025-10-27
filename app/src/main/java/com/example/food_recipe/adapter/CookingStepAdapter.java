package com.example.food_recipe.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.food_recipe.R;
import com.example.food_recipe.model.CookingStep;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link RecyclerView}를 사용하여 요리 단계 목록을 화면에 표시하는 어댑터 클래스입니다.
 * {@link CookingStep} 객체 리스트를 데이터 소스로 사용하여 각 단계를 아이템으로 구성합니다.
 */
public class CookingStepAdapter extends RecyclerView.Adapter<CookingStepAdapter.StepViewHolder> {

    /**
     * 어댑터가 현재 화면에 표시하고 있는 요리 단계 데이터 목록입니다.
     */
    private final List<CookingStep> steps = new ArrayList<>();

    /**
     * Glide를 통한 이미지 로딩 등, Android 프레임워크 기능에 접근하기 위한 Context 객체입니다.
     */
    private final Context context;

    /**
     * CookingStepAdapter의 생성자입니다.
     *
     * @param context 어댑터의 동작(예: 이미지 로딩)에 필요한 Context.
     */
    public CookingStepAdapter(Context context) {
        this.context = context;
    }

    /**
     * RecyclerView가 새로운 ViewHolder를 필요로 할 때 호출됩니다.
     * 여기서는 `list_item_cooking_step.xml` 레이아웃을 인플레이트하여 새로운 {@link StepViewHolder}를 생성하고 반환합니다.
     *
     * @param parent   새로운 View가 추가될 부모 ViewGroup.
     * @param viewType 새로운 View의 뷰 타입.
     * @return 생성된 StepViewHolder 인스턴스.
     */
    @NonNull
    @Override
    public StepViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_cooking_step, parent, false);
        return new StepViewHolder(view);
    }

    /**
     * RecyclerView가 특정 위치(position)의 아이템을 ViewHolder에 바인딩(표시)하려고 할 때 호출됩니다.
     * `steps` 리스트에서 해당 위치의 {@link CookingStep} 객체를 가져와 ViewHolder의 `bind` 메소드를 통해 UI에 데이터를 설정합니다.
     *
     * @param holder   데이터를 표시할 ViewHolder 인스턴스.
     * @param position 데이터 목록에서의 아이템 위치.
     */
    @Override
    public void onBindViewHolder(@NonNull StepViewHolder holder, int position) {
        CookingStep step = steps.get(position);
        holder.bind(step);
    }

    /**
     * 어댑터가 관리하는 아이템의 총 개수를 반환합니다.
     *
     * @return `steps` 리스트의 크기.
     */
    @Override
    public int getItemCount() {
        return steps.size();
    }

    /**
     * 어댑터가 표시할 요리 단계 목록을 설정하거나 갱신합니다.
     * 기존 목록을 지우고 새로운 목록을 추가한 후, RecyclerView에게 데이터 세트가 변경되었음을 알려 화면을 새로 그리도록 합니다.
     *
     * @param newSteps 새로 표시할 {@link CookingStep} 객체의 리스트. null이 전달될 경우 아무 작업도 수행하지 않습니다.
     */
    public void setSteps(List<CookingStep> newSteps) {
        if (newSteps == null) return;
        this.steps.clear();
        this.steps.addAll(newSteps);
        notifyDataSetChanged();
    }

    /**
     * RecyclerView의 각 아이템 View를 보관하는 ViewHolder 클래스입니다.
     * 아이템 레이아웃에 포함된 UI 요소들에 대한 참조를 가지며, 데이터 바인딩 로직을 포함합니다.
     */
    class StepViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvStepNumber;
        private final TextView tvDescription;
        private final ImageView ivStepImage;

        /**
         * StepViewHolder의 생성자입니다.
         * 아이템 View 내의 UI 컴포넌트들을 찾아 멤버 변수에 할당합니다.
         *
         * @param itemView ViewHolder가 관리할 아이템의 루트 View.
         */
        public StepViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStepNumber = itemView.findViewById(R.id.list_step_tv_step_number);
            tvDescription = itemView.findViewById(R.id.list_step_tv_description);
            ivStepImage = itemView.findViewById(R.id.list_step_iv_image);
        }

        /**
         * {@link CookingStep} 객체로부터 데이터를 가져와 UI 컴포넌트에 설정합니다.
         *
         * @param step 화면에 표시할 요리 단계 데이터 객체.
         */
        void bind(CookingStep step) {
            tvStepNumber.setText(String.valueOf(step.getStep()));
            tvDescription.setText(step.getDescription());

            String imageUrl = step.getImageUrl();
            // 이미지 URL이 유효한 경우에만 ImageView를 보이게 하고 Glide를 통해 이미지를 로드합니다.
            if (imageUrl != null && !imageUrl.isEmpty()) {
                ivStepImage.setVisibility(View.VISIBLE);
                Glide.with(context)
                        .load(imageUrl)
                        .into(ivStepImage);
            } else {
                // 이미지 URL이 없으면 ImageView를 숨깁니다.
                ivStepImage.setVisibility(View.GONE);
            }
        }
    }
}
