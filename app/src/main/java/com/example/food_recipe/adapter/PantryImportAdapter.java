package com.example.food_recipe.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.food_recipe.R;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PantryImportAdapter extends RecyclerView.Adapter<PantryImportAdapter.ViewHolder> {

    // --- [추가] 선택 항목 개수 변경을 Fragment에 알리기 위한 리스너 인터페이스 ---
    public interface OnSelectionChangedListener {
        void onSelectionChanged(int count);
    }

    private final List<String> pantryItems;
    private final Set<String> alreadySelectedChips;
    private final Set<String> newlySelectedItems;
    // --- [추가] 리스너 멤버 변수 ---
    private OnSelectionChangedListener selectionListener;

    public PantryImportAdapter(List<String> pantryItems, List<String> alreadySelectedChips) {
        this.pantryItems = pantryItems;
        this.alreadySelectedChips = new HashSet<>(alreadySelectedChips);
        this.newlySelectedItems = new HashSet<>();
    }

    // --- [추가] Fragment에서 리스너를 설정하기 위한 메소드 ---
    public void setOnSelectionChangedListener(OnSelectionChangedListener listener) {
        this.selectionListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pantry_import, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String item = pantryItems.get(position);
        holder.itemName.setText(item);

        if (alreadySelectedChips.contains(item)) {
            holder.itemCheckbox.setChecked(true);
            holder.itemCheckbox.setEnabled(false);
            holder.itemView.setEnabled(false);
        } else {
            holder.itemCheckbox.setEnabled(true);
            holder.itemView.setEnabled(true);
            holder.itemCheckbox.setChecked(newlySelectedItems.contains(item));

            holder.itemCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    newlySelectedItems.add(item);
                } else {
                    newlySelectedItems.remove(item);
                }
                // --- [추가] 선택 상태가 변경될 때마다 리스너를 호출하여 개수를 알림 ---
                if (selectionListener != null) {
                    selectionListener.onSelectionChanged(newlySelectedItems.size());
                }
            });
            holder.itemView.setOnClickListener(v -> holder.itemCheckbox.toggle());
        }
    }

    @Override
    public int getItemCount() {
        return pantryItems.size();
    }

    public ArrayList<String> getSelectedItems() {
        return new ArrayList<>(newlySelectedItems);
    }

    public void selectAll() {
        newlySelectedItems.clear();
        for (String item : pantryItems) {
            if (!alreadySelectedChips.contains(item)) {
                newlySelectedItems.add(item);
            }
        }
        notifyDataSetChanged();
        // --- [추가] 전체 선택 후에도 리스너를 호출하여 개수를 알림 ---
        if (selectionListener != null) {
            selectionListener.onSelectionChanged(newlySelectedItems.size());
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final CheckBox itemCheckbox;
        final TextView itemName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemCheckbox = itemView.findViewById(R.id.pantry_item_checkbox);
            itemName = itemView.findViewById(R.id.pantry_item_name);
        }
    }
}
