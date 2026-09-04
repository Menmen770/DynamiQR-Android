package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.example.myapplication.core.base.BaseActivity;
import com.example.myapplication.databinding.ActivityMainBinding;
import com.example.myapplication.databinding.ItemBottomNavTabBinding;
import com.example.myapplication.features.auth.LoginActivity;
import com.example.myapplication.features.dashboard.DashboardFragment;
import com.example.myapplication.features.generator.GeneratorFragment;
import com.example.myapplication.features.learn.LearnQrFragment;
import com.example.myapplication.features.scanner.ScannerFragment;

public class MainActivity extends BaseActivity<ActivityMainBinding> {

    public static final String EXTRA_TAB = "extra_tab";

    private static final int TAB_CODES = 0;
    private static final int TAB_CREATE = 1;
    private static final int TAB_SCAN = 2;
    private static final int TAB_LEARN = 3;

    private Fragment activeFragment;
    private final Fragment dashboardFragment = new DashboardFragment();
    private final Fragment scannerFragment = new ScannerFragment();
    private final Fragment generatorFragment = new GeneratorFragment();
    private final Fragment learnFragment = new LearnQrFragment();
    private int selectedTab = TAB_CODES;

    @Override
    protected ActivityMainBinding inflateBinding() {
        return ActivityMainBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!DynamiQRApplication.getInstance().getAuthManager().isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setupFragments(savedInstanceState);
        setupBottomNav();
        binding.appHeader.setOnLogoClickListener(() -> selectTab(TAB_CODES));
        refreshUserProfile();

        handleTabIntent(getIntent());
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshUserProfile();
    }

    private void refreshUserProfile() {
        DynamiQRApplication app = DynamiQRApplication.getInstance();
        if (app.getAuthManager().getUser() == null) {
            app.getAuthRepository().getMe().enqueue(new retrofit2.Callback<com.example.myapplication.data.models.MeResponse>() {
                @Override
                public void onResponse(retrofit2.Call<com.example.myapplication.data.models.MeResponse> call,
                                       retrofit2.Response<com.example.myapplication.data.models.MeResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().getUser() != null) {
                        app.getAuthManager().saveUser(response.body().getUser());
                        binding.appHeader.refreshUser();
                    }
                }

                @Override
                public void onFailure(retrofit2.Call<com.example.myapplication.data.models.MeResponse> call, Throwable t) {
                }
            });
        } else {
            binding.appHeader.refreshUser();
        }
    }

    private void setupFragments(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragmentContainer, dashboardFragment, "dashboard")
                    .add(R.id.fragmentContainer, scannerFragment, "scanner")
                    .add(R.id.fragmentContainer, generatorFragment, "generator")
                    .add(R.id.fragmentContainer, learnFragment, "learn")
                    .hide(scannerFragment)
                    .hide(generatorFragment)
                    .hide(learnFragment)
                    .commit();
            activeFragment = dashboardFragment;
        } else {
            selectedTab = savedInstanceState.getInt("selected_tab", TAB_CODES);
            activeFragment = fragmentForTab(selectedTab);
        }
        updateShellForTab(selectedTab);
    }

    private void setupBottomNav() {
        configureTab(binding.tabCodes.getRoot(), getString(R.string.tab_codes), TAB_CODES);
        configureTab(binding.tabCreate.getRoot(), getString(R.string.tab_create), TAB_CREATE);
        configureTab(binding.tabScan.getRoot(), getString(R.string.tab_scan), TAB_SCAN);
        configureTab(binding.tabLearn.getRoot(), getString(R.string.tab_guide), TAB_LEARN);
        highlightTab(selectedTab);
    }

    private void configureTab(View tabRoot, String label, int tabIndex) {
        ItemBottomNavTabBinding tab = ItemBottomNavTabBinding.bind(tabRoot);
        tab.tabIcon.setImageResource(iconForTab(tabIndex, false));
        tab.tabLabel.setText(label);
        tabRoot.setOnClickListener(v -> selectTab(tabIndex));
    }

    private int iconForTab(int tabIndex, boolean selected) {
        switch (tabIndex) {
            case TAB_CREATE:
                return R.drawable.ic_tab_create;
            case TAB_SCAN:
                return selected ? R.drawable.ic_tab_scan_filled : R.drawable.ic_tab_scan;
            case TAB_LEARN:
                return selected ? R.drawable.ic_tab_learn_filled : R.drawable.ic_tab_learn;
            case TAB_CODES:
            default:
                return selected ? R.drawable.ic_tab_codes_filled : R.drawable.ic_tab_codes;
        }
    }

    private void selectTab(int tabIndex) {
        if (tabIndex == selectedTab && activeFragment != null) {
            return;
        }
        Fragment target = fragmentForTab(tabIndex);
        if (target == null || target == activeFragment) {
            return;
        }
        getSupportFragmentManager().beginTransaction()
                .hide(activeFragment)
                .show(target)
                .commit();
        activeFragment = target;
        selectedTab = tabIndex;
        highlightTab(tabIndex);
        updateShellForTab(tabIndex);
    }

    private void updateShellForTab(int tabIndex) {
        boolean isScanner = tabIndex == TAB_SCAN;
        binding.appHeader.setVisibility(isScanner ? View.GONE : View.VISIBLE);
    }

    private void highlightTab(int tabIndex) {
        highlightSingleTab(binding.tabCodes.getRoot(), TAB_CODES, tabIndex == TAB_CODES);
        highlightSingleTab(binding.tabCreate.getRoot(), TAB_CREATE, tabIndex == TAB_CREATE);
        highlightSingleTab(binding.tabScan.getRoot(), TAB_SCAN, tabIndex == TAB_SCAN);
        highlightSingleTab(binding.tabLearn.getRoot(), TAB_LEARN, tabIndex == TAB_LEARN);
    }

    private void highlightSingleTab(View tabRoot, int tabIndex, boolean selected) {
        ItemBottomNavTabBinding tab = ItemBottomNavTabBinding.bind(tabRoot);
        tab.tabRoot.setBackground(selected
                ? ContextCompat.getDrawable(this, R.drawable.bg_tab_item_selected)
                : null);
        tab.iconWrap.setBackground(selected
                ? ContextCompat.getDrawable(this, R.drawable.bg_tab_icon_selected)
                : null);
        tab.activeDot.setVisibility(selected ? View.VISIBLE : View.GONE);
        int color = ContextCompat.getColor(this, selected ? R.color.primary : R.color.sub_text);
        tab.tabLabel.setTextColor(color);
        tab.tabIcon.setImageResource(iconForTab(tabIndex, selected));
        tab.tabIcon.setColorFilter(color);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("selected_tab", selectedTab);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleTabIntent(intent);
    }

    private void handleTabIntent(Intent intent) {
        if (intent == null) {
            return;
        }
        int tab = intent.getIntExtra(EXTRA_TAB, -1);
        if (tab >= 0) {
            selectTab(tab);
        }
    }

    public void navigateToTab(int menuItemId) {
        if (menuItemId == R.id.nav_dashboard) {
            selectTab(TAB_CODES);
        } else if (menuItemId == R.id.nav_generator) {
            selectTab(TAB_CREATE);
        } else if (menuItemId == R.id.nav_scanner) {
            selectTab(TAB_SCAN);
        } else if (menuItemId == R.id.nav_learn) {
            selectTab(TAB_LEARN);
        }
    }

    private Fragment fragmentForTab(int tabIndex) {
        switch (tabIndex) {
            case TAB_CREATE:
                return generatorFragment;
            case TAB_SCAN:
                return scannerFragment;
            case TAB_LEARN:
                return learnFragment;
            case TAB_CODES:
            default:
                return dashboardFragment;
        }
    }
}
