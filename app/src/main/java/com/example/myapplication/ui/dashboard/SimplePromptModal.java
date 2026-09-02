package com.example.myapplication.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.myapplication.databinding.LayoutSimplePromptBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class SimplePromptModal extends BottomSheetDialogFragment {
    private LayoutSimplePromptBinding binding;
    private String title;
    private String description;
    private OnConfirmListener listener;

    public interface OnConfirmListener {
        void onConfirm(String input);
    }

    public static SimplePromptModal newInstance(String title, String description, OnConfirmListener listener) {
        SimplePromptModal modal = new SimplePromptModal();
        modal.title = title;
        modal.description = description;
        modal.listener = listener;
        return modal;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = LayoutSimplePromptBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        binding.promptTitle.setText(title);
        binding.promptDescription.setText(description);
        
        binding.cancelBtn.setOnClickListener(v -> dismiss());
        binding.confirmBtn.setOnClickListener(v -> {
            if (listener != null) {
                listener.onConfirm(binding.promptInput.getText().toString());
            }
            dismiss();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
