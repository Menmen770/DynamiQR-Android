package com.example.myapplication.ui.adapters;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.caverock.androidsvg.SVG;
import com.example.myapplication.R;
import com.example.myapplication.databinding.ItemStyleThumbnailBinding;
import java.util.List;

public class StyleThumbnailAdapter extends RecyclerView.Adapter<StyleThumbnailAdapter.ViewHolder> {
    
    public interface StyleItem {
        String getId();
        int getImageResId();
        boolean isSvg();
    }

    private List<? extends StyleItem> items;
    private String selectedId;
    private OnItemSelectedListener listener;

    public interface OnItemSelectedListener {
        void onItemSelected(StyleItem item);
    }

    public StyleThumbnailAdapter(List<? extends StyleItem> items, String selectedId, OnItemSelectedListener listener) {
        this.items = items;
        this.selectedId = selectedId;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemStyleThumbnailBinding binding = ItemStyleThumbnailBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StyleItem item = items.get(position);
        
        if (item.getImageResId() != 0) {
            if (item.isSvg()) {
                loadSvg(holder, item.getImageResId());
            } else {
                holder.binding.thumbImage.setImageResource(item.getImageResId());
            }
        } else {
            holder.binding.thumbImage.setImageDrawable(null);
        }
        
        boolean isSelected = item.getId().equals(selectedId);
        if (isSelected) {
            holder.binding.thumbCard.setStrokeColor(holder.itemView.getContext().getColor(R.color.primary));
            holder.binding.selectedOverlay.setVisibility(View.VISIBLE);
            holder.binding.thumbCard.setCardElevation(8f);
        } else {
            holder.binding.thumbCard.setStrokeColor(holder.itemView.getContext().getColor(R.color.border));
            holder.binding.selectedOverlay.setVisibility(View.GONE);
            holder.binding.thumbCard.setCardElevation(0f);
        }

        holder.itemView.setOnClickListener(v -> {
            selectedId = item.getId();
            notifyDataSetChanged();
            listener.onItemSelected(item);
        });
    }

    private void loadSvg(ViewHolder holder, int resId) {
        try {
            SVG svg = SVG.getFromResource(holder.itemView.getContext(), resId);
            // Thumbnails are usually around 100-150px
            Bitmap bitmap = Bitmap.createBitmap(150, 150, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            svg.renderToCanvas(canvas);
            holder.binding.thumbImage.setImageBitmap(bitmap);
        } catch (Exception e) {
            // Silently fail if resource not found or invalid
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ItemStyleThumbnailBinding binding;
        public ViewHolder(ItemStyleThumbnailBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
