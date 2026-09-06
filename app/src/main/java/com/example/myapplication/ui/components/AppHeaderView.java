package com.example.myapplication.ui.components;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.myapplication.DynamiQRApplication;
import com.example.myapplication.R;
import com.example.myapplication.core.i18n.AppI18n;
import com.example.myapplication.data.local.AppPreferences;
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
    private AppPreferences appPreferences;
    private OnLogoClickListener logoClickListener;

    public interface OnLogoClickListener {
        void onLogoClick();
    }

    public AppHeaderView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        binding = ViewAppHeaderBinding.inflate(LayoutInflater.from(context), this, true);
        authManager = DynamiQRApplication.getInstance().getAuthManager();
        authRepository = DynamiQRApplication.getInstance().getAuthRepository();
        appPreferences = new AppPreferences(context);
        applyStatusBarInset();
        refreshUser();

        binding.userSection.setClickable(true);
        binding.userSection.setFocusable(true);
        binding.userSection.setOnClickListener(v -> showAccountMenu());
        binding.avatarButton.setClickable(true);
        binding.avatarButton.setFocusable(true);
        binding.avatarButton.setOnClickListener(v -> showAccountMenu());
        binding.brandLogo.setOnClickListener(v -> {
            if (logoClickListener != null) {
                logoClickListener.onLogoClick();
            }
        });
    }

    private void applyStatusBarInset() {
        ViewCompat.setOnApplyWindowInsetsListener(this, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            v.setPadding(v.getPaddingLeft(), bars.top, v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });
        ViewCompat.requestApplyInsets(this);
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
            binding.userNameText.setText(AppI18n.t(getContext(), "common", "settings.userFallback",
                    getContext().getString(R.string.settings_user_fallback)));
            binding.avatarButton.setText("?");
        }
    }

    private String getGreeting() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        Context ctx = getContext();
        if (hour >= 5 && hour < 12) {
            return AppI18n.t(ctx, "common", "greeting.morning", "Good morning");
        }
        if (hour >= 12 && hour < 17) {
            return AppI18n.t(ctx, "common", "greeting.afternoon", "Good afternoon");
        }
        if (hour >= 17 && hour < 21) {
            return AppI18n.t(ctx, "common", "greeting.evening", "Good evening");
        }
        return AppI18n.t(ctx, "common", "greeting.night", "Good night");
    }

    private void showAccountMenu() {
        BottomSheetDialog dialog = new BottomSheetDialog(getContext());
        BottomSheetAccountMenuBinding menu =
                BottomSheetAccountMenuBinding.inflate(LayoutInflater.from(getContext()));
        dialog.setContentView(menu.getRoot());
        localizeAccountMenu(menu);

        User user = authManager.getUser();
        if (user != null) {
            menu.profileNameInput.setText(user.getFullName() != null ? user.getFullName() : "");
            menu.profileEmailInput.setText(user.getEmail() != null ? user.getEmail() : "");
        }

        final boolean[] profileOpen = {false};
        final boolean[] languageOpen = {false};

        menu.profileHeaderRow.setOnClickListener(v -> {
            profileOpen[0] = !profileOpen[0];
            menu.profileExpandPanel.setVisibility(profileOpen[0] ? View.VISIBLE : View.GONE);
            menu.profileChevron.setImageResource(
                    profileOpen[0] ? R.drawable.ic_chevron_up : R.drawable.ic_chevron_down);
        });

        menu.saveProfileButton.setOnClickListener(v -> saveProfile(menu, dialog));

        boolean isNight = appPreferences.isDarkMode();
        menu.dayNightToggle.setNight(isNight, false);
        menu.themeModeLabel.setText(isNight
                ? AppI18n.t(getContext(), "common", "theme.night", getContext().getString(R.string.settings_night_mode))
                : AppI18n.t(getContext(), "common", "theme.day", getContext().getString(R.string.settings_day_mode)));
        menu.dayNightToggle.setOnCheckedChangeListener(night -> {
            appPreferences.setDarkMode(night);
            menu.themeModeLabel.setText(night
                    ? AppI18n.t(getContext(), "common", "theme.night", getContext().getString(R.string.settings_night_mode))
                    : AppI18n.t(getContext(), "common", "theme.day", getContext().getString(R.string.settings_day_mode)));
            dialog.dismiss();
            if (getContext() instanceof Activity) {
                ((Activity) getContext()).recreate();
            }
        });

        menu.currentLanguageLabel.setText(AppPreferences.languageLabel(appPreferences.getLanguage()));
        menu.languageHeaderRow.setOnClickListener(v -> {
            languageOpen[0] = !languageOpen[0];
            menu.languageOptionsPanel.setVisibility(languageOpen[0] ? View.VISIBLE : View.GONE);
            menu.languageChevron.setImageResource(
                    languageOpen[0] ? R.drawable.ic_chevron_up : R.drawable.ic_chevron_down);
            if (languageOpen[0] && menu.languageOptionsPanel.getChildCount() == 0) {
                populateLanguageOptions(menu, dialog);
            }
        });

        menu.logoutButton.setOnClickListener(v -> {
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

    private void localizeAccountMenu(BottomSheetAccountMenuBinding menu) {
        Context ctx = getContext();
        menu.settingsTitle.setText(AppI18n.t(ctx, "common", "settings.title",
                ctx.getString(R.string.settings_title)));
        menu.profileHeaderLabel.setText(AppI18n.t(ctx, "common", "settings.updateName",
                ctx.getString(R.string.settings_update_name)));
        menu.saveProfileButton.setText(AppI18n.t(ctx, "common", "settings.saveProfile",
                ctx.getString(R.string.settings_save_profile)));
        menu.displayModeHeaderLabel.setText(AppI18n.t(ctx, "common", "settings.displayMode",
                ctx.getString(R.string.settings_display_mode)));
        menu.languageHeaderLabel.setText(AppI18n.t(ctx, "common", "nav.changeLanguage",
                ctx.getString(R.string.settings_change_language)));
        menu.logoutLabel.setText(AppI18n.t(ctx, "common", "nav.logout",
                ctx.getString(R.string.settings_logout)));
    }

    private void populateLanguageOptions(BottomSheetAccountMenuBinding menu, BottomSheetDialog dialog) {
        String current = appPreferences.getLanguage();
        for (String lang : AppPreferences.supportedLanguages()) {
            TextView option = new TextView(getContext());
            option.setText(AppPreferences.languageLabel(lang));
            option.setTextSize(15);
            option.setPadding(dp(14), dp(14), dp(14), dp(14));
            boolean selected = lang.equals(current);
            option.setBackgroundResource(selected ? R.drawable.bg_stats_btn : R.drawable.bg_settings_row);
            option.setTextColor(ContextCompat.getColor(getContext(),
                    selected ? R.color.primary : R.color.text_main));
            option.setTypeface(option.getTypeface(), android.graphics.Typeface.BOLD);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.bottomMargin = dp(8);
            option.setLayoutParams(lp);
            option.setOnClickListener(v -> {
                if (!lang.equals(appPreferences.getLanguage())) {
                    dialog.dismiss();
                    appPreferences.setLanguage(lang);
                } else {
                    dialog.dismiss();
                }
            });
            menu.languageOptionsPanel.addView(option);
        }
    }

    private void saveProfile(BottomSheetAccountMenuBinding menu, BottomSheetDialog dialog) {
        String name = menu.profileNameInput.getText() != null
                ? menu.profileNameInput.getText().toString().trim() : "";
        if (name.isEmpty()) {
            Toast.makeText(getContext(),
                    getContext().getString(R.string.settings_enter_name), Toast.LENGTH_SHORT).show();
            return;
        }
        Map<String, String> body = new HashMap<>();
        body.put("fullName", name);
        authRepository.updateProfile(body).enqueue(new Callback<Map<String, User>>() {
            @Override
            public void onResponse(Call<Map<String, User>> call, Response<Map<String, User>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().get("user") != null) {
                    authManager.saveUser(response.body().get("user"));
                    refreshUser();
                    Toast.makeText(getContext(),
                            getContext().getString(R.string.settings_profile_saved), Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                } else {
                    Toast.makeText(getContext(),
                            getContext().getString(R.string.settings_save_failed), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, User>> call, Throwable t) {
                Toast.makeText(getContext(),
                        getContext().getString(R.string.settings_network_error), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private void performLogout() {
        authManager.clear();
        Intent intent = new Intent(getContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        getContext().startActivity(intent);
    }
}
