package com.example.myapplication.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.core.i18n.AppI18n;
import com.example.myapplication.core.utils.QrPreviewLoader;
import com.example.myapplication.core.utils.QrTypeProvider;
import com.example.myapplication.data.models.QrCode;
import com.example.myapplication.data.models.QrType;
import com.example.myapplication.databinding.ItemQrCodeBinding;
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

        void onRename(QrCode qr);

        void onToggleActive(QrCode qr, boolean active);

        void onStats(QrCode qr);

        void onChangeFolder(QrCode qr);

        void onShare(QrCode qr);

        String folderNameFor(QrCode qr);
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
        Context context = holder.itemView.getContext();
        QrCode item = items.get(position);
        boolean isDynamic = "dynamic".equals(item.getLinkMode());
        String name = item.getDisplayName();
        holder.binding.qrName.setText(name != null && !name.isEmpty()
                ? name
                : AppI18n.t(context, "dashboard", "card.qrCodeFallback", "Untitled"));
        holder.binding.qrTypeLabel.setText(typeLabel(context, item.getQrType()));
        holder.binding.qrDate.setText(formatDate(item.getCreatedAt()));
        holder.binding.destinationText.setText(destinationSummary(context, item, isDynamic));

        if (isDynamic) {
            holder.binding.modeBadge.setText(AppI18n.t(context, "dashboard", "card.dynamic", "Dynamic"));
            holder.binding.modeBadge.setBackgroundResource(R.drawable.bg_badge_dynamic);
            holder.binding.modeBadge.setTextColor(
                    ContextCompat.getColor(context, R.color.primary));
            holder.binding.scanChip.setVisibility(View.VISIBLE);
            holder.binding.scanChip.setText(
                    AppI18n.t(context, "dashboard", "card.scansShort", "Scans")
                            + " " + item.getScanCount());
            holder.binding.btnStats.setVisibility(View.VISIBLE);
            holder.binding.staticHint.setVisibility(View.GONE);
        } else {
            holder.binding.modeBadge.setText(AppI18n.t(context, "dashboard", "card.static", "Static"));
            holder.binding.modeBadge.setBackgroundResource(R.drawable.bg_badge_static);
            holder.binding.modeBadge.setTextColor(
                    ContextCompat.getColor(context, R.color.sub_text));
            holder.binding.scanChip.setVisibility(View.GONE);
            holder.binding.btnStats.setVisibility(View.GONE);
            holder.binding.staticHint.setVisibility(View.VISIBLE);
        }

        holder.binding.activeSwitch.setOnCheckedChangeListener(null);
        holder.binding.activeSwitch.setChecked(item.isActive());
        holder.binding.activeLabel.setText(activeLabel(context, item.isActive()));
        holder.binding.activeLabel.setTextColor(ContextCompat.getColor(
                context,
                item.isActive() ? R.color.primary : R.color.sub_text));

        String folderName = listener != null ? listener.folderNameFor(item) : null;
        holder.binding.folderName.setText(
                folderName != null && !folderName.isEmpty()
                        ? folderName
                        : AppI18n.t(context, "dashboard", "sidebar.unfiled", "Unfiled"));

        if (previewLoader != null) {
            previewLoader.loadInto(context, item, holder.binding.qrPreviewSmall);
        } else {
            holder.binding.qrPreviewSmall.setImageDrawable(null);
        }

        holder.binding.activeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener != null) {
                holder.binding.activeLabel.setText(activeLabel(context, isChecked));
                listener.onToggleActive(item, isChecked);
            }
        });
        holder.binding.btnRename.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRename(item);
            }
        });
        holder.binding.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDelete(item);
            }
        });
        holder.binding.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEdit(item);
            }
        });
        holder.binding.btnShare.setOnClickListener(v -> {
            if (listener != null) {
                listener.onShare(item);
            }
        });
        holder.binding.folderChip.setOnClickListener(v -> {
            if (listener != null) {
                listener.onChangeFolder(item);
            }
        });
        holder.binding.btnShareLabel.setText(
                AppI18n.t(context, "dashboard", "actions.share",
                        context.getString(R.string.action_share)));
        holder.binding.btnDeleteLabel.setText(
                AppI18n.t(context, "dashboard", "actions.delete", "Delete"));
        holder.binding.btnEditLabel.setText(
                AppI18n.t(context, "dashboard", "actions.edit", "Edit"));
        holder.binding.btnStatsLabel.setText(
                AppI18n.t(context, "dashboard", "card.stats", "Statistics"));
        holder.binding.btnStats.setOnClickListener(v -> {
            if (listener != null) {
                listener.onStats(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private String activeLabel(Context context, boolean active) {
        if (active) {
            return AppI18n.t(context, "dashboard", "card.active", "Active");
        }
        return AppI18n.t(context, "dashboard", "card.inactive", "Inactive");
    }

    private String typeLabel(Context context, String qrType) {
        if (qrType == null) {
            return "QR";
        }
        QrType type = QrTypeProvider.findById(context, qrType);
        return type != null ? type.getLabel() : qrType;
    }

    private String destinationSummary(Context context, QrCode item, boolean isDynamic) {
        if (isDynamic) {
            String slug = item.getPublicSlug();
            if (slug != null && !slug.isEmpty()) {
                return AppI18n.t(context, "dashboard", "card.shortLink", "Short link")
                        + " · /" + slug;
            }
            return AppI18n.t(context, "dashboard", "card.dynamicTarget",
                    "Dynamic destination — updatable");
        }
        String value = item.getQrValue();
        if (value == null || value.isEmpty()) {
            return AppI18n.t(context, "dashboard", "card.staticEmbedded",
                    "Embedded content");
        }
        return value.length() > 64 ? value.substring(0, 64) + "…" : value;
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
