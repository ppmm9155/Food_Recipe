package com.example.food_recipe.main;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.food_recipe.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * 메인 화면의 일부를 담당하는 홈 프래그먼트입니다.
 * Fragment는 Activity 내에서 재사용할 수 있는 UI 조각입니다.
 * 이 프래그먼트는 현재 MVP 패턴을 따르지 않고, 자체적으로 데이터 로직을 처리하고 있습니다.
 * (학습 참고: 복잡한 화면의 경우, 이 프래그먼트도 별도의 Presenter와 Model을 가질 수 있습니다.)
 */
public class HomeFragment extends Fragment {

    // "오늘 뭐 먹을까?" 문구를 표시할 텍스트 뷰
    private TextView title;

    /**
     * 프래그먼트가 자신의 UI를 처음으로 그릴 때 호출되는 메서드입니다.
     * @return 프래그먼트의 최상위 뷰 (화면에 보여질 내용)
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // 1. XML 레이아웃 파일을 자바 객체로 변환합니다. (Inflation)
        View root = inflater.inflate(R.layout.fragment_main, container, false);
        // 2. 레이아웃 파일 안에 있는 텍스트 뷰를 찾아 변수에 할당합니다.
        title = root.findViewById(R.id.home_title);
        // 3. 환영 문구를 불러오는 작업을 시작합니다.
        loadGreeting();
        // 4. 완성된 뷰를 반환하여 화면에 표시합니다.
        return root;
    }

    /**
     * Firebase에서 사용자 이름을 가져와 환영 문구를 설정하는 메서드입니다.
     */
    private void loadGreeting() {
        // 현재 로그인된 사용자가 있는지 확인합니다.
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            // 로그인된 사용자가 없으면 기본 문구를 설정하고 종료합니다.
            title.setText("오늘 뭐 먹을까?");
            return;
        }

        // 로그인된 사용자의 고유 ID(uid)를 가져옵니다.
        String uid = user.getUid();
        // Firestore 데이터베이스에서 "users" 컬렉션의 현재 사용자 문서를 가져옵니다.
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(this::applyUsername) // 성공 시 applyUsername 메서드 호출
                .addOnFailureListener(e -> { // 실패 시 로그를 남기고 기본 문구 설정
                    Log.e("HomeFragment", "사용자 이름 로드 실패", e);
                    title.setText("오늘 뭐 먹을까?");
                });
    }

    /**
     * Firestore에서 가져온 사용자 정보를 바탕으로 환영 문구를 적용하는 메서드입니다.
     * @param doc Firestore로부터 받은 사용자 문서 스냅샷
     */
    private void applyUsername(DocumentSnapshot doc) {
        if (doc != null && doc.exists()) {
            // 문서에서 "username" 필드의 값을 문자열로 가져옵니다.
            String username = doc.getString("username");
            if (username != null && !username.trim().isEmpty()) {
                // 사용자 이름이 존재하면, 환영 문구를 완성하여 텍스트 뷰에 설정합니다.
                title.setText("오늘 뭐 먹을까, " + username + "님?");
                return; // 작업 완료
            }
        }
        // 사용자 이름이 없거나, 문서를 가져오는 데 실패한 경우 기본 문구를 설정합니다.
        title.setText("오늘 뭐 먹을까?");
    }
}
