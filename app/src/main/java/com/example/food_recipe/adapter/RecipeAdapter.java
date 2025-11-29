package com.example.food_recipe.adapter;

import android.content.Context;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.food_recipe.R;
import com.example.food_recipe.model.Recipe;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link RecyclerView}를 사용하여 레시피 목록을 화면에 표시하는 어댑터 클래스입니다.
 * 홈 화면, 검색 결과 화면 등 다양한 곳에서 재사용됩니다.
 * {@link Recipe} 객체 리스트를 데이터 소스로 사용하며, 각 레시피의 요약 정보를 아이템으로 구성합니다.
 */
public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    /**
     * 어댑터가 현재 화면에 표시하고 있는 레시피 데이터 목록입니다.
     */
    private final List<Recipe> recipes = new ArrayList<>();

    /**
     * Glide를 통한 이미지 로딩 등, Android 프레임워크 기능에 접근하기 위한 Context 객체입니다.
     */
    private final Context context;

    /**
     * RecyclerView 아이템의 클릭 이벤트를 처리하기 위한 리스너 인터페이스입니다.
     */
    private OnItemClickListener listener;

    /**
     * 레시피 아이템 클릭 시 호출될 콜백 인터페이스입니다.
     */
    public interface OnItemClickListener {
        /**
         * 사용자가 레시피 아이템을 클릭했을 때 호출됩니다.
         *
         * @param recipe 클릭된 {@link Recipe} 객체.
         */
        void onItemClick(Recipe recipe);
    }

    /**
     * 아이템 클릭 리스너를 외부(Fragment 등)에서 설정하기 위한 메소드입니다.
     *
     * @param listener 아이템 클릭 이벤트를 처리할 리스너 객체.
     */
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    /**
     * RecipeAdapter의 생성자입니다.
     *
     * @param context 어댑터의 동작(예: 이미지 로딩)에 필요한 Context.
     */
    public RecipeAdapter(Context context) {
        this.context = context;
    }

    /**
     * RecyclerView가 새로운 ViewHolder를 필요로 할 때 호출됩니다.
     * `list_item_recipe.xml` 레이아웃을 인플레이트하여 새로운 {@link RecipeViewHolder}를 생성하고 반환합니다.
     *
     * @param parent   새로운 View가 추가될 부모 ViewGroup.
     * @param viewType 새로운 View의 뷰 타입.
     * @return 생성된 RecipeViewHolder 인스턴스.
     */
    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_recipe, parent, false);
        return new RecipeViewHolder(view);
    }

    /**
     * RecyclerView가 특정 위치(position)의 아이템을 ViewHolder에 바인딩(표시)하려고 할 때 호출됩니다.
     * `recipes` 리스트에서 해당 위치의 {@link Recipe} 객체를 가져와 ViewHolder의 `bind` 메소드를 통해 UI에 데이터를 설정합니다.
     *
     * @param holder   데이터를 표시할 ViewHolder 인스턴스.
     * @param position 데이터 목록에서의 아이템 위치.
     */
    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        Recipe recipe = recipes.get(position);
        holder.bind(recipe, listener);
    }

    /**
     * 어댑터가 관리하는 아이템의 총 개수를 반환합니다.
     *
     * @return `recipes` 리스트의 크기.
     */
    @Override
    public int getItemCount() {
        return recipes != null ? recipes.size() : 0;
    }

    /**
     * 어댑터가 표시할 레시피 목록을 설정하거나 갱신합니다.
     * 기존 목록을 지우고 새로운 목록을 추가한 후, RecyclerView에게 데이터 세트가 변경되었음을 알려 화면을 새로 그리도록 합니다.
     *
     * @param newRecipes 새로 표시할 {@link Recipe} 객체의 리스트.
     */
    public void setRecipes(List<Recipe> newRecipes) {
        this.recipes.clear();
        if (newRecipes != null) {
            this.recipes.addAll(newRecipes);
        }
        notifyDataSetChanged();
    }

    /**
     * RecyclerView의 각 아이템 View를 보관하는 ViewHolder 클래스입니다.
     * 아이템 레이아웃에 포함된 UI 요소들에 대한 참조를 가지며, 데이터 바인딩 및 이벤트 처리 로직을 포함합니다.
     */
    public class RecipeViewHolder extends RecyclerView.ViewHolder {

        private final ImageView ivRecipeImage;
        private final TextView tvRecipeTitle;
        private final TextView tvCookingTime;
        private final TextView tvIngredients;

        /**
         * RecipeViewHolder의 생성자입니다.
         * 아이템 View 내의 UI 컴포넌트들을 찾아 멤버 변수에 할당합니다.
         *
         * @param itemView ViewHolder가 관리할 아이템의 루트 View.
         */
        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            ivRecipeImage = itemView.findViewById(R.id.iv_recipe_image);
            tvRecipeTitle = itemView.findViewById(R.id.tv_recipe_title);
            tvCookingTime = itemView.findViewById(R.id.tv_cooking_time);
            tvIngredients = itemView.findViewById(R.id.tv_ingredients);
        }

        /**
         * {@link Recipe} 객체로부터 데이터를 가져와 UI 컴포넌트에 설정하고, 클릭 리스너를 바인딩합니다.
         *
         * @param recipe   화면에 표시할 레시피 데이터 객체.
         * @param listener 아이템 클릭 시 호출될 리스너.
         */
        public void bind(final Recipe recipe, final OnItemClickListener listener) {
            Glide.with(context)
                    .load(recipe.getImageUrl())
                    .into(ivRecipeImage);

            // [변경] 하이라이팅 태그(<b>)를 처리하기 위해 HtmlCompat을 사용합니다.
            String titleWithTags = recipe.getTitle();
            if (titleWithTags != null) {
                Spannable titleSpannable = (Spannable) HtmlCompat.fromHtml(titleWithTags, HtmlCompat.FROM_HTML_MODE_LEGACY);

                // [추가] Spannable 텍스트 내의 모든 StyleSpan(굵은 글씨)을 찾아 색상을 변경합니다.
                StyleSpan[] styleSpans = titleSpannable.getSpans(0, titleSpannable.length(), StyleSpan.class);
                for (StyleSpan span : styleSpans) {
                    if (span.getStyle() == android.graphics.Typeface.BOLD) {
                        int start = titleSpannable.getSpanStart(span);
                        int end = titleSpannable.getSpanEnd(span);
                        // [변경] 기존의 StyleSpan은 제거하고 (제목 전체가 이미 Bold이므로), 색상 Span을 적용합니다.
                        titleSpannable.removeSpan(span);
                        titleSpannable.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.search_highlight_color)),
                                start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                }
                tvRecipeTitle.setText(titleSpannable);
            } else {
                tvRecipeTitle.setText(""); // 제목이 null일 경우 빈 문자열로 처리
            }

            // [변경] cookingTime이 null이거나 유효하지 않은 경우를 대비하여 안정성을 강화합니다.
            String cookingTime = recipe.getCookingTime();
            if (isValidString(cookingTime)) {
                tvCookingTime.setText("조리 시간: " + cookingTime);
            } else {
                tvCookingTime.setText("조리 시간: 정보 없음");
            }

            // [변경] 재료 부분도 하이라이팅 태그(<b>)를 처리하도록 수정합니다.
            String ingredientsRaw = recipe.getIngredientsRaw();
            if (isValidString(ingredientsRaw)) {
                Spannable ingredientsSpannable = (Spannable) HtmlCompat.fromHtml("재료: " + ingredientsRaw, HtmlCompat.FROM_HTML_MODE_LEGACY);
                tvIngredients.setText(ingredientsSpannable);
            } else {
                List<String> ingredientsList = recipe.getIngredients();
                if (ingredientsList != null && !ingredientsList.isEmpty()) {
                    tvIngredients.setText("재료: " + TextUtils.join(", ", ingredientsList));
                } else {
                    tvIngredients.setText("재료: 정보 없음");
                }
            }

            // 아이템 View 전체에 대한 클릭 리스너 설정
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    // RecyclerView.NO_POSITION 체크를 통해 유효한 위치의 아이템만 처리
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(recipes.get(position));
                    }
                }
            });
        }

        /**
         * 문자열이 유효한(표시할 가치가 있는) 내용인지 확인하는 유틸리티 메소드입니다.
         * null, 빈 문자열, "null" 문자열, "정보 없음" 문자열을 모두 유효하지 않은 것으로 간주합니다.
         *
         * @param text 확인할 문자열.
         * @return 문자열이 유효하면 true, 그렇지 않으면 false.
         */
        private boolean isValidString(String text) {
            return text != null && !text.isEmpty() && !"null".equalsIgnoreCase(text) && !"정보 없음".equals(text);
        }
    }
}
