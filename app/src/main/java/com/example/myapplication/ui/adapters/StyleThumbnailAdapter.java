package com.example.myapplication.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.core.utils.SvgThumbHelper;
import com.example.myapplication.databinding.ItemStyleThumbnailBinding;
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

    public interface OnItemSelectedListener {
        void onItemSelected(StyleItem item);
    }

    public StyleThumbnailAdapter(List<? extends StyleItem> items, String selectedId, OnItemSelectedListener listener) {
        this.items = items;
        this.selectedId = selectedId;
        this.listener = listener;
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

        if (item.getImageResId() != 0) {
            if (item.isSvg()) {
                int sizePx = (int) (52 * holder.itemView.getResources().getDisplayMetrics().density);
                android.graphics.Bitmap bitmap = SvgThumbHelper.renderSvgResource(
                        holder.itemView.getContext(), item.getImageResId(), sizePx);
                holder.binding.thumbImage.setImageBitmap(bitmap);
            } else {
                holder.binding.thumbImage.setImageResource(item.getImageResId());
            }
        } else {
            holder.binding.thumbImage.setImageDrawable(null);
        }

        boolean isSelected = item.getId().equals(selectedId);
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
