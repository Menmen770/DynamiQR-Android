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
import com.example.myapplication.databinding.ActivityRegisterBinding;
import java.util.HashMap;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends BaseActivity<ActivityRegisterBinding> {
    private AuthManager authManager;
    private ApiService apiService;

    @Override
    protected ActivityRegisterBinding inflateBinding() {
        return ActivityRegisterBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        authManager = new AuthManager(this);
        apiService = RetrofitClient.getService(authManager);

        binding.registerButton.setOnClickListener(v -> handleRegister());
        binding.loginLink.setOnClickListener(v -> finish());
    }

    private void handleRegister() {
        String name = binding.nameInput.getText().toString().trim();
        String email = binding.emailInput.getText().toString().trim();
        String password = binding.passwordInput.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "אנא מלא את כל השדות", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.registerButton.setEnabled(false);
        Map<String, String> body = new HashMap<>();
        body.put("fullName", name);
        body.put("email", email);
        body.put("password", password);

        apiService.register(body).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                binding.registerButton.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse res = response.body();
                    if (res.getToken() != null) {
                        authManager.saveToken(res.getToken());
                        startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                        finishAffinity();
                    } else {
                        Intent intent = new Intent(RegisterActivity.this, VerifyEmailActivity.class);
                        intent.putExtra("email", email);
                        startActivity(intent);
                        finish();
                    }
                } else {
                    Toast.makeText(RegisterActivity.this, "הרשמה נכשלה", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                binding.registerButton.setEnabled(true);
                Toast.makeText(RegisterActivity.this, "שגיאה: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
