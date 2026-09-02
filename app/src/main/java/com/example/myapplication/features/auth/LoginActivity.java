package com.example.myapplication.features.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import com.example.myapplication.MainActivity;
import com.example.myapplication.core.base.BaseActivity;
import com.example.myapplication.data.local.AuthManager;
import com.example.myapplication.data.models.LoginResponse;
import com.example.myapplication.data.api.ApiService;
import com.example.myapplication.data.api.RetrofitClient;
import com.example.myapplication.databinding.ActivityLoginBinding;
import java.util.HashMap;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends BaseActivity<ActivityLoginBinding> {
    private AuthManager authManager;
    private ApiService apiService;

    @Override
    protected ActivityLoginBinding inflateBinding() {
        return ActivityLoginBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        authManager = new AuthManager(this);
        apiService = RetrofitClient.getService(authManager);

        if (authManager.getToken() != null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }

        binding.loginButton.setOnClickListener(v -> handleLogin());
        binding.registerLink.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void handleLogin() {
        String email = binding.emailInput.getText().toString().trim();
        String password = binding.passwordInput.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "אנא מלא את כל השדות", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.loginButton.setEnabled(false);
        Map<String, String> credentials = new HashMap<>();
        credentials.put("email", email);
        credentials.put("password", password);

        apiService.login(credentials).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                binding.loginButton.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();
                    if (loginResponse.getToken() != null) {
                        authManager.saveToken(loginResponse.getToken());
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    } else if (loginResponse.getError() != null) {
                        Toast.makeText(LoginActivity.this, loginResponse.getError(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "התחברות נכשלה", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                binding.loginButton.setEnabled(true);
                Toast.makeText(LoginActivity.this, "שגיאת תקשורת: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
