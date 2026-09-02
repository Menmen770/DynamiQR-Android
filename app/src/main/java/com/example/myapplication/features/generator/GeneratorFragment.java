package com.example.myapplication.features.generator;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.myapplication.R;
import com.example.myapplication.core.base.BaseFragment;
import com.example.myapplication.data.models.QrOptions;
import com.example.myapplication.core.utils.QrTypeProvider;
import com.example.myapplication.core.utils.ColorProvider;
import com.example.myapplication.core.utils.PresetData;
import com.example.myapplication.databinding.FragmentGeneratorBinding;
import com.example.myapplication.databinding.LayoutQrStyleColorBinding;
import com.example.myapplication.databinding.LayoutQrStyleLogoBinding;
import com.example.myapplication.databinding.LayoutQrStyleShapeBinding;
import com.example.myapplication.databinding.LayoutQrStyleStickerBinding;
import com.example.myapplication.ui.adapters.ColorCircleAdapter;
import com.example.myapplication.ui.adapters.StyleThumbnailAdapter;
import com.example.myapplication.ui.adapters.QrTypeSelectorAdapter;
import java.util.ArrayList;
import java.util.List;

public class GeneratorFragment extends BaseFragment<FragmentGeneratorBinding> {
    private QrOptions options = new QrOptions();
    private int currentStep = 1;

    @Override
    protected FragmentGeneratorBinding inflateBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentGeneratorBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.header.setTitle("צור קוד חדש");
        binding.header.setSubtitle("בחר סוג, הזן תוכן ועצב את ה-QR שלך");

        setupTypeSelector();
        setupStylePanel();

        binding.nextButton.setOnClickListener(v -> handleNext());
    }

    private void setupTypeSelector() {
        QrTypeSelectorAdapter adapter = new QrTypeSelectorAdapter(
            QrTypeProvider.getTypes(), 
            "url", 
            type -> {}
        );
        binding.typeRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        binding.typeRecyclerView.setAdapter(adapter);
    }

    private void setupStylePanel() {
        binding.stylePanel.styleTabGroup.setOnCheckedChangeListener((group, checkedId) -> {
            binding.stylePanel.tabContentContainer.removeAllViews();
            if (checkedId == R.id.tabColor) {
                showColorTab();
            } else if (checkedId == R.id.tabShape) {
                showShapeTab();
            } else if (checkedId == R.id.tabLogo) {
                showLogoTab();
            } else if (checkedId == R.id.tabSticker) {
                showStickerTab();
            }
        });
    }

    private void showColorTab() {
        LayoutQrStyleColorBinding colorBinding = LayoutQrStyleColorBinding.inflate(getLayoutInflater());
        binding.stylePanel.tabContentContainer.addView(colorBinding.getRoot());

        colorBinding.fgColorRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        colorBinding.fgColorRecyclerView.setAdapter(new ColorCircleAdapter(
            ColorProvider.getFgColors(), 
            options.getFgColor(), 
            hex -> {
                options.setFgColor(hex);
                binding.qrRenderer.setOptions(options);
            }
        ));

        colorBinding.bgColorRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        colorBinding.bgColorRecyclerView.setAdapter(new ColorCircleAdapter(
            ColorProvider.getBgColors(), 
            options.getBgColor(), 
            hex -> {
                options.setBgColor(hex);
                binding.qrRenderer.setOptions(options);
            }
        ));
    }

    private void showShapeTab() {
        LayoutQrStyleShapeBinding shapeBinding = LayoutQrStyleShapeBinding.inflate(getLayoutInflater());
        binding.stylePanel.tabContentContainer.addView(shapeBinding.getRoot());

        shapeBinding.bodyShapeRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        shapeBinding.bodyShapeRecyclerView.setAdapter(new StyleThumbnailAdapter(
            getBodyShapes(), 
            "body_1",
            item -> {
                int idx = Integer.parseInt(item.getId().split("_")[1]) - 1;
                options.setBodyShape(QrOptions.BodyShape.values()[idx]);
                binding.qrRenderer.setOptions(options);
            }
        ));

        shapeBinding.eyeShapeRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        shapeBinding.eyeShapeRecyclerView.setAdapter(new StyleThumbnailAdapter(
            getCornerShapes(), 
            "corner_1",
            item -> {
                int idx = Integer.parseInt(item.getId().split("_")[1]) - 1;
                options.setEyeShape(QrOptions.EyeShape.values()[idx]);
                binding.qrRenderer.setOptions(options);
            }
        ));
    }

    private void showLogoTab() {
        LayoutQrStyleLogoBinding logoBinding = LayoutQrStyleLogoBinding.inflate(getLayoutInflater());
        binding.stylePanel.tabContentContainer.addView(logoBinding.getRoot());

        logoBinding.logoRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 4));
        logoBinding.logoRecyclerView.setAdapter(new StyleThumbnailAdapter(
            getLogos(), 
            options.getLogoUrl(),
            item -> {
                options.setLogoUrl(item.getId());
                binding.qrRenderer.setOptions(options);
            }
        ));
    }

    private void showStickerTab() {
        LayoutQrStyleStickerBinding stickerBinding = LayoutQrStyleStickerBinding.inflate(getLayoutInflater());
        binding.stylePanel.tabContentContainer.addView(stickerBinding.getRoot());

        stickerBinding.stickerRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        stickerBinding.stickerRecyclerView.setAdapter(new StyleThumbnailAdapter(
            getStickers(), 
            options.getStickerId(),
            item -> {
                options.setStickerId(item.getId());
                binding.qrRenderer.setOptions(options);
            }
        ));
    }

    private List<PresetData.StyleItemImpl> getStickers() {
        List<PresetData.StyleItemImpl> items = new ArrayList<>();
        items.add(new PresetData.StyleItemImpl("none", android.R.drawable.ic_menu_close_clear_cancel, false));
        for (int i = 1; i <= 18; i++) {
            String id = String.format("frame-%02d", i);
            String name = String.format("sticker_thumb_%02d", i);
            int resId = getResources().getIdentifier(name, "drawable", requireContext().getPackageName());
            if (resId != 0) {
                items.add(new PresetData.StyleItemImpl(id, resId, false));
            }
        }
        return items;
    }

    private List<PresetData.StyleItemImpl> getLogos() {
        List<PresetData.StyleItemImpl> items = new ArrayList<>();
        String[] brands = {"whatsapp", "instagram", "facebook", "linkedin", "telegram", "spotify", "google", "github", "youtube", "tiktok", "x", "bit", "google_maps", "waze"};
        for (String brand : brands) {
            int resId = getResources().getIdentifier(brand, "raw", requireContext().getPackageName());
            if (resId != 0) {
                items.add(new PresetData.StyleItemImpl(brand, resId, true));
            }
        }
        return items;
    }

    private List<PresetData.StyleItemImpl> getBodyShapes() {
        List<PresetData.StyleItemImpl> items = new ArrayList<>();
        for (int i = 1; i <= 6; i++) {
            int resId = getResources().getIdentifier("body_shape_0" + i, "raw", requireContext().getPackageName());
            if (resId != 0) {
                items.add(new PresetData.StyleItemImpl("body_" + i, resId, true));
            }
        }
        return items;
    }

    private List<PresetData.StyleItemImpl> getCornerShapes() {
        List<PresetData.StyleItemImpl> items = new ArrayList<>();
        for (int i = 1; i <= 7; i++) {
            int resId = getResources().getIdentifier("corner_thumb_0" + i, "drawable", requireContext().getPackageName());
            if (resId != 0) {
                items.add(new PresetData.StyleItemImpl("corner_" + i, resId, false));
            }
        }
        return items;
    }

    private void handleNext() {
        if (currentStep == 1) {
            String text = binding.contentInput.getText().toString();
            if (text.isEmpty()) {
                Toast.makeText(getContext(), "אנא הזן תוכן", Toast.LENGTH_SHORT).show();
                return;
            }
            options.setContent(text);
            currentStep = 2;
            updateUiForStep2();
        } else {
            Toast.makeText(getContext(), "הקוד נשמר!", Toast.LENGTH_SHORT).show();
            requireActivity().onBackPressed();
        }
    }

    private void updateUiForStep2() {
        binding.stepTitle.setText("עיצוב וייצוא");
        binding.typeRecyclerView.setVisibility(View.GONE);
        binding.contentLayout.setVisibility(View.GONE);
        binding.qrRenderer.setVisibility(View.VISIBLE);
        binding.stylePanel.getRoot().setVisibility(View.VISIBLE);
        binding.nextButton.setText("שמור באוסף שלי");
        
        binding.qrRenderer.setOptions(options);
        binding.stylePanel.styleTabGroup.check(R.id.tabColor);
    }
}
