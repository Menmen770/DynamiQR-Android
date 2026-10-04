package com.example.myapplication.ui.components;

import android.app.Activity;
import android.app.DatePickerDialog;
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
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.myapplication.DynamiQRApplication;
import com.example.myapplication.R;
import com.example.myapplication.core.i18n.AppI18n;
import com.example.myapplication.core.utils.IsraeliIdValidator;
import com.example.myapplication.data.local.AppPreferences;
import com.example.myapplication.data.local.AuthManager;
import com.example.myapplication.data.models.User;
import com.example.myapplication.data.repository.LocalUserRepository;
import com.example.myapplication.databinding.BottomSheetAccountMenuBinding;
import com.example.myapplication.databinding.ViewAppHeaderBinding;
import com.example.myapplication.features.auth.LoginActivity;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import java.util.Calendar;
import java.util.Locale;

public class AppHeaderView extends FrameLayout {

    private ViewAppHeaderBinding binding;
    private AuthManager authManager;
    private LocalUserRepository localUserRepository;
    private AppPreferences appPreferences;
    private OnLogoClickListener logoClickListener;
    private String selectedBirthDate = "";

    public interface OnLogoClickListener {
        void onLogoClick();
    }

    public AppHeaderView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        binding = ViewAppHeaderBinding.inflate(LayoutInflater.from(context), this, true);
        DynamiQRApplication app = DynamiQRApplication.getInstance();
        authManager = app.getAuthManager();
        localUserRepository = app.getLocalUserRepository();
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
        selectedBirthDate = "";
        if (user != null) {
            menu.profileNameInput.setText(user.getFullName() != null ? user.getFullName() : "");
            menu.profileIdNumberInput.setText(user.getIdNumber() != null ? user.getIdNumber() : "");
            menu.profilePhoneInput.setText(user.getPhone() != null ? user.getPhone() : "");
            selectedBirthDate = user.getBirthDate() != null ? user.getBirthDate() : "";
            menu.profileBirthDateInput.setText(selectedBirthDate);
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

        menu.profileBirthDateInput.setOnClickListener(v -> showBirthDatePicker(menu));
        menu.saveProfileButton.setOnClickListener(v -> saveProfile(menu, dialog));
        menu.deleteAccountButton.setOnClickListener(v -> confirmDeleteAccount(dialog));

        boolean isNight = appPreferences.isDarkMode();
        menu.dayNightToggle.setNight(isNight, false);
        menu.themeModeLabel.setText(isNight
                ? getContext().getString(R.string.settings_night_mode)
                : getContext().getString(R.string.settings_day_mode));
        menu.dayNightToggle.setOnCheckedChangeListener(night -> {
            appPreferences.setDarkMode(night);
            menu.themeModeLabel.setText(night
                    ? getContext().getString(R.string.settings_night_mode)
                    : getContext().getString(R.string.settings_day_mode));
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
            dialog.dismiss();
            performLogout();
        });

        dialog.show();
    }

    private void showBirthDatePicker(BottomSheetAccountMenuBinding menu) {
        Calendar calendar = Calendar.getInstance();
        if (selectedBirthDate != null && !selectedBirthDate.isEmpty()) {
            try {
                String[] parts = selectedBirthDate.split("-");
                calendar.set(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]) - 1,
                        Integer.parseInt(parts[2]));
            } catch (Exception ignored) {
            }
        }
        DatePickerDialog picker = new DatePickerDialog(
                getContext(),
                (view, year, month, dayOfMonth) -> {
                    selectedBirthDate = String.format(Locale.US, "%04d-%02d-%02d",
                            year, month + 1, dayOfMonth);
                    menu.profileBirthDateInput.setText(selectedBirthDate);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        picker.getDatePicker().setMaxDate(System.currentTimeMillis());
        picker.show();
    }

    private void localizeAccountMenu(BottomSheetAccountMenuBinding menu) {
        Context ctx = getContext();
        menu.settingsTitle.setText(ctx.getString(R.string.settings_title));
        menu.profileHeaderLabel.setText(ctx.getString(R.string.settings_edit_profile));
        menu.saveProfileButton.setText(ctx.getString(R.string.settings_save_profile));
        menu.deleteAccountButton.setText(ctx.getString(R.string.settings_delete_account));
        menu.displayModeHeaderLabel.setText(ctx.getString(R.string.settings_display_mode));
        menu.languageHeaderLabel.setText(ctx.getString(R.string.settings_change_language));
        menu.logoutLabel.setText(ctx.getString(R.string.settings_logout));
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
        User current = authManager.getUser();
        if (current == null || current.getLocalRowId() <= 0) {
            Toast.makeText(getContext(),
                    getContext().getString(R.string.settings_save_failed), Toast.LENGTH_SHORT).show();
            return;
        }

        String name = textOf(menu.profileNameInput);
        String idNumber = textOf(menu.profileIdNumberInput);
        String phone = textOf(menu.profilePhoneInput);
        String email = textOf(menu.profileEmailInput);
        String birth = selectedBirthDate != null ? selectedBirthDate : "";
        String newPassword = textOf(menu.profilePasswordInput);

        if (name.isEmpty() || idNumber.isEmpty() || phone.isEmpty()
                || email.isEmpty() || birth.isEmpty()) {
            Toast.makeText(getContext(),
                    getContext().getString(R.string.error_fill_all), Toast.LENGTH_SHORT).show();
            return;
        }
        if (!IsraeliIdValidator.isValid(idNumber)) {
            Toast.makeText(getContext(),
                    getContext().getString(R.string.error_invalid_id), Toast.LENGTH_SHORT).show();
            return;
        }
        if (phone.length() < 9) {
            Toast.makeText(getContext(),
                    getContext().getString(R.string.error_invalid_phone), Toast.LENGTH_SHORT).show();
            return;
        }
        if (!newPassword.isEmpty() && newPassword.length() < 6) {
            Toast.makeText(getContext(),
                    getContext().getString(R.string.error_password_short), Toast.LENGTH_SHORT).show();
            return;
        }

        localUserRepository.updateProfile(
                current.getLocalRowId(),
                name,
                email,
                idNumber,
                phone,
                birth,
                newPassword.isEmpty() ? null : newPassword,
                new LocalUserRepository.Callback<User>() {
                    @Override
                    public void onSuccess(User result) {
                        authManager.saveLocalSession(result);
                        refreshUser();
                        Toast.makeText(getContext(),
                                getContext().getString(R.string.settings_profile_saved),
                                Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }

                    @Override
                    public void onError(String message) {
                        String msg = getContext().getString(R.string.settings_save_failed);
                        if ("email_exists".equals(message)) {
                            msg = getContext().getString(R.string.error_email_exists);
                        } else if ("id_exists".equals(message)) {
                            msg = getContext().getString(R.string.error_id_exists);
                        }
                        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void confirmDeleteAccount(BottomSheetDialog parentDialog) {
        User current = authManager.getUser();
        if (current == null || current.getLocalRowId() <= 0) {
            return;
        }
        new AlertDialog.Builder(getContext())
                .setTitle(R.string.settings_delete_account)
                .setMessage(R.string.settings_delete_account_confirm)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.settings_delete_account, (d, w) ->
                        localUserRepository.deleteAccount(current.getLocalRowId(),
                                new LocalUserRepository.Callback<Boolean>() {
                                    @Override
                                    public void onSuccess(Boolean result) {
                                        parentDialog.dismiss();
                                        Toast.makeText(getContext(),
                                                getContext().getString(R.string.settings_account_deleted),
                                                Toast.LENGTH_SHORT).show();
                                        performLogout();
                                    }

                                    @Override
                                    public void onError(String message) {
                                        Toast.makeText(getContext(),
                                                getContext().getString(R.string.settings_save_failed),
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }))
                .show();
    }

    private static String textOf(android.widget.EditText editText) {
        return editText.getText() != null ? editText.getText().toString().trim() : "";
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
