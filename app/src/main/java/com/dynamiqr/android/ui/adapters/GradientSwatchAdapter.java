package com.dynamiqr.android.ui.adapters;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.dynamiqr.android.core.utils.ColorProvider;
import com.dynamiqr.android.databinding.ItemGradientSwatchBinding;
import java.util.List;

public class GradientSwatchAdapter extends RecyclerView.Adapter<GradientSwatchAdapter.ViewHolder> {

    public interface Listener {
        void onGradientSelected(ColorProvider.GradientPreset preset);
    }

    private final List<ColorProvider.GradientPreset> presets;
    private String selectedId;
    private final Listener listener;

    public GradientSwatchAdapter(List<ColorProvider.GradientPreset> presets,
                                 String selectedId, Listener listener) {
        this.presets = presets;
        this.selectedId = selectedId;
        this.listener = listener;
    }

    public void setSelectedId(String id) {
        this.selectedId = id;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemGradientSwatchBinding binding = ItemGradientSwatchBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ColorProvider.GradientPreset preset = presets.get(position);
        GradientDrawable gd = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{Color.parseColor(preset.start), Color.parseColor(preset.end)});
        gd.setShape(GradientDrawable.OVAL);
        holder.binding.gradientSwatch.setBackground(gd);

        boolean selected = preset.id.equals(selectedId);
        holder.binding.selectionRing.setVisibility(selected ? View.VISIBLE : View.GONE);

        holder.itemView.setOnClickListener(v -> {
            selectedId = preset.id;
            notifyDataSetChanged();
            listener.onGradientSelected(preset);
        });
    }

    @Override
    public int getItemCount() {
        return presets.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final ItemGradientSwatchBinding binding;

        public ViewHolder(ItemGradientSwatchBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
