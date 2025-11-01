package com.example.food_recipe.search;

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

/**
 * [추가] '내 냉장고 재료 불러오기' 바텀 시트의 RecyclerView를 위한 어댑터입니다.
 */
public class PantryImportAdapter extends RecyclerView.Adapter<PantryImportAdapter.ViewHolder> {

    private final List<String> pantryItems;
    private final Set<String> alreadySelectedChips;
    private final Set<String> newlySelectedItems;

    /**
     * 어댑터 생성자
     * @param pantryItems Firestore 등에서 가져온 사용자의 전체 재료 목록
     * @param alreadySelectedChips SearchFragment에 이미 Chip으로 추가된 재료 목록 (중복 선택 방지용)
     */
    public PantryImportAdapter(List<String> pantryItems, List<String> alreadySelectedChips) {
        this.pantryItems = pantryItems;
        this.alreadySelectedChips = new HashSet<>(alreadySelectedChips);
        this.newlySelectedItems = new HashSet<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // [추가] item_pantry_import.xml 레이아웃을 inflate하여 ViewHolder를 생성합니다.
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pantry_import, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String item = pantryItems.get(position);
        holder.itemName.setText(item);

        // [추가] 이미 Chip으로 선택된 항목인지 확인합니다.
        if (alreadySelectedChips.contains(item)) {
            holder.itemCheckbox.setChecked(true);
            holder.itemCheckbox.setEnabled(false); // 중복 선택 방지를 위해 비활성화
            holder.itemView.setEnabled(false);
        } else {
            // [추가] 새로 선택하는 항목들의 상태를 관리합니다.
            holder.itemCheckbox.setEnabled(true);
            holder.itemView.setEnabled(true);
            holder.itemCheckbox.setChecked(newlySelectedItems.contains(item));

            // [추가] 체크박스 클릭 리스너 설정
            holder.itemCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    newlySelectedItems.add(item);
                } else {
                    newlySelectedItems.remove(item);
                }
            });

            // [추가] 아이템 전체 클릭 리스너 (사용성 향상)
            holder.itemView.setOnClickListener(v -> holder.itemCheckbox.toggle());
        }
    }

    @Override
    public int getItemCount() {
        return pantryItems.size();
    }

    /**
     * [추가] 사용자가 새로 선택한 재료 목록을 반환합니다.
     * @return 선택된 재료 이름들의 ArrayList
     */
    public ArrayList<String> getSelectedItems() {
        return new ArrayList<>(newlySelectedItems);
    }

    /**
     * [추가] 전체 선택/해제 기능을 위한 메소드입니다.
     */
    public void selectAll() {
        newlySelectedItems.clear();
        for (String item : pantryItems) {
            if (!alreadySelectedChips.contains(item)) {
                newlySelectedItems.add(item);
            }
        }
        notifyDataSetChanged(); // 모든 뷰를 갱신
    }


    /**
     * [추가] RecyclerView의 각 아이템 뷰를 보관하는 ViewHolder 클래스입니다.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        final CheckBox itemCheckbox;
        final TextView itemName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // [추가] item_pantry_import.xml에 정의된 ID를 사용하여 뷰를 찾습니다.
            itemCheckbox = itemView.findViewById(R.id.pantry_item_checkbox);
            itemName = itemView.findViewById(R.id.pantry_item_name);
        }
    }
}
