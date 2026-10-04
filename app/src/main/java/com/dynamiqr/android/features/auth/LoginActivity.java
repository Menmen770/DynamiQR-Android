package com.dynamiqr.android.features.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import com.dynamiqr.android.DynamiQRApplication;
import com.dynamiqr.android.MainActivity;
import com.dynamiqr.android.R;
import com.dynamiqr.android.core.base.BaseActivity;
import com.dynamiqr.android.data.local.AuthManager;
import com.dynamiqr.android.data.models.LoginResponse;
import com.dynamiqr.android.data.models.User;
import com.dynamiqr.android.data.repository.AuthRepository;
import com.dynamiqr.android.data.repository.LocalUserRepository;
import com.dynamiqr.android.databinding.ActivityLoginBinding;
import java.util.HashMap;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Hybrid auth: backend JWT for QR APIs + Room course profile extras.
 */
public class LoginActivity extends BaseActivity<ActivityLoginBinding> {

    private AuthManager authManager;
    private AuthRepository authRepository;
    private LocalUserRepository localUserRepository;

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
        localUserRepository = app.getLocalUserRepository();

        if (authManager.isLoggedIn() && !authManager.isLocalSession()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        binding.googleButton.setVisibility(View.GONE);
        binding.googleDivider.setVisibility(View.GONE);
        binding.loginButton.setOnClickListener(v -> handleLogin());
        binding.registerLink.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void handleLogin() {
        String email = binding.emailInput.getText() != null
                ? binding.emailInput.getText().toString().trim() : "";
        String password = binding.passwordInput.getText() != null
                ? binding.passwordInput.getText().toString().trim() : "";

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_fill_all), Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        Map<String, String> credentials = new HashMap<>();
        credentials.put("email", email);
        credentials.put("password", password);

        authRepository.login(credentials).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();
                    if (loginResponse.isNeedsEmailVerification()) {
                        setLoading(false);
                        Intent intent = new Intent(LoginActivity.this, VerifyEmailActivity.class);
                        String verifyEmail = loginResponse.getEmail() != null
                                ? loginResponse.getEmail() : email;
                        intent.putExtra("email", verifyEmail);
                        startActivity(intent);
                        return;
                    }
                    if (loginResponse.getToken() != null) {
                        finishLoginWithLocalProfile(loginResponse.getToken(),
                                loginResponse.getUser(), email, password);
                        return;
                    }
                    setLoading(false);
                    Toast.makeText(LoginActivity.this,
                            loginResponse.getError() != null
                                    ? loginResponse.getError()
                                    : getString(R.string.error_login_failed),
                            Toast.LENGTH_SHORT).show();
                } else {
                    setLoading(false);
                    Toast.makeText(LoginActivity.this,
                            getString(R.string.error_login_failed), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                setLoading(false);
                String detail = t.getMessage() != null ? t.getMessage() : "";
                Toast.makeText(LoginActivity.this,
                        getString(R.string.error_network)
                                + (detail.isEmpty() ? "" : ": " + detail),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void finishLoginWithLocalProfile(String token, User serverUser,
                                             String email, String password) {
        localUserRepository.findByEmail(email, new LocalUserRepository.Callback<User>() {
            @Override
            public void onSuccess(User local) {
                if (local != null) {
                    completeSession(token, LocalUserRepository.mergeServerAndLocal(serverUser, local));
                    return;
                }
                // First login on this device: create a Room shell; user can fill course fields in profile.
                String name = serverUser != null && serverUser.getFullName() != null
                        ? serverUser.getFullName() : "";
                localUserRepository.upsertCourseProfile(name, email, password, "", "", "",
                        new LocalUserRepository.Callback<User>() {
                            @Override
                            public void onSuccess(User created) {
                                completeSession(token,
                                        LocalUserRepository.mergeServerAndLocal(serverUser, created));
                            }

                            @Override
                            public void onError(String message) {
                                // Still allow app use with backend session; course fields later.
                                completeSession(token, serverUser);
                            }
                        });
            }

            @Override
            public void onError(String message) {
                completeSession(token, serverUser);
            }
        });
    }

    private void completeSession(String token, User user) {
        setLoading(false);
        authManager.saveSession(token, user);
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
        finish();
    }

    private void setLoading(boolean loading) {
        binding.loginButton.setEnabled(!loading);
        binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }
}
