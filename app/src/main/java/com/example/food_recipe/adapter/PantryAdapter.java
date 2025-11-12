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
 * [기존 주석 유지] 냉장고(Pantry) 재료 목록을 RecyclerView에 표시하기 위한 어댑터입니다.
 * [변경] 아이템 클릭 이벤트를 처리하기 위한 리스너가 추가되었습니다.
 */
public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.PantryViewHolder> {

    /** [기존 주석 유지] 어댑터가 현재 표시하고 있는 재료 아이템들의 리스트입니다. */
    private final List<PantryItem> pantryItems = new ArrayList<>();
    /**
     * [추가] 아이템 뷰 클릭 이벤트를 처리하기 위한 리스너 변수
     */
    private OnItemClickListener onItemClickListener;

    /**
     * [추가] 클릭 리스너의 인터페이스 정의.
     * 이 인터페이스를 구현하는 객체는 아이템 클릭 시 호출될 메서드를 제공해야 합니다.
     */
    public interface OnItemClickListener {
        void onItemClick(PantryItem pantryItem);
    }

    /**
     * [추가] 외부(Fragment)에서 클릭 리스너를 설정하기 위한 메서드입니다.
     * @param listener 아이템 클릭 시 호출될 리스너 객체
     */
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }


    /**
     * [기존 주석 유지] ViewHolder가 생성될 때 호출됩니다.
     */
    @NonNull
    @Override
    public PantryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_pantry, parent, false);
        return new PantryViewHolder(view);
    }

    /**
     * [기존 주석 유지] ViewHolder가 화면에 표시될 데이터를 바인딩할 때 호출됩니다.
     * [변경] 아이템 데이터 바인딩과 더불어, 아이템 뷰에 대한 클릭 리스너를 설정하는 로직이 추가되었습니다.
     */
    @Override
    public void onBindViewHolder(@NonNull PantryViewHolder holder, int position) {
        PantryItem item = pantryItems.get(position);
        holder.tvName.setText(item.getName());
        holder.tvQuantity.setText(String.format(Locale.getDefault(), "%.1f %s", item.getQuantity(), item.getUnit()));

        updateExpirationDateView(holder.tvExpiration, item.getExpirationDate());

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

        // [추가] 아이템 뷰에 클릭 리스너를 설정합니다.
        // 리스너가 설정되어 있을 경우, 클릭된 아이템 정보를 콜백 메서드로 전달합니다.
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(item);
            }
        });
    }

    /**
     * [기존 주석 유지] 유통기한까지 남은 기간(D-Day)을 계산하고, 기간에 따라 TextView의 텍스트와 색상을 변경합니다.
     */
    private void updateExpirationDateView(TextView textView, Date expirationDate) {
        if (expirationDate == null) {
            textView.setText("-");
            textView.setTextColor(ContextCompat.getColor(textView.getContext(), R.color.black_2));
            return;
        }

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
     * [기존 주석 유지] 어댑터가 가지고 있는 전체 아이템의 개수를 반환합니다.
     */
    @Override
    public int getItemCount() {
        return pantryItems.size();
    }

    /**
     * [기존 주석 유지] 새로운 데이터 리스트로 어댑터의 데이터를 교체하고 UI를 갱신합니다.
     */
    public void setPantryItems(List<PantryItem> newItems) {
        this.pantryItems.clear();
        this.pantryItems.addAll(newItems);
        notifyDataSetChanged();
    }

    /**
     * [기존 주석 유지] 특정 위치(position)에 있는 PantryItem 객체를 반환합니다.
     */
    public PantryItem getItemAt(int position) {
        if (position >= 0 && position < pantryItems.size()) {
            return pantryItems.get(position);
        }
        return null;
    }

    /**
     * [기존 주석 유지] '실행 취소' 기능을 위해 특정 위치의 아이템을 리스트에서 임시로 제거합니다.
     */
    public void removeItem(int position) {
        if (position >= 0 && position < pantryItems.size()) {
            pantryItems.remove(position);
            notifyItemRemoved(position);
        }
    }

    /**
     * [기존 주석 유지] '실행 취소' 기능을 위해 제거되었던 아이템을 원래 위치에 다시 추가합니다.
     */
    public void restoreItem(PantryItem item, int position) {
        if (item != null && position >= 0) {
            pantryItems.add(position, item);
            notifyItemInserted(position);
        }
    }

    /**
     * [기존 주석 유지] RecyclerView의 각 아이템 뷰에 대한 참조를 보관하는 내부 클래스입니다.
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
