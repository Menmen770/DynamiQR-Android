package com.dynamiqr.android.ui.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import androidx.annotation.Nullable;
import com.dynamiqr.android.databinding.ViewScreenPageHeaderBinding;

public class ScreenPageHeader extends LinearLayout {
    private ViewScreenPageHeaderBinding binding;

    public ScreenPageHeader(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        binding = ViewScreenPageHeaderBinding.inflate(LayoutInflater.from(context), this, true);
    }

    public void setTitle(String title) {
        binding.headerTitle.setText(title);
    }

    public void setSubtitle(String subtitle) {
        if (subtitle != null && !subtitle.isEmpty()) {
            binding.headerSubtitle.setText(subtitle);
            binding.headerSubtitle.setVisibility(View.VISIBLE);
        } else {
            binding.headerSubtitle.setVisibility(View.GONE);
        }
    }
}
