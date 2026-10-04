package com.dynamiqr.android.features.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import com.dynamiqr.android.DynamiQRApplication;
import com.dynamiqr.android.MainActivity;
import com.dynamiqr.android.R;
import com.dynamiqr.android.core.base.BaseActivity;
import com.dynamiqr.android.data.local.AuthManager;
import com.dynamiqr.android.data.models.LoginResponse;
import com.dynamiqr.android.data.repository.AuthRepository;
import com.dynamiqr.android.databinding.ActivityVerifyEmailBinding;
import java.util.HashMap;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VerifyEmailActivity extends BaseActivity<ActivityVerifyEmailBinding> {

    private AuthManager authManager;
    private AuthRepository authRepository;
    private String email;

    @Override
    protected ActivityVerifyEmailBinding inflateBinding() {
        return ActivityVerifyEmailBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DynamiQRApplication app = DynamiQRApplication.getInstance();
        authManager = app.getAuthManager();
        authRepository = app.getAuthRepository();

        email = getIntent().getStringExtra("email");
        binding.emailText.setText(email != null ? email : "");

        binding.verifyButton.setOnClickListener(v -> handleVerify());
        binding.resendButton.setOnClickListener(v -> handleResend());
    }

    private void handleVerify() {
        String code = binding.codeInput.getText().toString().trim();
        if (code.length() != 6) {
            Toast.makeText(this,
                    getString(R.string.auth_errors_code_length),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        binding.verifyButton.setEnabled(false);

        Map<String, String> body = new HashMap<>();
        body.put("email", email);
        body.put("code", code);

        authRepository.verifyEmail(body).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                binding.verifyButton.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse res = response.body();
                    if (res.getToken() != null) {
                        authManager.saveSession(res.getToken(), res.getUser());
                        startActivity(new Intent(VerifyEmailActivity.this, MainActivity.class));
                        finishAffinity();
                    }
                } else {
                    Toast.makeText(VerifyEmailActivity.this,
                            getString(R.string.auth_errors_verify_failed),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                binding.verifyButton.setEnabled(true);
                String detail = t.getMessage() != null ? t.getMessage() : "";
                Toast.makeText(VerifyEmailActivity.this,
                        getString(R.string.settings_network_error)
                                + (detail.isEmpty() ? "" : ": " + detail),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleResend() {
        if (email == null || email.isEmpty()) {
            return;
        }

        binding.resendButton.setEnabled(false);

        Map<String, String> body = new HashMap<>();
        body.put("email", email);

        authRepository.resendVerification(body).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                binding.resendButton.setEnabled(true);
                if (response.isSuccessful()) {
                    Toast.makeText(VerifyEmailActivity.this,
                            getString(R.string.auth_errors_code_sent),
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(VerifyEmailActivity.this,
                            getString(R.string.auth_errors_resend_failed),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                binding.resendButton.setEnabled(true);
                String detail = t.getMessage() != null ? t.getMessage() : "";
                Toast.makeText(VerifyEmailActivity.this,
                        getString(R.string.settings_network_error)
                                + (detail.isEmpty() ? "" : ": " + detail),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
