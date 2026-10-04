package com.dynamiqr.android.features.auth;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import com.dynamiqr.android.DynamiQRApplication;
import com.dynamiqr.android.MainActivity;
import com.dynamiqr.android.R;
import com.dynamiqr.android.core.base.BaseActivity;
import com.dynamiqr.android.core.utils.IsraeliIdValidator;
import com.dynamiqr.android.data.local.AuthManager;
import com.dynamiqr.android.data.models.LoginResponse;
import com.dynamiqr.android.data.models.User;
import com.dynamiqr.android.data.repository.AuthRepository;
import com.dynamiqr.android.data.repository.LocalUserRepository;
import com.dynamiqr.android.databinding.ActivityRegisterBinding;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Hybrid register: existing backend account (for QR) + Room course profile (ת.ז./phone/DOB).
 * Backend schema unchanged — only name/email/password go to the API.
 */
public class RegisterActivity extends BaseActivity<ActivityRegisterBinding> {

    private static final String STATE_BIRTH = "state_birth";

    private AuthManager authManager;
    private AuthRepository authRepository;
    private LocalUserRepository localUserRepository;
    private String selectedBirthDate = "";

    @Override
    protected ActivityRegisterBinding inflateBinding() {
        return ActivityRegisterBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DynamiQRApplication app = DynamiQRApplication.getInstance();
        authManager = app.getAuthManager();
        authRepository = app.getAuthRepository();
        localUserRepository = app.getLocalUserRepository();

        if (savedInstanceState != null) {
            selectedBirthDate = savedInstanceState.getString(STATE_BIRTH, "");
            if (!selectedBirthDate.isEmpty()) {
                binding.birthDateInput.setText(selectedBirthDate);
            }
        }

        binding.birthDateInput.setOnClickListener(v -> showBirthDatePicker());
        binding.registerButton.setOnClickListener(v -> handleRegister());
        binding.loginLink.setOnClickListener(v -> finish());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_BIRTH, selectedBirthDate);
    }

    private void showBirthDatePicker() {
        Calendar calendar = Calendar.getInstance();
        if (!selectedBirthDate.isEmpty()) {
            try {
                String[] parts = selectedBirthDate.split("-");
                calendar.set(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]) - 1,
                        Integer.parseInt(parts[2]));
            } catch (Exception ignored) {
            }
        }
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedBirthDate = String.format(Locale.US, "%04d-%02d-%02d",
                            year, month + 1, dayOfMonth);
                    binding.birthDateInput.setText(selectedBirthDate);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        dialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        dialog.show();
    }

    private void handleRegister() {
        String name = textOf(binding.nameInput);
        String idNumber = textOf(binding.idNumberInput);
        String phone = textOf(binding.phoneInput);
        String email = textOf(binding.emailInput);
        String password = textOf(binding.passwordInput);
        String birth = selectedBirthDate;

        if (name.isEmpty() || idNumber.isEmpty() || phone.isEmpty()
                || email.isEmpty() || password.isEmpty() || birth.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_fill_all), Toast.LENGTH_SHORT).show();
            return;
        }
        if (!IsraeliIdValidator.isValid(idNumber)) {
            Toast.makeText(this, getString(R.string.error_invalid_id), Toast.LENGTH_SHORT).show();
            return;
        }
        if (phone.length() < 9) {
            Toast.makeText(this, getString(R.string.error_invalid_phone), Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.length() < 6) {
            Toast.makeText(this, getString(R.string.error_password_short), Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        Map<String, String> body = new HashMap<>();
        body.put("fullName", name);
        body.put("email", email);
        body.put("password", password);

        authRepository.register(body).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse res = response.body();
                    if (res.getToken() != null) {
                        saveLocalThenEnter(res.getToken(), res.getUser(),
                                name, email, password, idNumber, phone, birth);
                        return;
                    }
                    // Email verification required — still store course profile locally.
                    localUserRepository.upsertCourseProfile(name, email, password, idNumber, phone, birth,
                            new LocalUserRepository.Callback<User>() {
                                @Override
                                public void onSuccess(User result) {
                                    setLoading(false);
                                    Intent intent = new Intent(RegisterActivity.this, VerifyEmailActivity.class);
                                    intent.putExtra("email", email);
                                    startActivity(intent);
                                    finish();
                                }

                                @Override
                                public void onError(String message) {
                                    setLoading(false);
                                    Intent intent = new Intent(RegisterActivity.this, VerifyEmailActivity.class);
                                    intent.putExtra("email", email);
                                    startActivity(intent);
                                    finish();
                                }
                            });
                    return;
                }
                setLoading(false);
                Toast.makeText(RegisterActivity.this,
                        getString(R.string.error_register_failed), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                setLoading(false);
                String detail = t.getMessage() != null ? t.getMessage() : "";
                Toast.makeText(RegisterActivity.this,
                        getString(R.string.error_network)
                                + (detail.isEmpty() ? "" : ": " + detail),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveLocalThenEnter(String token, User serverUser,
                                    String name, String email, String password,
                                    String idNumber, String phone, String birth) {
        localUserRepository.upsertCourseProfile(name, email, password, idNumber, phone, birth,
                new LocalUserRepository.Callback<User>() {
                    @Override
                    public void onSuccess(User local) {
                        setLoading(false);
                        User merged = LocalUserRepository.mergeServerAndLocal(serverUser, local);
                        authManager.saveSession(token, merged);
                        startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                        finishAffinity();
                    }

                    @Override
                    public void onError(String message) {
                        setLoading(false);
                        if ("id_exists".equals(message) || "email_exists".equals(message)) {
                            Toast.makeText(RegisterActivity.this, mapError(message), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        // Backend account exists — enter anyway; profile extras can be filled later.
                        authManager.saveSession(token, serverUser);
                        startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                        finishAffinity();
                    }
                });
    }

    private String mapError(String code) {
        if ("email_exists".equals(code)) {
            return getString(R.string.error_email_exists);
        }
        if ("id_exists".equals(code)) {
            return getString(R.string.error_id_exists);
        }
        return getString(R.string.error_register_failed);
    }

    private static String textOf(android.widget.EditText editText) {
        return editText.getText() != null ? editText.getText().toString().trim() : "";
    }

    private void setLoading(boolean loading) {
        binding.registerButton.setEnabled(!loading);
        binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }
}
