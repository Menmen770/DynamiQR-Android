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
import com.example.myapplication.core.i18n.AppI18n;
import com.example.myapplication.data.models.QrCode;
import com.example.myapplication.databinding.DialogQuickEditBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

/** Rename + optional dynamic target update for a saved QR. */
public class QuickEditBottomSheetFragment extends BottomSheetDialogFragment {

    private DialogQuickEditBinding binding;
    private DashboardViewModel viewModel;
    private String qrId;
    private boolean isDynamic;

    public static QuickEditBottomSheetFragment newInstance(QrCode qr) {
        QuickEditBottomSheetFragment fragment = new QuickEditBottomSheetFragment();
        Bundle args = new Bundle();
        args.putString("id", qr.getId());
        args.putString("name", qr.getDisplayName());
        args.putString("mode", qr.getLinkMode());
        args.putString("target", qr.getQrValue());
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DialogQuickEditBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewModelFactory factory = new ViewModelFactory(DynamiQRApplication.getInstance().getQrRepository());
        viewModel = new ViewModelProvider(requireActivity(), factory).get(DashboardViewModel.class);

        localizeChrome();

        Bundle args = getArguments();
        if (args != null) {
            qrId = args.getString("id");
            isDynamic = "dynamic".equals(args.getString("mode"));
            binding.editNameInput.setText(args.getString("name"));
            String target = args.getString("target");
            if (isDynamic && target != null && !target.isEmpty()) {
                binding.editTargetInput.setText(target);
            }
        }

        binding.editTargetLayout.setVisibility(isDynamic ? View.VISIBLE : View.GONE);
        binding.editSubtitle.setVisibility(isDynamic ? View.VISIBLE : View.GONE);

        binding.btnCancelEdit.setOnClickListener(v -> dismiss());
        binding.btnSaveEdit.setOnClickListener(v -> saveChanges());
    }

    private void localizeChrome() {
        binding.editTitle.setText(AppI18n.t(requireContext(), "dashboard", "card.quickEditTitle",
                "Quick edit"));
        binding.editSubtitle.setText(AppI18n.t(requireContext(), "dashboard", "card.editTargetHint",
                "Change destination for a dynamic code"));
        binding.editNameLayout.setHint(AppI18n.t(requireContext(), "dashboard", "card.renamePlaceholder",
                "Code name"));
        binding.editTargetLayout.setHint(AppI18n.t(requireContext(), "dashboard", "card.targetUrl",
                "Destination URL"));
        binding.btnCancelEdit.setText(AppI18n.t(requireContext(), "dashboard", "actions.cancel", "Cancel"));
        binding.btnSaveEdit.setText(AppI18n.t(requireContext(), "dashboard", "actions.save", "Save"));
    }

    private void saveChanges() {
        String newName = binding.editNameInput.getText() != null
                ? binding.editNameInput.getText().toString().trim() : "";
        String newTarget = binding.editTargetInput.getText() != null
                ? binding.editTargetInput.getText().toString().trim() : "";

        if (newName.isEmpty()) {
            binding.editNameLayout.setError(AppI18n.t(requireContext(), "dashboard", "card.renameRequired",
                    "Name is required"));
            return;
        }
        binding.editNameLayout.setError(null);

        binding.btnSaveEdit.setEnabled(false);
        String targetPayload = isDynamic && !newTarget.isEmpty() ? newTarget : null;
        viewModel.updateQr(qrId, newName, targetPayload, () -> {
            if (!isAdded()) {
                return;
            }
            Toast.makeText(requireContext(),
                    AppI18n.t(requireContext(), "dashboard", "card.updated", "Updated successfully"),
                    Toast.LENGTH_SHORT).show();
            dismiss();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
