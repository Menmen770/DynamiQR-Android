package com.example.myapplication.ui.generator;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.myapplication.databinding.ViewQrPreviewCompositeBinding;
import qrcode.QRCode;

public class QrPreviewComposite extends FrameLayout {
    private ViewQrPreviewCompositeBinding binding;

    public QrPreviewComposite(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        binding = ViewQrPreviewCompositeBinding.inflate(LayoutInflater.from(context), this, true);
    }

    public void updateQr(String content, String fgColorHex, String bgColorHex) {
        try {
            Bitmap bitmap = (Bitmap) QRCode.ofRoundedSquares()
                    .withColor(qrcode.color.Colors.css(fgColorHex))
                    .withBackgroundColor(qrcode.color.Colors.css(bgColorHex))
                    .withSize(15)
                    .build(content)
                    .render()
                    .nativeImage();
            binding.qrImageView.setImageBitmap(bitmap);
            binding.previewBackground.setBackgroundColor(Color.parseColor(bgColorHex));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
