package com.example.myapplication.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.core.utils.QrPreviewLoader;
import com.example.myapplication.core.utils.SvgThumbHelper;
import com.example.myapplication.data.models.QrCode;
import com.example.myapplication.databinding.ItemQrCodeBinding;
import com.example.myapplication.databinding.ItemStyleThumbnailBinding;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class QrAdapter extends RecyclerView.Adapter<QrAdapter.ViewHolder> {

    public interface QrActionListener {
        void onDelete(QrCode qr);
        void onEdit(QrCode qr);
        void onDetails(QrCode qr);
    }

    private static final SimpleDateFormat ISO_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
    private static final SimpleDateFormat DISPLAY_FORMAT =
            new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    private List<QrCode> items = new ArrayList<>();
    private final QrActionListener listener;
    private QrPreviewLoader previewLoader;

    public QrAdapter(QrActionListener listener) {
        this.listener = listener;
    }

    public void setPreviewLoader(QrPreviewLoader previewLoader) {
        this.previewLoader = previewLoader;
    }

    public void setItems(List<QrCode> items) {
        this.items = items != null ? items : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemQrCodeBinding binding = ItemQrCodeBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        QrCode item = items.get(position);
        String name = item.getDisplayName();
        holder.binding.qrName.setText(name != null && !name.isEmpty() ? name : "ללא שם");
        holder.binding.qrType.setText(formatQrType(item.getQrType(), item.getLinkMode()));
        holder.binding.qrStatusBadge.setText(item.isActive() ? "פעיל" : "לא פעיל");
        holder.binding.scanCount.setText(item.getScanCount() + " סריקות");
        holder.binding.qrDate.setText(formatDate(item.getCreatedAt()));

        if (previewLoader != null) {
            previewLoader.loadInto(holder.itemView.getContext(), item, holder.binding.qrPreviewSmall);
        } else {
            holder.binding.qrPreviewSmall.setImageDrawable(null);
        }

        holder.binding.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEdit(item);
            }
        });
        holder.binding.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDelete(item);
            }
        });
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDetails(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private String formatQrType(String qrType, String linkMode) {
        String type = qrType != null ? qrType : "url";
        String mode = "dynamic".equals(linkMode) ? "דינמי" : "סטטי";
        return "QR " + mode + " · " + type;
    }

    private String formatDate(String isoDate) {
        if (isoDate == null || isoDate.isEmpty()) {
            return "";
        }
        try {
            Date date = ISO_FORMAT.parse(isoDate);
            if (date != null) {
                return DISPLAY_FORMAT.format(date);
            }
        } catch (ParseException ignored) {
            return isoDate.substring(0, Math.min(10, isoDate.length()));
        }
        return "";
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemQrCodeBinding binding;

        ViewHolder(ItemQrCodeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
