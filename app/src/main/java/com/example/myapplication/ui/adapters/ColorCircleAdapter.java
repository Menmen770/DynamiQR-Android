package com.example.myapplication.ui.adapters;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.databinding.ItemColorCircleBinding;
import java.util.List;

public class ColorCircleAdapter extends RecyclerView.Adapter<ColorCircleAdapter.ViewHolder> {
    private List<String> colors;
    private String selectedColor;
    private OnColorSelectedListener listener;

    public interface OnColorSelectedListener {
        void onColorSelected(String hex);
    }

    public ColorCircleAdapter(List<String> colors, String selectedColor, OnColorSelectedListener listener) {
        this.colors = colors;
        this.selectedColor = selectedColor;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemColorCircleBinding binding = ItemColorCircleBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String hex = colors.get(position);
        GradientDrawable bg = (GradientDrawable) holder.binding.colorCircle.getBackground();
        bg.setColor(Color.parseColor(hex));

        holder.binding.selectedIndicator.setVisibility(hex.equalsIgnoreCase(selectedColor) ? View.VISIBLE : View.GONE);

        holder.itemView.setOnClickListener(v -> {
            selectedColor = hex;
            notifyDataSetChanged();
            listener.onColorSelected(hex);
        });
    }

    @Override
    public int getItemCount() {
        return colors.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ItemColorCircleBinding binding;
        ViewHolder(ItemColorCircleBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
