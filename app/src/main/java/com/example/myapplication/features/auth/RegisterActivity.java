package com.example.myapplication.features.auth;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import com.example.myapplication.DynamiQRApplication;
import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.core.base.BaseActivity;
import com.example.myapplication.core.utils.IsraeliIdValidator;
import com.example.myapplication.data.local.AuthManager;
import com.example.myapplication.data.models.User;
import com.example.myapplication.data.repository.LocalUserRepository;
import com.example.myapplication.databinding.ActivityRegisterBinding;
import java.util.Calendar;
import java.util.Locale;

public class RegisterActivity extends BaseActivity<ActivityRegisterBinding> {

    private static final String STATE_BIRTH = "state_birth";

    private AuthManager authManager;
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
        localUserRepository.register(name, email, password, idNumber, phone, birth,
                new LocalUserRepository.Callback<User>() {
                    @Override
                    public void onSuccess(User result) {
                        setLoading(false);
                        authManager.saveLocalSession(result);
                        startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                        finishAffinity();
                    }

                    @Override
                    public void onError(String message) {
                        setLoading(false);
                        Toast.makeText(RegisterActivity.this, mapError(message), Toast.LENGTH_SHORT).show();
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
