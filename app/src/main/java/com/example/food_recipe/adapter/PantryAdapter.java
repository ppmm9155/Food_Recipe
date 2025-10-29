package com.example.food_recipe.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.food_recipe.R;
import com.example.food_recipe.model.PantryItem;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * 냉장고(Pantry) 재료 목록을 RecyclerView에 표시하기 위한 어댑터입니다.
 * PantryItem 리스트 데이터를 받아 각 아이템을 레이아웃에 바인딩하는 역할을 합니다.
 * 또한, 스와이프를 통한 '실행 취소' 기능을 지원하기 위해 아이템을 임시로 제거하거나 복원하는 메서드를 제공합니다.
 */
public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.PantryViewHolder> {

    /** 어댑터가 현재 표시하고 있는 재료 아이템들의 리스트입니다. */
    private final List<PantryItem> pantryItems = new ArrayList<>();

    /**
     * ViewHolder가 생성될 때 호출됩니다.
     * list_item_pantry.xml 레이아웃을 인플레이트하여 ViewHolder 객체를 생성하고 반환합니다.
     *
     * @param parent ViewHolder가 속하게 될 부모 ViewGroup입니다.
     * @param viewType 여러 종류의 뷰 타입을 구분할 때 사용됩니다. (현재는 한 종류만 사용)
     * @return 생성된 PantryViewHolder 객체
     */
    @NonNull
    @Override
    public PantryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_pantry, parent, false);
        return new PantryViewHolder(view);
    }

    /**
     * ViewHolder가 화면에 표시될 데이터를 바인딩할 때 호출됩니다.
     * 특정 위치(position)에 있는 PantryItem 데이터를 가져와 ViewHolder의 뷰들에 설정합니다.
     *
     * @param holder 데이터를 바인딩할 PantryViewHolder 객체입니다.
     * @param position 현재 아이템의 리스트 내 위치입니다.
     */
    @Override
    public void onBindViewHolder(@NonNull PantryViewHolder holder, int position) {
        PantryItem item = pantryItems.get(position);
        holder.tvName.setText(item.getName());
        holder.tvQuantity.setText(String.format(Locale.getDefault(), "%.1f %s", item.getQuantity(), item.getUnit()));

        // 유통기한 D-Day 계산 및 텍스트 색상 변경 로직을 호출합니다.
        updateExpirationDateView(holder.tvExpiration, item.getExpirationDate());

        // 카테고리 문자열에서 이모지를 분리하여 표시합니다.
        String category = item.getCategory();
        if (category != null && !category.isEmpty()) {
            String[] parts = category.split(" ");
            if (parts.length > 1) {
                holder.tvCategoryEmoji.setText(parts[1]);
            } else {
                holder.tvCategoryEmoji.setText("✨"); // 기본 이모지
            }
        }
        holder.tvStorage.setText(item.getStorage());
    }

    /**
     * 유통기한까지 남은 기간(D-Day)을 계산하고, 기간에 따라 TextView의 텍스트와 색상을 변경합니다.
     *
     * @param textView 스타일을 적용할 유통기한 TextView
     * @param expirationDate 유통기한 날짜 정보
     */
    private void updateExpirationDateView(TextView textView, Date expirationDate) {
        if (expirationDate == null) {
            textView.setText("-"); // 유통기한 정보가 없을 경우
            textView.setTextColor(ContextCompat.getColor(textView.getContext(), R.color.black_2));
            return;
        }

        // 정확한 D-Day 계산을 위해 시간, 분, 초를 0으로 설정합니다.
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        Calendar expiry = Calendar.getInstance();
        expiry.setTime(expirationDate);
        expiry.set(Calendar.HOUR_OF_DAY, 0);
        expiry.set(Calendar.MINUTE, 0);
        expiry.set(Calendar.SECOND, 0);
        expiry.set(Calendar.MILLISECOND, 0);

        long diffInMillis = expiry.getTimeInMillis() - today.getTimeInMillis();
        long diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis);

        Context context = textView.getContext();
        String dDayText;
        int textColor;

        // 남은 기간에 따라 텍스트와 색상을 다르게 설정합니다.
        if (diffInDays < 0) {
            dDayText = "기한 만료";
            textColor = Color.GRAY;
        } else if (diffInDays == 0) {
            dDayText = "D-Day";
            textColor = ContextCompat.getColor(context, R.color.red);
        } else if (diffInDays <= 7) {
            dDayText = "D-" + diffInDays;
            textColor = ContextCompat.getColor(context, R.color.orange);
        } else {
            dDayText = "D-" + diffInDays;
            textColor = ContextCompat.getColor(context, R.color.black_2);
        }

        textView.setText(dDayText);
        textView.setTextColor(textColor);
    }

    /**
     * 어댑터가 가지고 있는 전체 아이템의 개수를 반환합니다.
     *
     * @return 리스트의 크기
     */
    @Override
    public int getItemCount() {
        return pantryItems.size();
    }

    /**
     * 새로운 데이터 리스트로 어댑터의 데이터를 교체하고 UI를 갱신합니다.
     *
     * @param newItems 새로 표시할 PantryItem 리스트
     */
    public void setPantryItems(List<PantryItem> newItems) {
        this.pantryItems.clear();
        this.pantryItems.addAll(newItems);
        notifyDataSetChanged(); // 전체 데이터셋이 변경되었음을 알림
    }

    /**
     * 특정 위치(position)에 있는 PantryItem 객체를 반환합니다.
     * Fragment에서 스와이프된 아이템의 정보를 얻기 위해 사용됩니다.
     *
     * @param position 정보를 가져올 아이템의 위치
     * @return 해당 위치의 PantryItem 객체
     */
    public PantryItem getItemAt(int position) {
        if (position >= 0 && position < pantryItems.size()) {
            return pantryItems.get(position);
        }
        return null;
    }

    /**
     * '실행 취소' 기능을 위해 특정 위치의 아이템을 리스트에서 임시로 제거합니다.
     * notifyItemRemoved를 사용하여 RecyclerView에 아이템 제거 애니메이션을 적용합니다.
     *
     * @param position 제거할 아이템의 위치
     */
    public void removeItem(int position) {
        if (position >= 0 && position < pantryItems.size()) {
            pantryItems.remove(position);
            notifyItemRemoved(position);
        }
    }

    /**
     * '실행 취소' 기능을 위해 제거되었던 아이템을 원래 위치에 다시 추가합니다.
     * notifyItemInserted를 사용하여 RecyclerView에 아이템 추가 애니메이션을 적용합니다.
     *
     * @param item 복원할 PantryItem 객체
     * @param position 아이템을 복원할 원래 위치
     */
    public void restoreItem(PantryItem item, int position) {
        if (item != null && position >= 0) {
            pantryItems.add(position, item);
            notifyItemInserted(position);
        }
    }

    /**
     * RecyclerView의 각 아이템 뷰에 대한 참조를 보관하는 내부 클래스입니다.
     */
    static class PantryViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        TextView tvQuantity;
        TextView tvExpiration;
        TextView tvCategoryEmoji;
        TextView tvStorage;

        public PantryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.pantry_item_tv_name);
            tvQuantity = itemView.findViewById(R.id.pantry_item_tv_quantity);
            tvExpiration = itemView.findViewById(R.id.pantry_item_tv_expiration);
            tvCategoryEmoji = itemView.findViewById(R.id.pantry_item_tv_category_emoji);
            tvStorage = itemView.findViewById(R.id.pantry_item_tv_storage);
        }
    }
}
