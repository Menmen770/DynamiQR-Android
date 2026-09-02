package com.example.myapplication.features.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import com.example.myapplication.DynamiQRApplication;
import com.example.myapplication.MainActivity;
import com.example.myapplication.core.base.BaseActivity;
import com.example.myapplication.data.local.AuthManager;
import com.example.myapplication.data.models.LoginResponse;
import com.example.myapplication.data.repository.AuthRepository;
import com.example.myapplication.databinding.ActivityLoginBinding;
import java.util.HashMap;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends BaseActivity<ActivityLoginBinding> {

    private AuthManager authManager;
    private AuthRepository authRepository;

    @Override
    protected ActivityLoginBinding inflateBinding() {
        return ActivityLoginBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DynamiQRApplication app = DynamiQRApplication.getInstance();
        authManager = app.getAuthManager();
        authRepository = app.getAuthRepository();

        if (authManager.getToken() != null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        binding.loginButton.setOnClickListener(v -> handleLogin());
        binding.registerLink.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
        binding.googleButton.setOnClickListener(v ->
                Toast.makeText(this, "התחברות עם Google בקרוב", Toast.LENGTH_SHORT).show());
    }

    private void handleLogin() {
        String email = binding.emailInput.getText().toString().trim();
        String password = binding.passwordInput.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "אנא מלא את כל השדות", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        Map<String, String> credentials = new HashMap<>();
        credentials.put("email", email);
        credentials.put("password", password);

        authRepository.login(credentials).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();
                    if (loginResponse.isNeedsEmailVerification()) {
                        Intent intent = new Intent(LoginActivity.this, VerifyEmailActivity.class);
                        String verifyEmail = loginResponse.getEmail() != null
                                ? loginResponse.getEmail() : email;
                        intent.putExtra("email", verifyEmail);
                        startActivity(intent);
                        return;
                    }
                    if (loginResponse.getToken() != null) {
                        authManager.saveToken(loginResponse.getToken());
                        if (loginResponse.getUser() != null) {
                            authManager.saveUser(loginResponse.getUser());
                        }
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    } else if (loginResponse.getError() != null) {
                        Toast.makeText(LoginActivity.this, loginResponse.getError(), Toast.LENGTH_SHORT).show();
                    } else if (loginResponse.getMessage() != null) {
                        Toast.makeText(LoginActivity.this, loginResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "התחברות נכשלה", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                setLoading(false);
                Toast.makeText(LoginActivity.this, "שגיאת תקשורת: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setLoading(boolean loading) {
        binding.loginButton.setEnabled(!loading);
        binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }
}
