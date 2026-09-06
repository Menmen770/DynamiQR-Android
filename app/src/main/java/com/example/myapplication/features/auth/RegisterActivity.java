package com.example.myapplication.features.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import com.example.myapplication.DynamiQRApplication;
import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.core.base.BaseActivity;
import com.example.myapplication.core.i18n.AppI18n;
import com.example.myapplication.data.local.AuthManager;
import com.example.myapplication.data.models.LoginResponse;
import com.example.myapplication.data.repository.AuthRepository;
import com.example.myapplication.databinding.ActivityRegisterBinding;
import java.util.HashMap;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends BaseActivity<ActivityRegisterBinding> {

    private AuthManager authManager;
    private AuthRepository authRepository;

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

        binding.registerButton.setOnClickListener(v -> handleRegister());
        binding.loginLink.setOnClickListener(v -> finish());
    }

    private void handleRegister() {
        String name = binding.nameInput.getText().toString().trim();
        String email = binding.emailInput.getText().toString().trim();
        String password = binding.passwordInput.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_fill_all), Toast.LENGTH_SHORT).show();
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
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse res = response.body();
                    if (res.getToken() != null) {
                        authManager.saveSession(res.getToken(), res.getUser());
                        startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                        finishAffinity();
                    } else {
                        Intent intent = new Intent(RegisterActivity.this, VerifyEmailActivity.class);
                        intent.putExtra("email", email);
                        startActivity(intent);
                        finish();
                    }
                } else {
                    Toast.makeText(RegisterActivity.this,
                            AppI18n.t(RegisterActivity.this, "auth", "errors.registerFailed",
                                    "Registration failed"),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                setLoading(false);
                String detail = t.getMessage() != null ? t.getMessage() : "";
                Toast.makeText(RegisterActivity.this,
                        getString(R.string.settings_network_error)
                                + (detail.isEmpty() ? "" : ": " + detail),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setLoading(boolean loading) {
        binding.registerButton.setEnabled(!loading);
    }
}
