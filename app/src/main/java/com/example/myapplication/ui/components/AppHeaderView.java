package com.example.myapplication.ui.components;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;
import androidx.annotation.Nullable;
import com.example.myapplication.DynamiQRApplication;
import com.example.myapplication.R;
import com.example.myapplication.data.local.AuthManager;
import com.example.myapplication.data.models.User;
import com.example.myapplication.data.repository.AuthRepository;
import com.example.myapplication.databinding.BottomSheetAccountMenuBinding;
import com.example.myapplication.databinding.ViewAppHeaderBinding;
import com.example.myapplication.features.auth.LoginActivity;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AppHeaderView extends FrameLayout {

    private ViewAppHeaderBinding binding;
    private AuthManager authManager;
    private AuthRepository authRepository;
    private OnLogoClickListener logoClickListener;

    public interface OnLogoClickListener {
        void onLogoClick();
    }

    public AppHeaderView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        binding = ViewAppHeaderBinding.inflate(LayoutInflater.from(context), this, true);
        authManager = DynamiQRApplication.getInstance().getAuthManager();
        authRepository = DynamiQRApplication.getInstance().getAuthRepository();
        refreshUser();
        binding.userSection.setOnClickListener(v -> showAccountMenu());
        binding.brandLogo.setOnClickListener(v -> {
            if (logoClickListener != null) {
                logoClickListener.onLogoClick();
            }
        });
    }

    public void setOnLogoClickListener(OnLogoClickListener listener) {
        this.logoClickListener = listener;
    }

    public void refreshUser() {
        User user = authManager.getUser();
        binding.greetingText.setText(getGreeting());
        if (user != null) {
            binding.userNameText.setText(user.getDisplayName());
            binding.avatarButton.setText(user.getInitial());
        } else {
            binding.userNameText.setText("משתמש");
            binding.avatarButton.setText("?");
        }
    }

    private String getGreeting() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (hour >= 5 && hour < 12) {
            return "בוקר טוב";
        }
        if (hour >= 12 && hour < 17) {
            return "צהריים טובים";
        }
        if (hour >= 17 && hour < 21) {
            return "ערב טוב";
        }
        return "לילה טוב";
    }

    private void showAccountMenu() {
        BottomSheetDialog dialog = new BottomSheetDialog(getContext());
        BottomSheetAccountMenuBinding menuBinding =
                BottomSheetAccountMenuBinding.inflate(LayoutInflater.from(getContext()));
        dialog.setContentView(menuBinding.getRoot());

        User user = authManager.getUser();
        if (user != null) {
            menuBinding.profileNameInput.setText(user.getFullName() != null ? user.getFullName() : "");
            menuBinding.profileEmailInput.setText(user.getEmail() != null ? user.getEmail() : "");
        }

        menuBinding.saveProfileButton.setOnClickListener(v -> {
            String name = menuBinding.profileNameInput.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(getContext(), "הזן שם", Toast.LENGTH_SHORT).show();
                return;
            }
            Map<String, String> body = new HashMap<>();
            body.put("fullName", name);
            authRepository.updateProfile(body).enqueue(new Callback<Map<String, User>>() {
                @Override
                public void onResponse(Call<Map<String, User>> call, Response<Map<String, User>> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().get("user") != null) {
                        User updated = response.body().get("user");
                        authManager.saveUser(updated);
                        refreshUser();
                        Toast.makeText(getContext(), "הפרופיל נשמר", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    } else {
                        Toast.makeText(getContext(), "שמירה נכשלה", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Map<String, User>> call, Throwable t) {
                    Toast.makeText(getContext(), "שגיאת תקשורת", Toast.LENGTH_SHORT).show();
                }
            });
        });

        menuBinding.logoutButton.setOnClickListener(v -> {
            authRepository.logout().enqueue(new Callback<Map<String, Object>>() {
                @Override
                public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                    performLogout();
                }

                @Override
                public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                    performLogout();
                }
            });
            dialog.dismiss();
        });

        dialog.show();
    }

    private void performLogout() {
        authManager.clear();
        Intent intent = new Intent(getContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        getContext().startActivity(intent);
    }
}
