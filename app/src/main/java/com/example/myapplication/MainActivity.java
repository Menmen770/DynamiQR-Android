package com.example.myapplication;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;
import com.example.myapplication.core.base.BaseActivity;
import com.example.myapplication.databinding.ActivityMainBinding;
import com.example.myapplication.databinding.ItemBottomNavTabBinding;
import com.example.myapplication.features.auth.LoginActivity;

public class MainActivity extends BaseActivity<ActivityMainBinding> {

    public static final String EXTRA_TAB = "extra_tab";

    private static final int TAB_CODES = 0;
    private static final int TAB_CREATE = 1;
    private static final int TAB_SCAN = 2;
    private static final int TAB_LEARN = 3;

    private NavController navController;
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

        NavHostFragment navHost = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHost == null) {
            finish();
            return;
        }
        navController = navHost.getNavController();

        if (savedInstanceState != null) {
            selectedTab = savedInstanceState.getInt("selected_tab", TAB_CODES);
        }

        setupBottomNav();
        binding.appHeader.setOnLogoClickListener(() -> selectTab(TAB_CODES));
        binding.appHeader.refreshUser();

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            int destId = destination.getId();
            if (destId == R.id.nav_dashboard) {
                selectedTab = TAB_CODES;
            } else if (destId == R.id.nav_generator) {
                selectedTab = TAB_CREATE;
            } else if (destId == R.id.nav_scanner) {
                selectedTab = TAB_SCAN;
            } else if (destId == R.id.nav_learn) {
                selectedTab = TAB_LEARN;
            }
            highlightTab(selectedTab);
            updateShellForTab(selectedTab);
        });

        handleTabIntent(getIntent());
        if (savedInstanceState == null && getIntent().getIntExtra(EXTRA_TAB, -1) < 0) {
            highlightTab(selectedTab);
            updateShellForTab(selectedTab);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        binding.appHeader.refreshUser();
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
        int destId = destForTab(tabIndex);
        if (navController.getCurrentDestination() != null
                && navController.getCurrentDestination().getId() == destId) {
            highlightTab(tabIndex);
            updateShellForTab(tabIndex);
            return;
        }
        NavOptions options = new NavOptions.Builder()
                .setLaunchSingleTop(true)
                .setRestoreState(true)
                .setPopUpTo(navController.getGraph().getStartDestinationId(), false, true)
                .build();
        try {
            navController.navigate(destId, null, options);
        } catch (IllegalArgumentException ignored) {
        }
        selectedTab = tabIndex;
        highlightTab(tabIndex);
        updateShellForTab(tabIndex);
    }

    private int destForTab(int tabIndex) {
        switch (tabIndex) {
            case TAB_CREATE:
                return R.id.nav_generator;
            case TAB_SCAN:
                return R.id.nav_scanner;
            case TAB_LEARN:
                return R.id.nav_learn;
            case TAB_CODES:
            default:
                return R.id.nav_dashboard;
        }
    }

    private void updateShellForTab(int tabIndex) {
        boolean isScanner = tabIndex == TAB_SCAN;
        binding.appHeader.setVisibility(isScanner ? View.GONE : View.VISIBLE);
        // Rotation only on Learn (guide) tab; other main tabs stay portrait.
        if (tabIndex == TAB_LEARN) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
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
}
