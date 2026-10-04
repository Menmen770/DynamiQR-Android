package com.example.myapplication.features.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import com.example.myapplication.DynamiQRApplication;
import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.core.base.BaseActivity;
import com.example.myapplication.data.local.AuthManager;
import com.example.myapplication.data.models.User;
import com.example.myapplication.data.repository.LocalUserRepository;
import com.example.myapplication.databinding.ActivityLoginBinding;

public class LoginActivity extends BaseActivity<ActivityLoginBinding> {

    private AuthManager authManager;
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
        localUserRepository = app.getLocalUserRepository();

        if (authManager.isLoggedIn()) {
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
        localUserRepository.login(email, password, new LocalUserRepository.Callback<User>() {
            @Override
            public void onSuccess(User result) {
                setLoading(false);
                authManager.saveLocalSession(result);
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            }

            @Override
            public void onError(String message) {
                setLoading(false);
                Toast.makeText(LoginActivity.this,
                        getString(R.string.error_login_failed), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setLoading(boolean loading) {
        binding.loginButton.setEnabled(!loading);
        binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }
}
