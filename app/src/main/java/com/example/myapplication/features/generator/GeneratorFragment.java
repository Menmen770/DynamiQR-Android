package com.example.myapplication.features.generator;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.myapplication.DynamiQRApplication;
import com.example.myapplication.R;
import com.example.myapplication.core.base.BaseFragment;
import com.example.myapplication.core.factory.ViewModelFactory;
import com.example.myapplication.core.utils.ColorProvider;
import com.example.myapplication.core.utils.PresetLogos;
import com.example.myapplication.core.utils.QrLogoHelper;
import com.example.myapplication.core.utils.QrPreviewCompositor;
import com.example.myapplication.core.utils.PresetData;
import com.example.myapplication.core.utils.QrTypeProvider;
import com.example.myapplication.data.models.QrType;
import com.example.myapplication.databinding.FragmentGeneratorBinding;
import com.example.myapplication.databinding.LayoutGeneratorMoreTypesBinding;
import com.example.myapplication.databinding.LayoutQrStyleColorBinding;
import com.example.myapplication.databinding.LayoutQrStyleLogoBinding;
import com.example.myapplication.databinding.LayoutQrStyleShapeBinding;
import com.example.myapplication.databinding.LayoutQrStyleStickerBinding;
import com.example.myapplication.ui.adapters.ColorCircleAdapter;
import com.example.myapplication.ui.adapters.GradientSwatchAdapter;
import com.example.myapplication.ui.adapters.QrTypeSelectorAdapter;
import com.example.myapplication.ui.adapters.StyleThumbnailAdapter;
import com.example.myapplication.ui.components.ColorPickerSheet;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class GeneratorFragment extends BaseFragment<FragmentGeneratorBinding> {

    private GeneratorViewModel viewModel;
    private String selectedType = "url";
    private QrTypeSelectorAdapter mainTypeAdapter;
    private Bitmap previewBitmap;

    @Override
    protected FragmentGeneratorBinding inflateBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentGeneratorBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewModelFactory factory = new ViewModelFactory(DynamiQRApplication.getInstance().getQrRepository());
        viewModel = new ViewModelProvider(this, factory).get(GeneratorViewModel.class);

        binding.header.setTitle("יוצר קודי QR");
        binding.header.setSubtitle("בחר סוג, עצב ושמור — שלושה שלבים פשוטים");

        setupTypeSelector();
        setupStylePanel();
        setupActions();
        updateFieldsForType(selectedType);
        observeViewModel();
        renderStep(GeneratorViewModel.STEP_CONTENT);
    }

    private void setupTypeSelector() {
        mainTypeAdapter = new QrTypeSelectorAdapter(
                QrTypeProvider.getMainTypes(),
                selectedType,
                this::onTypeSelected);
        binding.typeRecyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        binding.typeRecyclerView.setAdapter(mainTypeAdapter);

        binding.moreTypesButton.setOnClickListener(v -> showMoreTypesSheet());
    }

    private void onTypeSelected(QrType type) {
        selectedType = type.getId();
        viewModel.setQrType(type.getId());
        updateFieldsForType(type.getId());
        updateMoreTypesButton();
    }

    private void updateMoreTypesButton() {
        if (QrTypeProvider.isMoreType(selectedType)) {
            QrType type = QrTypeProvider.findById(selectedType);
            binding.moreTypesButton.setText(type.getLabel());
            binding.moreTypesButton.setStrokeColorResource(R.color.primary);
            binding.moreTypesButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary));
        } else {
            binding.moreTypesButton.setText("עוד אפשרויות");
            binding.moreTypesButton.setStrokeColorResource(R.color.border);
            binding.moreTypesButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.sub_text));
        }
    }

    private void showMoreTypesSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        LayoutGeneratorMoreTypesBinding sheet = LayoutGeneratorMoreTypesBinding.inflate(getLayoutInflater());
        dialog.setContentView(sheet.getRoot());

        QrTypeSelectorAdapter moreAdapter = new QrTypeSelectorAdapter(
                QrTypeProvider.getMoreTypes(),
                selectedType,
                type -> {
                    onTypeSelected(type);
                    mainTypeAdapter.setSelectedId(type.getId());
                    dialog.dismiss();
                });
        sheet.moreTypesRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        sheet.moreTypesRecycler.setAdapter(moreAdapter);
        dialog.show();
    }

    private void updateFieldsForType(String type) {
        binding.linkModeGroup.setVisibility(
                "url".equals(type) || "pdf".equals(type) ? View.VISIBLE : View.GONE);

        switch (type) {
            case "email":
                binding.fieldLabel.setText("כתובת אימייל");
                binding.contentLayout.setHint("name@example.com");
                binding.fieldHint.setText("יפתח אפליקציית דואר בסריקה");
                binding.contentInput.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                break;
            case "phone":
                binding.fieldLabel.setText("מספר טלפון");
                binding.contentLayout.setHint("+972 50 123 4567");
                binding.fieldHint.setText("יפתח חיוג בסריקה");
                binding.contentInput.setInputType(InputType.TYPE_CLASS_PHONE);
                break;
            case "whatsapp":
                binding.fieldLabel.setText("מספר וואטסאפ");
                binding.contentLayout.setHint("+972 50 123 4567");
                binding.fieldHint.setText("יפתח שיחת וואטסאפ");
                binding.contentInput.setInputType(InputType.TYPE_CLASS_PHONE);
                break;
            case "wifi":
                binding.fieldLabel.setText("פרטי רשת Wi-Fi");
                binding.contentLayout.setHint("SSID,סיסמה");
                binding.fieldHint.setText("פורמט: שם רשת,סיסמה");
                binding.contentInput.setInputType(InputType.TYPE_CLASS_TEXT);
                break;
            case "pdf":
                binding.fieldLabel.setText("קישור לקובץ PDF");
                binding.contentLayout.setHint("https://example.com/file.pdf");
                binding.fieldHint.setText("קישור ישיר לקובץ PDF");
                binding.contentInput.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
                break;
            case "contact":
                binding.fieldLabel.setText("פרטי איש קשר");
                binding.contentLayout.setHint("שם, טלפון, אימייל");
                binding.fieldHint.setText("הפרד בפסיקים בין השדות");
                binding.contentInput.setInputType(InputType.TYPE_CLASS_TEXT);
                break;
            default:
                binding.fieldLabel.setText("כתובת URL");
                binding.contentLayout.setHint("https://example.com");
                binding.fieldHint.setText("הקישור ייפתח בסריקה");
                binding.contentInput.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
                break;
        }
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

    private void setupActions() {
        binding.continueStyleButton.setOnClickListener(v -> goToStyleStep());
        binding.continueExportButton.setOnClickListener(v -> viewModel.goToStep(GeneratorViewModel.STEP_EXPORT));
        binding.backContentButton.setOnClickListener(v -> viewModel.goToStep(GeneratorViewModel.STEP_CONTENT));
        binding.backStyleButton.setOnClickListener(v -> viewModel.goToStep(GeneratorViewModel.STEP_STYLE));
        binding.saveButton.setOnClickListener(v -> {
            String name = binding.saveNameInput.getText().toString().trim();
            viewModel.saveQr(name);
        });
        binding.shareButton.setOnClickListener(v -> sharePreview());

        binding.step1Item.setOnClickListener(v -> viewModel.goToStep(GeneratorViewModel.STEP_CONTENT));
        binding.step2Item.setOnClickListener(v -> {
            if (validateContent()) {
                viewModel.goToStep(GeneratorViewModel.STEP_STYLE);
            }
        });
        binding.step3Item.setOnClickListener(v -> {
            if (validateContent()) {
                viewModel.goToStep(GeneratorViewModel.STEP_EXPORT);
            }
        });
    }

    private boolean validateContent() {
        String text = binding.contentInput.getText().toString().trim();
        if (text.isEmpty()) {
            Toast.makeText(requireContext(), "אנא הזן תוכן", Toast.LENGTH_SHORT).show();
            return false;
        }
        viewModel.setQrType(selectedType);
        viewModel.setContent(text);
        return true;
    }

    private void goToStyleStep() {
        if (validateContent()) {
            viewModel.goToStep(GeneratorViewModel.STEP_STYLE);
        }
    }

    private void showColorTab() {
        LayoutQrStyleColorBinding colorBinding = LayoutQrStyleColorBinding.inflate(getLayoutInflater());
        binding.stylePanel.tabContentContainer.addView(colorBinding.getRoot());

        String currentFg = viewModel.getFgColor() != null ? viewModel.getFgColor() : "#111111";
        String currentBg = "#ffffff".equals(viewModel.getBgColor()) || viewModel.getBgColor() == null
                ? "#ffffff" : viewModel.getBgColor();

        final ColorCircleAdapter[] fgAdapterRef = new ColorCircleAdapter[1];
        fgAdapterRef[0] = new ColorCircleAdapter(
                ColorProvider.getFgColors(), currentFg,
                new ColorCircleAdapter.Listener() {
                    @Override
                    public void onColorSelected(String hex) {
                        viewModel.setFgColor(hex);
                    }

                    @Override
                    public void onCustomClicked(String currentHex) {
                        ColorPickerSheet.show(requireContext(), "צבע ה-QR",
                                ColorProvider.getExtendedFgColors(), currentHex, hex -> {
                                    viewModel.setFgColor(hex);
                                    fgAdapterRef[0].setSelectedColor(hex);
                                });
                    }
                });
        colorBinding.fgColorRecyclerView.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        colorBinding.fgColorRecyclerView.setAdapter(fgAdapterRef[0]);

        final ColorCircleAdapter[] bgAdapterRef = new ColorCircleAdapter[1];
        bgAdapterRef[0] = new ColorCircleAdapter(
                ColorProvider.getBgColors(), currentBg,
                new ColorCircleAdapter.Listener() {
                    @Override
                    public void onColorSelected(String hex) {
                        viewModel.setBgColor(hex);
                    }

                    @Override
                    public void onCustomClicked(String currentHex) {
                        ColorPickerSheet.show(requireContext(), "צבע רקע",
                                ColorProvider.getExtendedBgColors(), currentHex, hex -> {
                                    viewModel.setBgColor(hex);
                                    bgAdapterRef[0].setSelectedColor(hex);
                                });
                    }
                });
        colorBinding.bgColorRecyclerView.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        colorBinding.bgColorRecyclerView.setAdapter(bgAdapterRef[0]);

        GradientSwatchAdapter fgGradAdapter = new GradientSwatchAdapter(
                ColorProvider.getQrGradientPresets(), "brand-teal",
                preset -> viewModel.setGradient(preset.start, preset.end, preset.angle));
        colorBinding.fgGradientRecyclerView.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        colorBinding.fgGradientRecyclerView.setAdapter(fgGradAdapter);

        GradientSwatchAdapter bgGradAdapter = new GradientSwatchAdapter(
                ColorProvider.getBgGradientPresets(), "peach-cream",
                preset -> {
                    viewModel.setBgGradient(preset.start, preset.end, preset.angle);
                    viewModel.setBgColor(preset.start);
                });
        colorBinding.bgGradientRecyclerView.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        colorBinding.bgGradientRecyclerView.setAdapter(bgGradAdapter);

        // Angle chips
        colorBinding.gradientAngleGroup.removeAllViews();
        int currentAngle = viewModel.getGradientAngle();
        for (Integer angle : ColorProvider.getGradientAngles()) {
            Chip chip = new Chip(requireContext());
            chip.setText(angle + "°");
            chip.setCheckable(true);
            chip.setChecked(angle == currentAngle || (currentAngle == 135 && angle == 135));
            chip.setOnClickListener(v -> viewModel.setGradient(
                    viewModel.getGradientStart(), viewModel.getGradientEnd(), angle));
            colorBinding.gradientAngleGroup.addView(chip);
        }

        boolean isGradient = "gradient".equals(viewModel.getColorMode());
        if (isGradient) {
            colorBinding.colorModeGroup.check(R.id.modeGradient);
        } else {
            colorBinding.colorModeGroup.check(R.id.modeSolid);
        }
        applyFgModeUi(colorBinding, isGradient);

        colorBinding.colorModeGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            boolean grad = checkedIds.contains(R.id.modeGradient);
            applyFgModeUi(colorBinding, grad);
            viewModel.setColorMode(grad ? "gradient" : "solid");
            if (grad) {
                ColorProvider.GradientPreset first = ColorProvider.getQrGradientPresets().get(0);
                viewModel.setGradient(first.start, first.end, first.angle);
            }
        });

        String bgMode = viewModel.getBgColorMode();
        if ("none".equals(bgMode)) {
            colorBinding.bgModeGroup.check(R.id.bgModeNone);
        } else if ("gradient".equals(bgMode)) {
            colorBinding.bgModeGroup.check(R.id.bgModeGradient);
        } else {
            colorBinding.bgModeGroup.check(R.id.bgModeSolid);
        }
        applyBgModeUi(colorBinding, bgMode);

        colorBinding.bgModeGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            String mode = "solid";
            if (checkedIds.contains(R.id.bgModeNone)) {
                mode = "none";
            } else if (checkedIds.contains(R.id.bgModeGradient)) {
                mode = "gradient";
            }
            applyBgModeUi(colorBinding, mode);
            viewModel.setBgColorMode(mode);
            if ("gradient".equals(mode)) {
                ColorProvider.GradientPreset first = ColorProvider.getBgGradientPresets().get(0);
                viewModel.setBgGradient(first.start, first.end, first.angle);
                viewModel.setBgColor(first.start);
            } else if ("none".equals(mode)) {
                viewModel.setBgColor("#ffffff");
            }
        });
    }

    private void applyFgModeUi(LayoutQrStyleColorBinding b, boolean gradient) {
        b.fgColorRecyclerView.setVisibility(gradient ? View.GONE : View.VISIBLE);
        b.gradientPanel.setVisibility(gradient ? View.VISIBLE : View.GONE);
    }

    private void applyBgModeUi(LayoutQrStyleColorBinding b, String mode) {
        boolean solid = "solid".equals(mode);
        boolean gradient = "gradient".equals(mode);
        b.bgColorRecyclerView.setVisibility(solid ? View.VISIBLE : View.GONE);
        b.bgGradientRecyclerView.setVisibility(gradient ? View.VISIBLE : View.GONE);
    }

    private void showShapeTab() {
        LayoutQrStyleShapeBinding shapeBinding = LayoutQrStyleShapeBinding.inflate(getLayoutInflater());
        binding.stylePanel.tabContentContainer.addView(shapeBinding.getRoot());
        shapeBinding.bodyShapeRecyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 3));
        StyleThumbnailAdapter bodyAdapter = new StyleThumbnailAdapter(
                getBodyShapes(),
                viewModel.getSelectedBodyId(),
                item -> viewModel.setBodyShape(item.getId()));
        shapeBinding.bodyShapeRecyclerView.setAdapter(bodyAdapter);
        fixRecyclerHeight(shapeBinding.bodyShapeRecyclerView, 2);

        shapeBinding.eyeShapeRecyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 3));
        StyleThumbnailAdapter cornerAdapter = new StyleThumbnailAdapter(
                getCornerShapes(),
                viewModel.getSelectedCornerId(),
                item -> viewModel.setCornerShape(item.getId()));
        shapeBinding.eyeShapeRecyclerView.setAdapter(cornerAdapter);
        fixRecyclerHeight(shapeBinding.eyeShapeRecyclerView, 2);
    }

    private void fixRecyclerHeight(androidx.recyclerview.widget.RecyclerView recyclerView, int rows) {
        recyclerView.setNestedScrollingEnabled(false);
        int itemHeight = (int) (80 * getResources().getDisplayMetrics().density);
        ViewGroup.LayoutParams params = recyclerView.getLayoutParams();
        params.height = itemHeight * rows;
        recyclerView.setLayoutParams(params);
    }

    private void showLogoTab() {
        LayoutQrStyleLogoBinding logoBinding = LayoutQrStyleLogoBinding.inflate(getLayoutInflater());
        binding.stylePanel.tabContentContainer.addView(logoBinding.getRoot());
        logoBinding.logoRecyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 4));

        String currentLogo = viewModel.getLogoId();
        if (currentLogo == null || currentLogo.isEmpty()) {
            currentLogo = "none";
        }
        StyleThumbnailAdapter logoAdapter = new StyleThumbnailAdapter(
                getLogos(),
                currentLogo,
                item -> {
                    if ("none".equals(item.getId())) {
                        viewModel.clearLogo();
                    } else {
                        float inset = PresetLogos.insetForId(item.getId());
                        int resId = PresetLogos.rawResForId(requireContext(), item.getId());
                        String dataUrl = QrLogoHelper.rawSvgToDataUrl(requireContext(), resId, 256, inset);
                        // בחירת לוגו לא משנה את צורת החור — נשארת בחירת המשתמש (ברירת מחדל: ללא חור)
                        viewModel.setLogo(item.getId(), dataUrl, inset);
                    }
                },
                true);
        logoBinding.logoRecyclerView.setAdapter(logoAdapter);
        fixRecyclerHeight(logoBinding.logoRecyclerView, 4);

        logoBinding.logoShapeGroup.setOnCheckedStateChangeListener(null);
        String shape = viewModel.getLogoShape();
        if ("circle".equals(shape)) {
            logoBinding.logoShapeGroup.check(R.id.logoCircle);
        } else if ("square".equals(shape)) {
            logoBinding.logoShapeGroup.check(R.id.logoSquare);
        } else {
            logoBinding.logoShapeGroup.check(R.id.logoOverlay);
        }

        logoBinding.logoShapeGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                return;
            }
            int id = checkedIds.get(0);
            if (id == R.id.logoCircle) {
                viewModel.setLogoShape("circle");
            } else if (id == R.id.logoSquare) {
                viewModel.setLogoShape("square");
            } else {
                viewModel.setLogoShape("overlay");
            }
        });
    }

    private void showStickerTab() {
        LayoutQrStyleStickerBinding stickerBinding = LayoutQrStyleStickerBinding.inflate(getLayoutInflater());
        binding.stylePanel.tabContentContainer.addView(stickerBinding.getRoot());
        stickerBinding.stickerRecyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 3));
        StyleThumbnailAdapter stickerAdapter = new StyleThumbnailAdapter(
                getStickers(),
                viewModel.getStickerId(),
                item -> viewModel.setSticker(item.getId()));
        stickerBinding.stickerRecyclerView.setAdapter(stickerAdapter);
        fixRecyclerHeight(stickerBinding.stickerRecyclerView, 7);
    }

    private void observeViewModel() {
        viewModel.getCurrentStep().observe(getViewLifecycleOwner(), this::renderStep);
        viewModel.getQrPreviewImage().observe(getViewLifecycleOwner(), this::showPreviewImage);
        viewModel.isLoading().observe(getViewLifecycleOwner(), loading -> {
            boolean isLoading = Boolean.TRUE.equals(loading);
            binding.previewLoading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.continueStyleButton.setEnabled(!isLoading);
            binding.saveButton.setEnabled(!isLoading);
        });
        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
        viewModel.getSaveMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) {
                binding.saveMessageText.setText(msg);
                binding.saveMessageText.setVisibility(View.VISIBLE);
            }
        });
    }

    private void showPreviewImage(String dataUrl) {
        if (dataUrl == null || dataUrl.isEmpty()) {
            return;
        }
        try {
            String base64 = dataUrl.contains(",") ? dataUrl.substring(dataUrl.indexOf(',') + 1) : dataUrl;
            byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
            Bitmap qrBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            String sticker = viewModel.getStickerId();
            previewBitmap = QrPreviewCompositor.composite(
                    requireContext(),
                    qrBitmap,
                    sticker,
                    viewModel.getBgColor(),
                    viewModel.getFgColor(),
                    QrPreviewCompositor.DEFAULT_STAGE_PX);
            binding.qrPreviewImage.setImageBitmap(previewBitmap);
            binding.qrExportPreviewImage.setImageBitmap(previewBitmap);
        } catch (Exception ignored) {
        }
    }

    private void sharePreview() {
        if (previewBitmap == null) {
            Toast.makeText(requireContext(), "אין תצוגה מקדימה לשיתוף", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            File cacheDir = new File(requireContext().getCacheDir(), "qr_exports");
            if (!cacheDir.exists()) {
                cacheDir.mkdirs();
            }
            File file = new File(cacheDir, "dynamiqr_" + System.currentTimeMillis() + ".png");
            try (FileOutputStream out = new FileOutputStream(file)) {
                previewBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            }
            Uri uri = FileProvider.getUriForFile(
                    requireContext(),
                    requireContext().getPackageName() + ".fileprovider",
                    file);
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("image/png");
            share.putExtra(Intent.EXTRA_STREAM, uri);
            share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(share, "שתף QR"));
        } catch (Exception e) {
            Toast.makeText(requireContext(), "שיתוף נכשל", Toast.LENGTH_SHORT).show();
        }
    }

    private void renderStep(int step) {
        boolean isContent = step == GeneratorViewModel.STEP_CONTENT;
        boolean isStyle = step == GeneratorViewModel.STEP_STYLE;
        boolean isExport = step == GeneratorViewModel.STEP_EXPORT;

        binding.stepContentCard.setVisibility(isContent ? View.VISIBLE : View.GONE);
        binding.stepStyleCard.setVisibility(isStyle ? View.VISIBLE : View.GONE);
        binding.stepExportCard.setVisibility(isExport ? View.VISIBLE : View.GONE);

        styleStepIndicator(step);

        if (isStyle) {
            binding.stylePanel.styleTabGroup.check(R.id.tabColor);
        }
    }

    private void styleStepIndicator(int step) {
        styleBadge(binding.step1Badge, binding.step1Label, step, GeneratorViewModel.STEP_CONTENT);
        styleBadge(binding.step2Badge, binding.step2Label, step, GeneratorViewModel.STEP_STYLE);
        styleBadge(binding.step3Badge, binding.step3Label, step, GeneratorViewModel.STEP_EXPORT);

        boolean line1Done = step > GeneratorViewModel.STEP_CONTENT;
        boolean line2Done = step > GeneratorViewModel.STEP_STYLE;
        binding.stepLine1.setBackgroundResource(line1Done ? R.drawable.bg_step_line_done : R.drawable.bg_step_line_idle);
        binding.stepLine2.setBackgroundResource(line2Done ? R.drawable.bg_step_line_done : R.drawable.bg_step_line_idle);
    }

    private void styleBadge(TextView badge, TextView label, int currentStep, int badgeStep) {
        boolean active = currentStep == badgeStep;
        boolean done = currentStep > badgeStep;
        if (active) {
            badge.setBackgroundResource(R.drawable.bg_step_circle_active);
            badge.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
            label.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary));
        } else if (done) {
            badge.setBackgroundResource(R.drawable.bg_step_badge_done);
            badge.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary));
            label.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary));
        } else {
            badge.setBackgroundResource(R.drawable.bg_step_circle_inactive);
            badge.setTextColor(ContextCompat.getColor(requireContext(), R.color.sub_text));
            label.setTextColor(ContextCompat.getColor(requireContext(), R.color.sub_text));
        }
    }

    private List<PresetData.StyleItemImpl> getStickers() {
        List<PresetData.StyleItemImpl> items = new ArrayList<>();
        items.add(new PresetData.StyleItemImpl("none", R.drawable.ic_close, false));
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
        items.add(new PresetData.StyleItemImpl("none", R.drawable.ic_close, false));
        for (PresetLogos.Preset preset : PresetLogos.all()) {
            items.add(new PresetData.StyleItemImpl(preset.id, preset.rawResId, true));
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
        for (int i = 1; i <= 6; i++) {
            int resId = getResources().getIdentifier("corner_thumb_0" + i, "drawable", requireContext().getPackageName());
            if (resId != 0) {
                items.add(new PresetData.StyleItemImpl("corner_" + i, resId, false));
            }
        }
        return items;
    }
}
