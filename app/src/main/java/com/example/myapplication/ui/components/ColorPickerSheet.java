package com.example.myapplication.ui.components;

import android.content.Context;
import android.view.LayoutInflater;
import androidx.recyclerview.widget.GridLayoutManager;
import com.example.myapplication.databinding.BottomSheetColorPickerBinding;
import com.example.myapplication.ui.adapters.ColorCircleAdapter;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import java.util.List;

/** בחירת צבע מפלטה מוכנה — בלי הקלדת HEX (כמו עיפרון ב-RN, בלי שדה מספרים). */
public final class ColorPickerSheet {

    public interface OnPicked {
        void onPicked(String hex);
    }

    private ColorPickerSheet() {
    }

    public static void show(Context context, String title, List<String> palette,
                            String current, OnPicked onPicked) {
        BottomSheetDialog dialog = new BottomSheetDialog(context);
        BottomSheetColorPickerBinding binding = BottomSheetColorPickerBinding.inflate(
                LayoutInflater.from(context));
        dialog.setContentView(binding.getRoot());

        binding.pickerTitle.setText(title);
        final String[] draft = {current != null ? current : palette.get(0)};

        ColorCircleAdapter adapter = new ColorCircleAdapter(
                palette, draft[0],
                new ColorCircleAdapter.Listener() {
                    @Override
                    public void onColorSelected(String hex) {
                        draft[0] = hex;
                    }

                    @Override
                    public void onCustomClicked(String currentHex) {
                        // אין עיפרון בתוך הגיליון
                    }
                },
                false);
        binding.pickerRecycler.setLayoutManager(new GridLayoutManager(context, 5));
        binding.pickerRecycler.setAdapter(adapter);

        binding.pickerApply.setOnClickListener(v -> {
            onPicked.onPicked(draft[0]);
            dialog.dismiss();
        });

        dialog.show();
    }
}
