package com.example.food_recipe.backup;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.food_recipe.R;
import com.example.food_recipe.login.LoginActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class BackupJoinActivity extends AppCompatActivity {

    private static final String TAG = "JoinActivity";

    // ── Firebase ─────────────────────────────────────────────
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // ── UI refs (activity_join.xml 과 1:1 매칭) ───────────────
    private TextInputLayout tilId, tilEmail, tilPassword, tilPasswordConfirm;
    private TextInputEditText etId, etEmail, etPassword, etPasswordConfirm;
    private MaterialButton btnCheckId, btnVerifyEmail, btnRegister;

    // 최근 체크한 아이디 캐시(사용자 편의용)
    private String lastCheckedUsernameLower = null;
    private boolean lastCheckedUsernameAvailable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);

        // Firebase
        mAuth = FirebaseAuth.getInstance();
        mAuth.setLanguageCode("ko");
        db = FirebaseFirestore.getInstance();

        bindViews();
        bindEvents();
    }

    private void bindViews() {
        tilId              = findViewById(R.id.tilId);
        tilEmail           = findViewById(R.id.tilEmail);
        tilPassword        = findViewById(R.id.tilPassword);
        tilPasswordConfirm = findViewById(R.id.tilPasswordConfirm);

        etId               = findViewById(R.id.Join_etId);
        etEmail            = findViewById(R.id.Join_etEmail);
        etPassword         = findViewById(R.id.Join_etPassword);
        etPasswordConfirm  = findViewById(R.id.Join_etPasswordConfirm);

        btnCheckId         = findViewById(R.id.btnCheckId);
        btnVerifyEmail     = findViewById(R.id.btnVerifyEmail);
        btnRegister        = findViewById(R.id.Join_btnRegister);
    }

    private void bindEvents() {
        // 아이디 입력이 바뀌면 이전 중복확인 결과 무효화
        etId.addTextChangedListener(new SimpleWatcher(() -> {
            clearError(tilId);
            lastCheckedUsernameLower = null;
            lastCheckedUsernameAvailable = false;
        }));

        // 이메일 입력 중: 에러/헬퍼 정리
        etEmail.addTextChangedListener(new SimpleWatcher(() -> {
            clearError(tilEmail);
            setHelper(tilEmail, null);
        }));

        // 비밀번호: 에러 정리
        etPassword.addTextChangedListener(new SimpleWatcher(() -> clearError(tilPassword)));

        // 비밀번호 확인: 즉시 일치 여부 안내(헬퍼)
        etPasswordConfirm.addTextChangedListener(new SimpleWatcher(() -> {
            clearError(tilPasswordConfirm);
            String p1 = text(etPassword);
            String p2 = text(etPasswordConfirm);
            if (!p2.isEmpty()) {
                if (p1.equals(p2)) {
                    setHelper(tilPasswordConfirm, "비밀번호가 일치합니다.");
                } else {
                    setHelper(tilPasswordConfirm, "비밀번호가 일치하지 않습니다.");
                }
            } else {
                setHelper(tilPasswordConfirm, null);
            }
        }));

        // 아이디 중복확인
        btnCheckId.setOnClickListener(v -> checkUsernameAvailability());

        // 이메일 "인증" 버튼: 실제로는 사전 **가용성 체크**(회원가입 전에 사용 가능 여부 확인)
        btnVerifyEmail.setOnClickListener(v -> checkEmailAvailability());

        // 회원가입
        btnRegister.setOnClickListener(v -> attemptRegister());
    }

    // ─────────────────────────────────────────────────────────────
    // 1) 아이디 가용성 체크 (/usernames/{usernameLower} 단일 문서 조회)
    // ─────────────────────────────────────────────────────────────
    private void checkUsernameAvailability() {
        String username = normalizeUsername(text(etId));
        if (!validateUsername(username)) return;

        String lower = username.toLowerCase(Locale.ROOT);

        setUiEnabled(false);
        db.collection("usernames").document(lower).get()
                .addOnSuccessListener(doc -> {
                    boolean taken = doc.exists();
                    lastCheckedUsernameLower = lower;
                    lastCheckedUsernameAvailable = !taken;

                    if (taken) {
                        showError(tilId, "이미 사용 중인 아이디입니다.");
                    } else {
                        showOk(tilId, "사용 가능한 아이디입니다.");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "checkUsernameAvailability error", e);
                    toast("아이디 확인 중 오류가 발생했습니다. 다시 시도해 주세요.");
                })
                .addOnCompleteListener(t -> setUiEnabled(true));
    }

    // ─────────────────────────────────────────────────────────────
    // 2) 이메일 가용성 체크 (fetchSignInMethodsForEmail)
    // ─────────────────────────────────────────────────────────────
    private void checkEmailAvailability() {
        String email = normalizeEmail(text(etEmail));
        if (!validateEmail(email)) return;

        setUiEnabled(false);
        mAuth.fetchSignInMethodsForEmail(email)
                .addOnSuccessListener(result -> {
                    boolean available = result == null
                            || result.getSignInMethods() == null
                            || result.getSignInMethods().isEmpty();
                    if (available) {
                        showOk(tilEmail, "사용 가능한 이메일입니다.");
                    } else {
                        showError(tilEmail, "이미 가입된 이메일입니다.");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "checkEmailAvailability error", e);
                    toast("이메일 확인 중 오류가 발생했습니다. 다시 시도해 주세요.");
                })
                .addOnCompleteListener(t -> setUiEnabled(true));
    }

    // ─────────────────────────────────────────────────────────────
    // 3) 회원가입 시도 → createUser → 이메일 인증 메일 발송 → Firestore 배치 저장
    //    (usernames/{lower} + users/{uid} 동시 커밋)
    // ─────────────────────────────────────────────────────────────
    private void attemptRegister() {
        // 입력값
        String username = normalizeUsername(text(etId));
        String email    = normalizeEmail(text(etEmail));
        String p1       = text(etPassword);
        String p2       = text(etPasswordConfirm);

        // 로컬 검증
        if (!validateUsername(username)) return;
        if (!validateEmail(email)) return;
        if (!validatePasswords(p1, p2)) return;

        // (선택) UX: 최근 확인한 아이디와 다르면 사전 체크 권장 메시지
        String lower = username.toLowerCase(Locale.ROOT);
        if (!(lower.equals(lastCheckedUsernameLower) && lastCheckedUsernameAvailable)) {
            // 강제는 아님 — 서버에서 다시 충돌 검증함
            setHelper(tilId, "Tip: 중복확인을 먼저 누르면 더 빨라요. (바로 진행해도 괜찮습니다)");
        }

        setUiEnabled(false);

        // 3-1) 서버에서 아이디 한 번 더 확인(레이스 방지)
        db.collection("usernames").document(lower).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        setUiEnabled(true);
                        showError(tilId, "이미 사용 중인 아이디입니다.");
                        return;
                    }
                    // 3-2) 계정 생성
                    createUserThenSaveProfile(username, email, p1);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "final username availability check error", e);
                    setUiEnabled(true);
                    toast("아이디 확인 중 오류가 발생했습니다. 다시 시도해 주세요.");
                });
    }

    private void createUserThenSaveProfile(@NonNull String username, @NonNull String email, @NonNull String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(this, (AuthResult result) -> {
                    FirebaseUser user = result.getUser();
                    if (user == null) {
                        setUiEnabled(true);
                        toast("회원가입 처리 중 문제가 발생했습니다. 다시 시도해 주세요.");
                        return;
                    }

                    // 표시 이름 업데이트(선호)
                    user.updateProfile(new UserProfileChangeRequest.Builder()
                                    .setDisplayName(username)
                                    .build())
                            .addOnFailureListener(e -> Log.w(TAG, "updateProfile failed", e));

                    // 인증 메일 발송(실패해도 프로필 저장은 진행)
                    user.sendEmailVerification()
                            .addOnSuccessListener(v -> toast("인증 메일을 보냈습니다. 메일함을 확인해 주세요."))
                            .addOnFailureListener(e -> {
                                Log.w(TAG, "sendEmailVerification failed", e);
                                toast("인증 메일 발송에 실패했습니다. 나중에 다시 시도해 주세요.");
                            });

                    // Firestore 저장(배치) → users/{uid} + usernames/{lower}
                    saveUserProfileBatch(user, username, email);
                })
                .addOnFailureListener(e -> {
                    setUiEnabled(true);
                    handleSignupAuthError(e);
                });
    }

    // Auth 에러 메세지 구체화
    private void handleSignupAuthError(Exception e) {
        String code = (e instanceof FirebaseAuthException)
                ? ((FirebaseAuthException) e).getErrorCode()
                : null;
        Log.e(TAG, "createUser failed" + (code != null ? " code=" + code : ""), e);

        if ("ERROR_EMAIL_ALREADY_IN_USE".equals(code)) {
            showError(tilEmail, "이미 가입된 이메일입니다.");
            return;
        }
        if ("ERROR_INVALID_EMAIL".equals(code)) {
            showError(tilEmail, "이메일 형식이 올바르지 않습니다.");
            return;
        }
        if ("ERROR_WEAK_PASSWORD".equals(code)) {
            showError(tilPassword, "비밀번호가 너무 약합니다. (최소 6자 이상 권장)");
            return;
        }
        toast("회원가입에 실패했습니다. 다시 시도해 주세요.");
    }

    // Firestore 배치 저장 (규칙과 일치: 동일 배치에 usernames + users)
    private void saveUserProfileBatch(@NonNull FirebaseUser fUser,
                                      @NonNull String username,
                                      @NonNull String email) {

        String uid   = fUser.getUid();
        String lower = username.toLowerCase(Locale.ROOT);

        DocumentReference userRef  = db.collection("users").document(uid);
        DocumentReference nameRef  = db.collection("usernames").document(lower);

        Map<String, Object> userDoc = new HashMap<>();
        userDoc.put("uid", uid);
        userDoc.put("username", username);
        userDoc.put("usernameLower", lower);
        userDoc.put("email", email);
        userDoc.put("emailVerified", fUser.isEmailVerified());
        userDoc.put("createdAt", FieldValue.serverTimestamp());
        userDoc.put("provider", "password");

        Map<String, Object> unameDoc = new HashMap<>();
        unameDoc.put("uid", uid);
        unameDoc.put("createdAt", FieldValue.serverTimestamp());

        WriteBatch batch = db.batch();
        // 규칙 요구사항: 같은 배치에서 두 문서를 함께 작성
        batch.set(nameRef, unameDoc);
        batch.set(userRef, userDoc);

        batch.commit()
                .addOnSuccessListener(v -> {
                    Log.d(TAG, "Firestore batch commit SUCCESS: uid=" + uid + ", username=" + username + ", email=" + email);
                    toast("회원가입이 완료되었습니다. 이메일 인증 후 로그인해 주세요.");

                    // 안전하게 초기 화면으로 유도: 가입 직후 자동 로그인 상태 → 로그아웃 유도 (선호 정책)
                    try {
                        FirebaseAuth.getInstance().signOut();
                    } catch (Exception ignore) {}

                    // 로그인 화면으로 이동
                    startActivity(new Intent(BackupJoinActivity.this, LoginActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Firestore batch commit FAILED. uid=" + uid + ", username=" + username + ", email=" + email, e);
                    // 대표적인 케이스: 동시에 같은 아이디를 누군가 등록한 경쟁 상태
                    toast("아이디가 방금 사용되었습니다. 다른 아이디를 선택해 주세요.");
                    setUiEnabled(true);
                });
    }

    // ─────────────────────────────────────────────────────────────
    // 유효성 체크 & 헬퍼/에러 표기
    // ─────────────────────────────────────────────────────────────
    private boolean validateUsername(@NonNull String username) {
        if (username.isEmpty()) {
            showError(tilId, "아이디를 입력해 주세요.");
            return false;
        }
        // 영문/숫자 4~16자
        if (!username.matches("^[a-zA-Z0-9]{4,16}$")) {
            showError(tilId, "아이디는 영문/숫자 4~16자여야 합니다.");
            return false;
        }
        clearError(tilId);
        return true;
    }

    private boolean validateEmail(@NonNull String email) {
        if (email.isEmpty()) {
            showError(tilEmail, "이메일을 입력해 주세요.");
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError(tilEmail, "이메일 형식이 올바르지 않습니다.");
            return false;
        }
        clearError(tilEmail);
        return true;
    }

    private boolean validatePasswords(@NonNull String p1, @NonNull String p2) {
        if (p1.isEmpty()) {
            showError(tilPassword, "비밀번호를 입력해 주세요.");
            return false;
        }
        if (p1.length() < 6) {
            showError(tilPassword, "비밀번호는 최소 6자 이상이어야 합니다.");
            return false;
        }
        if (p2.isEmpty()) {
            showError(tilPasswordConfirm, "비밀번호를 다시 입력해 주세요.");
            return false;
        }
        if (!p1.equals(p2)) {
            showError(tilPasswordConfirm, "비밀번호가 일치하지 않습니다.");
            return false;
        }
        clearError(tilPassword);
        clearError(tilPasswordConfirm);
        return true;
    }

    private void showError(TextInputLayout til, String msg) {
        if (til == null) return;
        til.setError(msg);
        setHelper(til, null);
        // 포커스 이동
        if (til.getEditText() != null) {
            til.getEditText().requestFocus();
        }
    }

    private void showOk(TextInputLayout til, String helper) {
        if (til == null) return;
        til.setError(null);
        setHelper(til, helper);
    }

    private void clearError(TextInputLayout til) {
        if (til == null) return;
        if (til.getError() != null) til.setError(null);
    }

    private void setHelper(TextInputLayout til, String helper) {
        if (til == null) return;
        til.setHelperText(helper);
    }

    // ─────────────────────────────────────────────────────────────
    // 유틸
    // ─────────────────────────────────────────────────────────────
    private String text(TextInputEditText et) {
        return (et != null && et.getText() != null) ? et.getText().toString() : "";
    }

    /** 이메일: trim + NFC + 소문자(도메인 파트만 소문자지만, 여기선 전체 소문자로 단순화) */
    private String normalizeEmail(String raw) {
        String s = raw == null ? "" : raw;
        s = Normalizer.normalize(s, Normalizer.Form.NFC).trim();
        return s.toLowerCase(Locale.ROOT);
    }

    /** 아이디: 공백 제거 + NFC 정규화 */
    private String normalizeUsername(String raw) {
        String s = raw == null ? "" : raw.trim();
        return Normalizer.normalize(s, Normalizer.Form.NFC);
    }

    private void setUiEnabled(boolean enabled) {
        if (btnCheckId != null)     btnCheckId.setEnabled(enabled);
        if (btnVerifyEmail != null) btnVerifyEmail.setEnabled(enabled);
        if (btnRegister != null)    btnRegister.setEnabled(enabled);

        if (etId != null)               etId.setEnabled(enabled);
        if (etEmail != null)            etEmail.setEnabled(enabled);
        if (etPassword != null)         etPassword.setEnabled(enabled);
        if (etPasswordConfirm != null)  etPasswordConfirm.setEnabled(enabled);

        float alpha = enabled ? 1f : 0.5f;
        if (btnCheckId != null)     btnCheckId.setAlpha(alpha);
        if (btnVerifyEmail != null) btnVerifyEmail.setAlpha(alpha);
        if (btnRegister != null)    btnRegister.setAlpha(alpha);
    }

    private void toast(@NonNull String msg) {
        if (isFinishing() || isDestroyed()) return;
        Toast.makeText(BackupJoinActivity.this, msg, Toast.LENGTH_SHORT).show();
    }

    // 간단 TextWatcher
    private static class SimpleWatcher implements android.text.TextWatcher {
        private final Runnable onChange;
        SimpleWatcher(Runnable onChange) { this.onChange = onChange; }
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) { if (onChange != null) onChange.run(); }
        @Override public void afterTextChanged(android.text.Editable s) {}
    }
}
