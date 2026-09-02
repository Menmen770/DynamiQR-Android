package com.example.myapplication.ui.generator;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.databinding.ItemQrTypeBinding;
import java.util.List;

public class QrTypeSelectorAdapter extends RecyclerView.Adapter<QrTypeSelectorAdapter.ViewHolder> {
    private List<QrType> items;
    private String selectedId;
    private OnTypeSelectedListener listener;

    public interface OnTypeSelectedListener {
        void onTypeSelected(QrType type);
    }

    public QrTypeSelectorAdapter(List<QrType> items, String selectedId, OnTypeSelectedListener listener) {
        this.items = items;
        this.selectedId = selectedId;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemQrTypeBinding binding = ItemQrTypeBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        QrType item = items.get(position);
        holder.binding.typeLabel.setText(item.getLabel());
        holder.binding.typeIcon.setImageResource(item.getIconRes());

        boolean isSelected = item.getId().equals(selectedId);
        if (isSelected) {
            holder.binding.typeCard.setStrokeColor(holder.itemView.getContext().getColor(R.color.primary));
            holder.binding.typeInner.setBackgroundColor(holder.itemView.getContext().getColor(R.color.primary));
            holder.binding.typeLabel.setTextColor(holder.itemView.getContext().getColor(R.color.white));
            holder.binding.typeIcon.setColorFilter(holder.itemView.getContext().getColor(R.color.white));
        } else {
            holder.binding.typeCard.setStrokeColor(holder.itemView.getContext().getColor(R.color.border));
            holder.binding.typeInner.setBackgroundColor(holder.itemView.getContext().getColor(R.color.surface));
            holder.binding.typeLabel.setTextColor(holder.itemView.getContext().getColor(R.color.text_main));
            holder.binding.typeIcon.setColorFilter(holder.itemView.getContext().getColor(R.color.sub_text));
        }

        holder.itemView.setOnClickListener(v -> {
            selectedId = item.getId();
            notifyDataSetChanged();
            listener.onTypeSelected(item);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ItemQrTypeBinding binding;
        ViewHolder(ItemQrTypeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
