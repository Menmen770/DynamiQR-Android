package com.example.myapplication.features.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.myapplication.data.models.QrCode;
import com.example.myapplication.databinding.DialogQrStatsBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class StatsBottomSheetFragment extends BottomSheetDialogFragment {

    private static final String ARG_QR = "arg_qr";
    private static final SimpleDateFormat ISO_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
    private static final SimpleDateFormat DISPLAY_FORMAT =
            new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    private DialogQrStatsBinding binding;
    private QrCode qrCode;

    public static StatsBottomSheetFragment newInstance(QrCode qr) {
        StatsBottomSheetFragment fragment = new StatsBottomSheetFragment();
        Bundle args = new Bundle();
        // Since QrCode is not Parcelable, we'll pass fields or use a better way.
        // For simplicity, let's just use static or pass ID and fetch (but we have it here).
        // I'll make QrCode Serializable for this quick implementation.
        // Wait, I can't easily change QrCode to Serializable without checking imports.
        // Let's just pass the data manually for now.
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
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogQrStatsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            binding.statTitle.setText(args.getString("name", "ללא שם"));
            String mode = "dynamic".equals(args.getString("mode")) ? "דינמי" : "סטטי";
            binding.statSubtitle.setText(args.getString("type", "URL") + " · " + mode);
            binding.statScanCount.setText(String.valueOf(args.getInt("scans", 0)));
            binding.statStatus.setText(args.getBoolean("active") ? "פעיל" : "לא פעיל");
            binding.statMode.setText(mode);
            binding.statType.setText(args.getString("type", "URL"));
            binding.statDate.setText(formatDate(args.getString("date")));
        }

        binding.btnCloseStats.setOnClickListener(v -> dismiss());
    }

    private String formatDate(String isoDate) {
        if (isoDate == null || isoDate.isEmpty()) return "-";
        try {
            Date date = ISO_FORMAT.parse(isoDate);
            return DISPLAY_FORMAT.format(date);
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
