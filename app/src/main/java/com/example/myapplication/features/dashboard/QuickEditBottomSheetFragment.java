package com.example.myapplication.features.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import com.example.myapplication.DynamiQRApplication;
import com.example.myapplication.core.factory.ViewModelFactory;
import com.example.myapplication.data.models.QrCode;
import com.example.myapplication.databinding.DialogQuickEditBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class QuickEditBottomSheetFragment extends BottomSheetDialogFragment {

    private DialogQuickEditBinding binding;
    private DashboardViewModel viewModel;
    private String qrId;

    public static QuickEditBottomSheetFragment newInstance(QrCode qr) {
        QuickEditBottomSheetFragment fragment = new QuickEditBottomSheetFragment();
        Bundle args = new Bundle();
        args.putString("id", qr.getId());
        args.putString("name", qr.getDisplayName());
        // For dynamic QR, the content might be stored differently or we assume it's URL.
        // We'll just pass a placeholder or the actual content if we had it.
        // Since we don't have the full content in the list model, we might need to fetch it.
        // But for now, let's just allow editing the name.
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogQuickEditBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        ViewModelFactory factory = new ViewModelFactory(DynamiQRApplication.getInstance().getQrRepository());
        viewModel = new ViewModelProvider(requireActivity(), factory).get(DashboardViewModel.class);

        Bundle args = getArguments();
        if (args != null) {
            qrId = args.getString("id");
            binding.editNameInput.setText(args.getString("name"));
        }

        binding.btnCancelEdit.setOnClickListener(v -> dismiss());
        binding.btnSaveEdit.setOnClickListener(v -> saveChanges());
    }

    private void saveChanges() {
        String newName = binding.editNameInput.getText().toString().trim();
        String newTarget = binding.editTargetInput.getText().toString().trim();

        if (newName.isEmpty()) {
            binding.editNameLayout.setError("שם חובה");
            return;
        }

        binding.btnSaveEdit.setEnabled(false);
        viewModel.updateQr(qrId, newName, newTarget.isEmpty() ? null : newTarget, () -> {
            Toast.makeText(getContext(), "עודכן בהצלחה", Toast.LENGTH_SHORT).show();
            dismiss();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
