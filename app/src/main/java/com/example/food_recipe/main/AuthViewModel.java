package com.example.food_recipe.main;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.google.firebase.auth.FirebaseUser;

/**
 * [추가] MainActivity와 하위 프래그먼트들의 인증 상태(로그인된 사용자 정보)를 공유하기 위한 ViewModel입니다.
 * 이 ViewModel은 Activity의 생명주기를 따르므로, 화면 회전이나 프래그먼트 교체 시에도 데이터를 안전하게 유지합니다.
 */
public class AuthViewModel extends ViewModel {

    // 외부에서는 수정할 수 없도록 private으로 선언된 MutableLiveData입니다.
    // ViewModel 내부에서만 값을 변경할 수 있습니다.
    private final MutableLiveData<FirebaseUser> _user = new MutableLiveData<>();

    // 외부(Fragment, Activity)에서는 이 LiveData를 통해 사용자 정보의 '변화'를 관찰(observe)만 할 수 있습니다.
    // 이를 통해 데이터의 일관성과 안정성을 보장합니다.
    public LiveData<FirebaseUser> user = _user;

    /**
     * AuthStateListener로부터 받은 최신 사용자 정보를 LiveData에 설정합니다.
     * 이 메소드가 호출되면, user LiveData를 관찰하고 있는 모든 UI가 자동으로 업데이트됩니다.
     * @param firebaseUser 현재 로그인된 사용자 객체. 로그아웃 상태일 경우 null일 수 있습니다.
     */
    void setUser(FirebaseUser firebaseUser) {
        _user.setValue(firebaseUser);
    }
}
