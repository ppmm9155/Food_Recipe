package com.example.food_recipe.join;

import com.example.food_recipe.base.BasePresenter;
import com.example.food_recipe.utils.ValidationUtils;
import com.google.firebase.auth.FirebaseAuthException;

import java.util.Locale;

/**
 * Presenter (중재자 역할)
 * - View(화면)와 Model(데이터 처리) 사이의 로직 담당
 * - 입력값 검증, Firebase 결과에 따른 분기 처리
 * - View는 화면만, Model은 데이터 통신만 담당 → Presenter가 흐름을 제어
 */
// [변경] BasePresenter를 상속받아 View 생명주기를 안전하게 관리
public class JoinPresenter extends BasePresenter<JoinContract.View> implements JoinContract.Presenter {

    // [삭제] view 멤버 변수. BasePresenter가 관리하므로 제거.
    private final JoinContract.Model model;

    // 아이디 중복확인 캐시 (원본 코드 UX 유지)
    private String lastCheckedUsernameLower = null;
    private boolean lastCheckedUsernameAvailable = false;

    // [변경] 생성자에서 View를 받지 않음.
    public JoinPresenter(JoinContract.Model model) {
        this.model = model;
    }

    @Override
    public void onUsernameEdited() {
        // 아이디 입력이 바뀌면 이전 중복확인 결과를 무효화
        lastCheckedUsernameLower = null;
        lastCheckedUsernameAvailable = false;
    }

    @Override
    public void checkUsernameAvailability(String usernameRaw) {
        // [추가] isViewAttached() 체크로 안정성 강화
        if (!isViewAttached()) return;

        final String username = ValidationUtils.normalizeUsername(usernameRaw);
        if (!ValidationUtils.validateUsername(username)) {
            getView().showIdError("아이디는 영문/숫자 4~16자여야 합니다.");
            return;
        }

        final String lower = username.toLowerCase(Locale.ROOT);

        getView().setUiEnabled(false);
        model.checkUsernameAvailability(lower, new JoinModel.UsernameCallback() {
            @Override
            public void onResult(boolean available) {
                if (!isViewAttached()) return;
                getView().setUiEnabled(true);
                lastCheckedUsernameLower = lower;
                lastCheckedUsernameAvailable = available;

                if (available) getView().showIdOk("사용 가능한 아이디입니다.");
                else getView().showIdError("이미 사용 중인 아이디입니다.");
            }

            @Override
            public void onError(Exception e) {
                if (!isViewAttached()) return;
                getView().setUiEnabled(true);
                getView().toast("아이디 확인 중 오류가 발생했습니다. 다시 시도해 주세요.");
            }
        });
    }

    @Override
    public void checkEmailAvailability(String emailRaw) {
        if (!isViewAttached()) return;

        final String email = ValidationUtils.normalizeEmail(emailRaw);
        if (!ValidationUtils.validateEmail(email)) {
            getView().showEmailError("이메일 형식이 올바르지 않습니다.");
            return;
        }

        getView().setUiEnabled(false);
        model.checkEmailAvailability(email, new JoinModel.EmailCallback() {
            @Override
            public void onResult(boolean available) {
                if (!isViewAttached()) return;
                getView().setUiEnabled(true);

                if (available) {
                    getView().showEmailOk("형식 확인 완료. 회원가입 단계에서 최종 확인됩니다.");
                } else {
                    getView().showEmailError("이미 가입된 이메일일 수 있습니다. 회원가입에서 최종 확인됩니다.");
                }
            }
            @Override
            public void onError(Exception e) {
                if (!isViewAttached()) return;
                getView().setUiEnabled(true);
                getView().toast("이메일 확인 중 오류가 발생했습니다. 다시 시도해 주세요.");
            }
        });
    }


    @Override
    public void attemptRegister(String usernameRaw, String emailRaw, String p1, String p2) {
        if (!isViewAttached()) return;

        final String username = ValidationUtils.normalizeUsername(usernameRaw);
        final String email = ValidationUtils.normalizeEmail(emailRaw);

        if (!ValidationUtils.validateUsername(username)) {
            getView().showIdError("아이디는 영문/숫자 4~16자여야 합니다.");
            return;
        }
        if (!ValidationUtils.validateEmail(email)) {
            getView().showEmailError("이메일 형식이 올바르지 않습니다.");
            return;
        }
        if (!ValidationUtils.validatePasswords(p1, p2)) {
            getView().showPasswordConfirmError("비밀번호가 일치하지 않습니다.");
            return;
        }

        final String lower = username.toLowerCase(Locale.ROOT);

        if (!(lower.equals(lastCheckedUsernameLower) && lastCheckedUsernameAvailable)) {
            getView().showIdHelper("Tip: 중복확인을 먼저 누르면 더 빨라요. (바로 진행해도 괜찮습니다)");
        }

        getView().setUiEnabled(false);

        model.checkUsernameAvailability(lower, new JoinModel.UsernameCallback() {
            @Override
            public void onResult(boolean available) {
                if (!isViewAttached()) return;
                if (!available) {
                    getView().setUiEnabled(true);
                    getView().showIdError("이미 사용 중인 아이디입니다.");
                    return;
                }
                model.createUserThenSaveProfile(username, email, p1, new JoinModel.RegisterCallback() {
                    @Override
                    public void onSuccess() {
                        if (!isViewAttached()) return;
                        getView().setUiEnabled(true);
                        getView().toast("회원가입이 완료되었습니다. 이메일 인증 후 로그인해 주세요.");
                        try { com.google.firebase.auth.FirebaseAuth.getInstance().signOut(); } catch (Exception ignore) {}
                        getView().navigateToLogin();
                    }

                    @Override
                    public void onError(Exception e) {
                        if (!isViewAttached()) return;
                        getView().setUiEnabled(true);
                        String code = (e instanceof FirebaseAuthException) ? ((FirebaseAuthException) e).getErrorCode() : null;
                        if ("ERROR_EMAIL_ALREADY_IN_USE".equals(code)) {
                            getView().showEmailError("이미 가입된 이메일입니다."); return;
                        }
                        if ("ERROR_INVALID_EMAIL".equals(code)) {
                            getView().showEmailError("이메일 형식이 올바르지 않습니다."); return;
                        }
                        if ("ERROR_WEAK_PASSWORD".equals(code)) {
                            getView().showPasswordError("비밀번호가 너무 약합니다. (최소 6자 이상 권장)"); return;
                        }
                        getView().toast("아이디가 방금 사용되었습니다. 다른 아이디를 선택해 주세요.");
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                if (!isViewAttached()) return;
                getView().setUiEnabled(true);
                getView().toast("아이디 확인 중 오류가 발생했습니다. 다시 시도해 주세요.");
            }
        });
    }

    // [삭제] detachView()는 BasePresenter에 구현되어 있으므로 제거
}
