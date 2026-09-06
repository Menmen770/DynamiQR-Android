package com.example.myapplication.features.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.myapplication.core.i18n.AppI18n;
import com.example.myapplication.data.models.QrCode;
import com.example.myapplication.databinding.DialogQrStatsBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class StatsBottomSheetFragment extends BottomSheetDialogFragment {

    private static final SimpleDateFormat ISO_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
    private static final SimpleDateFormat DISPLAY_FORMAT =
            new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    private DialogQrStatsBinding binding;

    public static StatsBottomSheetFragment newInstance(QrCode qr) {
        StatsBottomSheetFragment fragment = new StatsBottomSheetFragment();
        Bundle args = new Bundle();
        args.putString("id", qr.getId());
        args.putString("name", qr.getDisplayName());
        args.putString("type", qr.getQrType());
        args.putString("mode", qr.getLinkMode());
        args.putInt("scans", qr.getScanCount());
        args.putString("date", qr.getCreatedAt());
        args.putBoolean("active", qr.isActive());
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DialogQrStatsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            String name = args.getString("name",
                    AppI18n.t(requireContext(), "dashboard", "card.qrCodeFallback", "Untitled"));
            binding.statTitle.setText(
                    AppI18n.t(requireContext(), "dashboard", "card.stats", "Statistics") + " · " + name);
            boolean isDynamic = "dynamic".equals(args.getString("mode"));
            String mode = isDynamic
                    ? AppI18n.t(requireContext(), "dashboard", "card.dynamic", "Dynamic")
                    : AppI18n.t(requireContext(), "dashboard", "card.static", "Static");
            binding.statSubtitle.setText(args.getString("type", "URL") + " · " + mode);
            binding.statScanCount.setText(String.valueOf(args.getInt("scans", 0)));
            binding.statStatus.setText(args.getBoolean("active")
                    ? AppI18n.t(requireContext(), "dashboard", "card.active", "Active")
                    : AppI18n.t(requireContext(), "dashboard", "card.inactive", "Inactive"));
            binding.statMode.setText(mode);
            binding.statType.setText(args.getString("type", "URL"));
            binding.statDate.setText(formatDate(args.getString("date")));
        }

        binding.btnCloseStats.setText(AppI18n.t(requireContext(), "dashboard", "actions.close", "Close"));
        binding.btnCloseStats.setOnClickListener(v -> dismiss());
    }

    private String formatDate(String isoDate) {
        if (isoDate == null || isoDate.isEmpty()) {
            return "-";
        }
        try {
            Date date = ISO_FORMAT.parse(isoDate);
            return date != null ? DISPLAY_FORMAT.format(date) : isoDate;
        } catch (ParseException e) {
            return isoDate;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
