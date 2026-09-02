package com.example.myapplication.features.learn;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.myapplication.core.base.BaseFragment;
import com.example.myapplication.databinding.FragmentLearnQrBinding;

public class LearnQrFragment extends BaseFragment<FragmentLearnQrBinding> {

    @Override
    protected FragmentLearnQrBinding inflateBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentLearnQrBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        binding.header.setTitle("מה זה קוד QR?");
        binding.header.setSubtitle("מדריך קצר לעסקים ולמותגים — איך משתמשים נכון");

        binding.btnCreateHero.setOnClickListener(v -> {
            // Navigate to Generator
        });
    }
}
