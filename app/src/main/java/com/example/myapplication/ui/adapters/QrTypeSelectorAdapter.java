package com.example.myapplication.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.data.models.QrType;
import com.example.myapplication.databinding.ItemQrTypeBinding;
import java.util.List;

public class QrTypeSelectorAdapter extends RecyclerView.Adapter<QrTypeSelectorAdapter.ViewHolder> {

    private final List<QrType> items;
    private String selectedId;
    private final OnTypeSelectedListener listener;

    public interface OnTypeSelectedListener {
        void onTypeSelected(QrType type);
    }

    public QrTypeSelectorAdapter(List<QrType> items, String selectedId, OnTypeSelectedListener listener) {
        this.items = items;
        this.selectedId = selectedId;
        this.listener = listener;
    }

    public void setSelectedId(String selectedId) {
        this.selectedId = selectedId;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemQrTypeBinding binding = ItemQrTypeBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        QrType item = items.get(position);
        holder.binding.typeLabel.setText(item.getLabel());
        holder.binding.typeIcon.setImageResource(item.getIconRes());

        boolean isSelected = item.getId().equals(selectedId);
        if (isSelected) {
            holder.binding.typeInner.setBackgroundResource(R.drawable.bg_type_selected);
            holder.binding.typeLabel.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.white));
            holder.binding.typeIcon.setColorFilter(ContextCompat.getColor(holder.itemView.getContext(), R.color.white));
        } else {
            holder.binding.typeInner.setBackgroundResource(R.drawable.bg_type_idle);
            holder.binding.typeLabel.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.text_main));
            holder.binding.typeIcon.setColorFilter(ContextCompat.getColor(holder.itemView.getContext(), R.color.sub_text));
        }

        holder.itemView.setOnClickListener(v -> {
            selectedId = item.getId();
            notifyDataSetChanged();
            if (listener != null) {
                listener.onTypeSelected(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemQrTypeBinding binding;

        ViewHolder(ItemQrTypeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
