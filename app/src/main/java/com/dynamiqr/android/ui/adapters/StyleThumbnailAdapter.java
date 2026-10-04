package com.dynamiqr.android.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import com.dynamiqr.android.R;
import com.dynamiqr.android.core.utils.PresetLogos;
import com.dynamiqr.android.core.utils.SvgThumbHelper;
import com.dynamiqr.android.databinding.ItemStyleThumbnailBinding;
import java.util.List;

public class StyleThumbnailAdapter extends RecyclerView.Adapter<StyleThumbnailAdapter.ViewHolder> {

    public interface StyleItem {
        String getId();
        int getImageResId();
        boolean isSvg();
    }

    private final List<? extends StyleItem> items;
    private String selectedId;
    private final OnItemSelectedListener listener;
    private final boolean normalizeLogoSize;
    /** לחיצה חוזרת על אותו פריט מבטלת בחירה (לוגו/סטיקר). */
    private final boolean allowToggleOff;

    public interface OnItemSelectedListener {
        /** item == null כשבוטלה בחירה (toggle off). */
        void onItemSelected(@Nullable StyleItem item);
    }

    public StyleThumbnailAdapter(List<? extends StyleItem> items, String selectedId,
                                 OnItemSelectedListener listener) {
        this(items, selectedId, listener, false, false);
    }

    public StyleThumbnailAdapter(List<? extends StyleItem> items, String selectedId,
                                 OnItemSelectedListener listener, boolean normalizeLogoSize) {
        this(items, selectedId, listener, normalizeLogoSize, false);
    }

    public StyleThumbnailAdapter(List<? extends StyleItem> items, String selectedId,
                                 OnItemSelectedListener listener, boolean normalizeLogoSize,
                                 boolean allowToggleOff) {
        this.items = items;
        this.selectedId = selectedId;
        this.listener = listener;
        this.normalizeLogoSize = normalizeLogoSize;
        this.allowToggleOff = allowToggleOff;
    }

    public void setSelectedId(String id) {
        this.selectedId = id;
        notifyDataSetChanged();
    }

    public String getSelectedId() {
        return selectedId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemStyleThumbnailBinding binding = ItemStyleThumbnailBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StyleItem item = items.get(position);
        float density = holder.itemView.getResources().getDisplayMetrics().density;
        int sizePx = Math.round(72 * density);

        holder.binding.thumbImage.setScaleType(android.widget.ImageView.ScaleType.FIT_CENTER);
        if (item.getImageResId() != 0) {
            if (item.isSvg()) {
                float visualScale = 1f;
                if (normalizeLogoSize) {
                    float inset = PresetLogos.insetForId(item.getId());
                    visualScale = inset < 0.9f ? (0.9f / Math.max(0.35f, inset)) : 1f;
                    visualScale = Math.min(1.2f, visualScale);
                }
                android.graphics.Bitmap bitmap = SvgThumbHelper.renderSvgResource(
                        holder.itemView.getContext(), item.getImageResId(), sizePx, visualScale);
                if (bitmap != null) {
                    holder.binding.thumbImage.setImageBitmap(bitmap);
                } else {
                    holder.binding.thumbImage.setImageResource(item.getImageResId());
                }
            } else {
                holder.binding.thumbImage.setImageResource(item.getImageResId());
            }
        } else {
            holder.binding.thumbImage.setImageDrawable(null);
        }

        boolean isSelected = selectedId != null && item.getId().equals(selectedId);
        int primary = holder.itemView.getContext().getColor(R.color.primary);
        int border = holder.itemView.getContext().getColor(R.color.border);
        if (isSelected) {
            holder.binding.thumbCard.setStrokeColor(primary);
            holder.binding.thumbCard.setStrokeWidth(3);
            holder.binding.selectedOverlay.setVisibility(View.VISIBLE);
            holder.binding.thumbCard.setCardElevation(4f);
            holder.binding.thumbCard.setCardBackgroundColor(
                    holder.itemView.getContext().getColor(R.color.primary_light));
        } else {
            holder.binding.thumbCard.setStrokeColor(border);
            holder.binding.thumbCard.setStrokeWidth(1);
            holder.binding.selectedOverlay.setVisibility(View.GONE);
            holder.binding.thumbCard.setCardElevation(0f);
            holder.binding.thumbCard.setCardBackgroundColor(
                    holder.itemView.getContext().getColor(R.color.surface));
        }

        holder.itemView.setOnClickListener(v -> {
            if (allowToggleOff && isSelected) {
                selectedId = "";
                notifyDataSetChanged();
                listener.onItemSelected(null);
                return;
            }
            selectedId = item.getId();
            notifyDataSetChanged();
            listener.onItemSelected(item);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final ItemStyleThumbnailBinding binding;

        public ViewHolder(ItemStyleThumbnailBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
