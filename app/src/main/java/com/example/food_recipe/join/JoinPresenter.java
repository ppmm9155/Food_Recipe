package com.example.food_recipe.join;

import com.example.food_recipe.utils.ValidationUtils;
import com.google.firebase.auth.FirebaseAuthException;

import java.util.Locale;

/**
 * Presenter (중재자 역할)
 * - View(화면)와 Model(데이터 처리) 사이의 로직 담당
 * - 입력값 검증, Firebase 결과에 따른 분기 처리
 * - View는 화면만, Model은 데이터 통신만 담당 → Presenter가 흐름을 제어
 */
public class JoinPresenter implements JoinContract.Presenter {

    private final JoinContract.View view;
    private final JoinContract.Model model;

    // 아이디 중복확인 캐시 (원본 코드 UX 유지)
    private String lastCheckedUsernameLower = null;
    private boolean lastCheckedUsernameAvailable = false;

    public JoinPresenter(JoinContract.View view, JoinContract.Model model) {
        this.view = view;
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
        final String username = ValidationUtils.normalizeUsername(usernameRaw);
        if (!ValidationUtils.validateUsername(username)) {
            view.showIdError("아이디는 영문/숫자 4~16자여야 합니다.");
            return;
        }

        final String lower = username.toLowerCase(Locale.ROOT);

        view.setUiEnabled(false);
        model.checkUsernameAvailability(lower, new JoinModel.UsernameCallback() {
            @Override
            public void onResult(boolean available) {
                view.setUiEnabled(true);
                lastCheckedUsernameLower = lower;
                lastCheckedUsernameAvailable = available;

                if (available) view.showIdOk("사용 가능한 아이디입니다.");
                else view.showIdError("이미 사용 중인 아이디입니다.");
            }

            @Override
            public void onError(Exception e) {
                view.setUiEnabled(true);
                view.toast("아이디 확인 중 오류가 발생했습니다. 다시 시도해 주세요.");
            }
        });
    }

    @Override
    public void checkEmailAvailability(String emailRaw) {
        final String email = ValidationUtils.normalizeEmail(emailRaw);
        if (!ValidationUtils.validateEmail(email)) {
            view.showEmailError("이메일 형식이 올바르지 않습니다.");
            return;
        }

        // 🔐 열거 보호 ON 환경에서도 틀린 안내를 피하려면:
        view.setUiEnabled(false);
        model.checkEmailAvailability(email, new JoinModel.EmailCallback() {
            @Override
            public void onResult(boolean available) {
                view.setUiEnabled(true);

                // ✅ 확정 표현 금지: 중립 문구로만 안내
                if (available) {
                    view.showEmailOk("형식 확인 완료. 회원가입 단계에서 최종 확인됩니다.");
                } else {
                    view.showEmailError("이미 가입된 이메일일 수 있습니다. 회원가입에서 최종 확인됩니다.");
                }
            }
            @Override
            public void onError(Exception e) {
                view.setUiEnabled(true);
                view.toast("이메일 확인 중 오류가 발생했습니다. 다시 시도해 주세요.");
            }
        });
    }


    @Override
    public void attemptRegister(String usernameRaw, String emailRaw, String p1, String p2) {
        final String username = ValidationUtils.normalizeUsername(usernameRaw);
        final String email = ValidationUtils.normalizeEmail(emailRaw);

        if (!ValidationUtils.validateUsername(username)) {
            view.showIdError("아이디는 영문/숫자 4~16자여야 합니다.");
            return;
        }
        if (!ValidationUtils.validateEmail(email)) {
            view.showEmailError("이메일 형식이 올바르지 않습니다.");
            return;
        }
        if (!ValidationUtils.validatePasswords(p1, p2)) {
            view.showPasswordConfirmError("비밀번호가 일치하지 않습니다.");
            return;
        }

        final String lower = username.toLowerCase(Locale.ROOT);

        // UX: 중복확인 팁 메시지 (원본과 동일)
        if (!(lower.equals(lastCheckedUsernameLower) && lastCheckedUsernameAvailable)) {
            view.showIdHelper("Tip: 중복확인을 먼저 누르면 더 빨라요. (바로 진행해도 괜찮습니다)");
        }

        view.setUiEnabled(false);

        // 서버에서 아이디 다시 체크 (동시에 누가 가입했을 경우 대비)
        model.checkUsernameAvailability(lower, new JoinModel.UsernameCallback() {
            @Override
            public void onResult(boolean available) {
                if (!available) {
                    view.setUiEnabled(true);
                    view.showIdError("이미 사용 중인 아이디입니다.");
                    return;
                }
                // 아이디 사용 가능 → 회원가입 진행
                model.createUserThenSaveProfile(username, email, p1, new JoinModel.RegisterCallback() {
                    @Override
                    public void onSuccess() {
                        view.setUiEnabled(true);
                        view.toast("회원가입이 완료되었습니다. 이메일 인증 후 로그인해 주세요.");
                        try { com.google.firebase.auth.FirebaseAuth.getInstance().signOut(); } catch (Exception ignore) {}
                        view.navigateToLogin();
                    }

                    @Override
                    public void onError(Exception e) {
                        view.setUiEnabled(true);
                        String code = (e instanceof FirebaseAuthException) ? ((FirebaseAuthException) e).getErrorCode() : null;
                        if ("ERROR_EMAIL_ALREADY_IN_USE".equals(code)) {
                            view.showEmailError("이미 가입된 이메일입니다."); return;
                        }
                        if ("ERROR_INVALID_EMAIL".equals(code)) {
                            view.showEmailError("이메일 형식이 올바르지 않습니다."); return;
                        }
                        if ("ERROR_WEAK_PASSWORD".equals(code)) {
                            view.showPasswordError("비밀번호가 너무 약합니다. (최소 6자 이상 권장)"); return;
                        }
                        view.toast("아이디가 방금 사용되었습니다. 다른 아이디를 선택해 주세요.");
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                view.setUiEnabled(true);
                view.toast("아이디 확인 중 오류가 발생했습니다. 다시 시도해 주세요.");
            }
        });
    }

    @Override
    public void detachView() {
        // 현재는 정리할 리소스 없음
    }
}
