package com.dynamiqr.android.features.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import com.dynamiqr.android.R;
import com.dynamiqr.android.DynamiQRApplication;
import com.dynamiqr.android.core.factory.ViewModelFactory;
import com.dynamiqr.android.data.models.QrCode;
import com.dynamiqr.android.databinding.DialogQuickEditBinding;
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
        binding.editTitle.setText(getString(R.string.dashboard_card_quick_edit_title));
        binding.editSubtitle.setText(getString(R.string.dashboard_card_edit_target_hint));
        binding.editNameLayout.setHint(getString(R.string.dashboard_card_rename_placeholder));
        binding.editTargetLayout.setHint(getString(R.string.dashboard_card_target_url));
        binding.btnCancelEdit.setText(getString(R.string.action_cancel));
        binding.btnSaveEdit.setText(getString(R.string.dashboard_actions_save));
    }

    private void saveChanges() {
        String newName = binding.editNameInput.getText() != null
                ? binding.editNameInput.getText().toString().trim() : "";
        String newTarget = binding.editTargetInput.getText() != null
                ? binding.editTargetInput.getText().toString().trim() : "";

        if (newName.isEmpty()) {
            binding.editNameLayout.setError(getString(R.string.dashboard_card_rename_required));
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
                    getString(R.string.dashboard_card_updated),
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
