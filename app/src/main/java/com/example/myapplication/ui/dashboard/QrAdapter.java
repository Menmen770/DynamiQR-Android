package com.example.myapplication.ui.dashboard;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.data.models.QrCode;
import com.example.myapplication.databinding.ItemQrCodeBinding;
import java.util.ArrayList;
import java.util.List;

public class QrAdapter extends RecyclerView.Adapter<QrAdapter.ViewHolder> {
    private List<QrCode> items = new ArrayList<>();

    public void setItems(List<QrCode> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemQrCodeBinding binding = ItemQrCodeBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        QrCode item = items.get(position);
        holder.binding.qrName.setText(item.getName() != null ? item.getName() : "ללא שם");
        holder.binding.qrType.setText(item.getType() != null ? item.getType() : "QR דינמי");
        holder.binding.qrStatus.setText(item.isActive() ? "פעיל" : "לא פעיל");
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ItemQrCodeBinding binding;
        ViewHolder(ItemQrCodeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
