package com.example.food_recipe.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.food_recipe.R;
import java.util.List;

/**
 * [추가] 마이페이지의 메뉴 목록을 표시하기 위한 RecyclerView 어댑터
 */
public class MyPageMenuAdapter extends RecyclerView.Adapter<MyPageMenuAdapter.ViewHolder> {

    /**
     * [추가] 메뉴 아이템 데이터 목록
     */
    private final List<String> menuItems;

    /**
     * [추가] 아이템 클릭 이벤트를 처리하기 위한 리스너 인터페이스
     */
    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    /**
     * [추가] 클릭 리스너 인스턴스
     */
    private OnItemClickListener listener;

    /**
     * [추가] 외부에서 클릭 리스너를 설정하기 위한 메서드
     * @param listener 아이템 클릭 리스너
     */
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    /**
     * [추가] 생성자: 메뉴 아이템 목록을 전달받음
     * @param menuItems 표시할 메뉴 이름 목록
     */
    public MyPageMenuAdapter(List<String> menuItems) {
        this.menuItems = menuItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // [추가] list_item_mypage_menu.xml 레이아웃을 인플레이트하여 ViewHolder 생성
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_mypage_menu, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // [추가] 현재 위치에 해당하는 메뉴 이름을 ViewHolder에 바인딩
        String menuItem = menuItems.get(position);
        holder.menuTitle.setText(menuItem);
    }

    @Override
    public int getItemCount() {
        // [추가] 메뉴 아이템의 전체 개수 반환
        return menuItems.size();
    }

    /**
     * [추가] 각 메뉴 아이템의 뷰를 보관하는 ViewHolder 클래스
     */
    public class ViewHolder extends RecyclerView.ViewHolder {
        // [추가] 메뉴 제목을 표시하는 텍스트뷰
        public final TextView menuTitle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            menuTitle = itemView.findViewById(R.id.tv_menu_title);

            // [추가] 아이템 뷰에 클릭 리스너 설정
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(position);
                }
            });
        }
    }
}
