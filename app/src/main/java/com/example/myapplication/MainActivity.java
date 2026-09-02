package com.example.myapplication;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import com.example.myapplication.core.base.BaseActivity;
import com.example.myapplication.databinding.ActivityMainBinding;
import com.example.myapplication.features.dashboard.DashboardFragment;
import com.example.myapplication.features.generator.GeneratorFragment;
import com.example.myapplication.features.scanner.ScannerFragment;
import com.example.myapplication.features.learn.LearnQrFragment;

public class MainActivity extends BaseActivity<ActivityMainBinding> {

    @Override
    protected ActivityMainBinding inflateBinding() {
        return ActivityMainBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loadFragment(new DashboardFragment());

        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_dashboard) {
                loadFragment(new DashboardFragment());
                return true;
            } else if (id == R.id.nav_scanner) {
                loadFragment(new ScannerFragment());
                return true;
            } else if (id == R.id.nav_generator) {
                loadFragment(new GeneratorFragment());
                return true;
            } else if (id == R.id.nav_learn) {
                loadFragment(new LearnQrFragment());
                return true;
            }
            return false;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }
}
