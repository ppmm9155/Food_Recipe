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
import com.example.food_recipe.model.Recipe;

import java.util.ArrayList;
import java.util.List;

/**
 * [수정] 최신 Recipe 모델(getTitle)에 맞게 바인딩 로직을 수정한 어댑터입니다.
 */
public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    private final List<Recipe> recipes = new ArrayList<>();
    private final Context context;

    public RecipeAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_recipe, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        Recipe recipe = recipes.get(position);
        holder.bind(recipe);
    }

    @Override
    public int getItemCount() {
        return recipes != null ? recipes.size() : 0;
    }

    public void setRecipes(List<Recipe> newRecipes) {
        this.recipes.clear();
        if (newRecipes != null) {
            this.recipes.addAll(newRecipes);
        }
        notifyDataSetChanged();
    }

    public class RecipeViewHolder extends RecyclerView.ViewHolder {

        private final ImageView ivRecipeImage;
        private final TextView tvRecipeTitle;
        private final TextView tvCookingTime;
        private final TextView tvIngredients;

        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            ivRecipeImage = itemView.findViewById(R.id.iv_recipe_image);
            tvRecipeTitle = itemView.findViewById(R.id.tv_recipe_title);
            tvCookingTime = itemView.findViewById(R.id.tv_cooking_time);
            tvIngredients = itemView.findViewById(R.id.tv_ingredients);
        }

        public void bind(Recipe recipe) {
            Glide.with(context)
                    .load(recipe.getImageUrl())
                    .into(ivRecipeImage);

            // [수정] getRecipeName() -> getTitle() 로 변경
            tvRecipeTitle.setText(recipe.getTitle());
            // [수정] cooking_time을 사용하도록 변경
            tvCookingTime.setText("조리 시간: " + recipe.getCookingTime());

            if (recipe.getIngredients() != null && !recipe.getIngredients().isEmpty()) {
                tvIngredients.setText("재료: " + recipe.getIngredients());
            } else {
                tvIngredients.setText("재료: 정보 없음");
            }
        }
    }
}
