package com.example.myapplication.ui.adapters;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.databinding.ItemColorCircleBinding;
import java.util.ArrayList;
import java.util.List;

/**
 * שורת צבעים כמו ב-RN: עיגולי preset + כפתור עיפרון בסוף.
 */
public class ColorCircleAdapter extends RecyclerView.Adapter<ColorCircleAdapter.ViewHolder> {

    public static final String CUSTOM_ID = "__custom__";

    public interface Listener {
        void onColorSelected(String hex);

        void onCustomClicked(String currentHex);
    }

    private final List<String> colors;
    private String selectedColor;
    private final Listener listener;
    private final boolean includePencil;

    public ColorCircleAdapter(List<String> colors, String selectedColor, Listener listener) {
        this(colors, selectedColor, listener, true);
    }

    public ColorCircleAdapter(List<String> colors, String selectedColor,
                              Listener listener, boolean includePencil) {
        this.colors = new ArrayList<>(colors);
        this.selectedColor = selectedColor != null ? selectedColor : "#111111";
        this.listener = listener;
        this.includePencil = includePencil;
    }

    public void setSelectedColor(String hex) {
        this.selectedColor = hex;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (includePencil && position == colors.size()) {
            return 1;
        }
        return 0;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemColorCircleBinding binding = ItemColorCircleBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        boolean isPencil = includePencil && position == colors.size();

        if (isPencil) {
            GradientDrawable pencilBg = new GradientDrawable();
            pencilBg.setShape(GradientDrawable.OVAL);
            try {
                pencilBg.setColor(Color.parseColor(selectedColor));
            } catch (Exception e) {
                pencilBg.setColor(Color.GRAY);
            }
            holder.binding.colorCircle.setBackground(pencilBg);
            holder.binding.editIcon.setVisibility(View.VISIBLE);
            holder.binding.editIcon.setColorFilter(Color.WHITE);
            holder.binding.selectedCheck.setVisibility(View.GONE);
            holder.binding.selectionRing.setVisibility(View.VISIBLE);
            holder.itemView.setOnClickListener(v -> listener.onCustomClicked(selectedColor));
            return;
        }

        String hex = colors.get(position);
        GradientDrawable solid = new GradientDrawable();
        solid.setShape(GradientDrawable.OVAL);
        try {
            solid.setColor(Color.parseColor(hex));
        } catch (Exception e) {
            solid.setColor(Color.GRAY);
        }
        // מסגרת עדינה לצבעים בהירים
        solid.setStroke(Math.round(1 * holder.itemView.getResources().getDisplayMetrics().density),
                Color.parseColor("#D1D5DB"));
        holder.binding.colorCircle.setBackground(solid);
        holder.binding.editIcon.setVisibility(View.GONE);

        boolean isSelected = hex.equalsIgnoreCase(selectedColor);
        holder.binding.selectionRing.setVisibility(isSelected ? View.VISIBLE : View.GONE);
        holder.binding.selectedCheck.setVisibility(isSelected ? View.VISIBLE : View.GONE);

        holder.itemView.setOnClickListener(v -> {
            selectedColor = hex;
            notifyDataSetChanged();
            listener.onColorSelected(hex);
        });
    }

    @Override
    public int getItemCount() {
        return colors.size() + (includePencil ? 1 : 0);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final ItemColorCircleBinding binding;

        public ViewHolder(ItemColorCircleBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
