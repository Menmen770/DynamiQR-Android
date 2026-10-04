package com.dynamiqr.android.features.generator;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.InputType;
import android.util.Base64;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.dynamiqr.android.BuildConfig;
import com.dynamiqr.android.DynamiQRApplication;
import com.dynamiqr.android.R;
import com.dynamiqr.android.core.base.BaseFragment;
import com.dynamiqr.android.core.factory.ViewModelFactory;
import com.dynamiqr.android.core.utils.ColorProvider;
import com.dynamiqr.android.core.utils.PresetLogos;
import com.dynamiqr.android.core.utils.QrLogoHelper;
import com.dynamiqr.android.core.utils.QrPreviewCompositor;
import com.dynamiqr.android.core.utils.PresetData;
import com.dynamiqr.android.core.utils.QrTypeProvider;
import com.dynamiqr.android.data.models.QrType;
import com.dynamiqr.android.databinding.DialogStaticDynamicHelpBinding;
import com.dynamiqr.android.databinding.FragmentGeneratorBinding;
import com.dynamiqr.android.databinding.LayoutGeneratorMoreTypesBinding;
import com.dynamiqr.android.databinding.LayoutQrStyleColorBinding;
import com.dynamiqr.android.databinding.LayoutQrStyleLogoBinding;
import com.dynamiqr.android.databinding.LayoutQrStyleShapeBinding;
import com.dynamiqr.android.databinding.LayoutQrStyleStickerBinding;
import com.dynamiqr.android.ui.adapters.ColorCircleAdapter;
import com.dynamiqr.android.ui.adapters.GradientSwatchAdapter;
import com.dynamiqr.android.ui.adapters.QrTypeSelectorAdapter;
import com.dynamiqr.android.ui.adapters.StyleThumbnailAdapter;
import com.dynamiqr.android.ui.components.ColorPickerSheet;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GeneratorFragment extends BaseFragment<FragmentGeneratorBinding> {

    private GeneratorViewModel viewModel;
    private String selectedType = "url";
    private Bitmap previewBitmap;
    private boolean pdfUrlMode;
    private String pickedPdfName;
    private LayoutQrStyleLogoBinding logoTabBinding;
    private StyleThumbnailAdapter logoAdapter;

    private final ActivityResultLauncher<String> logoPicker =
            registerForActivityResult(new ActivityResultContracts.GetContent(), this::onLogoPicked);
    private final ActivityResultLauncher<String> pdfPicker =
            registerForActivityResult(new ActivityResultContracts.GetContent(), this::onPdfPicked);

    @Override
    protected FragmentGeneratorBinding inflateBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentGeneratorBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewModelFactory factory = new ViewModelFactory(DynamiQRApplication.getInstance().getQrRepository());
        viewModel = new ViewModelProvider(this, factory).get(GeneratorViewModel.class);

        binding.header.setTitle(getString(R.string.generator_title));
        binding.header.setSubtitle(getString(R.string.generator_screen_subtitle));
        applyGeneratorChrome();

        setupTypeSelector();
        setupStylePanel();
        setupLinkMode();
        setupPdfOptions();
        setupWifiSecurity();
        setupActions();
        updateFieldsForType(selectedType);
        observeViewModel();
        renderStep(GeneratorViewModel.STEP_CONTENT);
    }

    private void setupWifiSecurity() {
        binding.wifiSecurityGroup.setOnCheckedStateChangeListener((group, checkedIds) ->
                updateWifiPasswordVisibility());
        updateWifiPasswordVisibility();
    }

    private void updateWifiPasswordVisibility() {
        boolean openNetwork = binding.wifiSecurityGroup.getCheckedChipId() == R.id.wifiSecOpen;
        int visibility = openNetwork ? View.GONE : View.VISIBLE;
        binding.wifiPasswordLabel.setVisibility(visibility);
        binding.wifiPasswordLayout.setVisibility(visibility);
        if (openNetwork && binding.wifiPasswordInput.getText() != null) {
            binding.wifiPasswordInput.setText("");
        }
    }

    private void setupPdfOptions() {
        binding.pdfModeFile.setText(getString(R.string.generator_pdf_mode_upload_file));
        binding.pdfModeUrl.setText(getString(R.string.generator_pdf_mode_paste_url));
        binding.pickPdfButton.setText(getString(R.string.generator_upload_choose_file));
        binding.pdfUploadHint.setText(getString(R.string.generator_preview_pdf_upload_hint));

        binding.pdfModeGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                return;
            }
            pdfUrlMode = checkedIds.get(0) == R.id.pdfModeUrl;
            refreshPdfPanels();
        });
        binding.pickPdfButton.setOnClickListener(v ->
                pdfPicker.launch("application/pdf"));
    }

    private void refreshPdfPanels() {
        boolean isPdf = "pdf".equals(selectedType);
        binding.pdfOptionsSection.setVisibility(isPdf ? View.VISIBLE : View.GONE);
        if (!isPdf) {
            if (!"wifi".equals(selectedType)) {
                binding.fieldLabel.setVisibility(View.VISIBLE);
                binding.contentLayout.setVisibility(View.VISIBLE);
            }
            binding.fieldHint.setVisibility(View.VISIBLE);
            return;
        }
        binding.pdfFilePanel.setVisibility(pdfUrlMode ? View.GONE : View.VISIBLE);
        // File mode: URL field is filled automatically after upload — keep it visible but secondary.
        // URL mode: user pastes the link themselves.
        binding.fieldLabel.setVisibility(pdfUrlMode ? View.VISIBLE : View.GONE);
        binding.contentLayout.setVisibility(pdfUrlMode ? View.VISIBLE : View.GONE);
        binding.fieldHint.setVisibility(pdfUrlMode ? View.VISIBLE : View.GONE);
        if (!pdfUrlMode && pickedPdfName != null && !pickedPdfName.isEmpty()) {
            binding.pdfFileName.setVisibility(View.VISIBLE);
            String content = binding.contentInput.getText() != null
                    ? binding.contentInput.getText().toString().trim() : "";
            if (!content.isEmpty()) {
                binding.pdfFileName.setText(getString(R.string.generator_preview_pdf_selected_title) + "\n" + pickedPdfName);
                binding.pdfUploadHint.setText(getString(R.string.generator_preview_pdf_ready_hint));
                // Keep a hidden-but-set content field for generation; show URL mode fields only when needed.
                binding.fieldLabel.setVisibility(View.GONE);
                binding.contentLayout.setVisibility(View.GONE);
                binding.fieldHint.setVisibility(View.GONE);
            } else {
                binding.pdfFileName.setText(pickedPdfName);
                binding.pdfUploadHint.setText(getString(R.string.generator_upload_uploading));
            }
        } else if (!pdfUrlMode) {
            binding.pdfFileName.setVisibility(View.GONE);
            binding.pdfUploadHint.setText(getString(R.string.generator_preview_pdf_upload_hint));
        } else {
            binding.pdfFileName.setVisibility(View.GONE);
        }
    }

    private boolean validateContent() {
        if ("wifi".equals(selectedType)) {
            String ssid = textOf(binding.wifiSsidInput);
            if (ssid.isEmpty()) {
                Toast.makeText(requireContext(),
                        getString(R.string.generator_wifi_ssid_required),
                        Toast.LENGTH_SHORT).show();
                return false;
            }
            String password = textOf(binding.wifiPasswordInput);
            String security = selectedWifiSecurity();
            if (!"nopass".equals(security) && password.isEmpty()) {
                Toast.makeText(requireContext(),
                        getString(R.string.generator_wifi_password_required),
                        Toast.LENGTH_SHORT).show();
                return false;
            }
            viewModel.setQrType(selectedType);
            viewModel.setWifiFields(ssid, password, security);
            viewModel.setMessage("");
            return true;
        }

        String text = binding.contentInput.getText() != null
                ? binding.contentInput.getText().toString().trim() : "";
        if ("pdf".equals(selectedType) && !pdfUrlMode) {
            if (text.isEmpty()) {
                Toast.makeText(requireContext(),
                        getString(R.string.generator_upload_choose_file),
                        Toast.LENGTH_SHORT).show();
                return false;
            }
            viewModel.setQrType(selectedType);
            viewModel.setContent(text);
            viewModel.setMessage("");
            return true;
        }
        if (text.isEmpty()) {
            Toast.makeText(requireContext(),
                    getString(R.string.generator_preview_empty_hint),
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        viewModel.setQrType(selectedType);
        viewModel.setContent(text);
        viewModel.setMessage(currentMessageText());
        return true;
    }

    private static String textOf(android.widget.EditText editText) {
        return editText.getText() != null ? editText.getText().toString().trim() : "";
    }

    private String selectedWifiSecurity() {
        int checked = binding.wifiSecurityGroup.getCheckedChipId();
        if (checked == R.id.wifiSecWep) {
            return "WEP";
        }
        if (checked == R.id.wifiSecOpen) {
            return "nopass";
        }
        return "WPA";
    }

    private String currentMessageText() {
        if (binding.messageInput.getText() == null) {
            return "";
        }
        return binding.messageInput.getText().toString();
    }

    private void setMessageFieldVisible(boolean visible, String hint) {
        binding.messageFieldSection.setVisibility(visible ? View.VISIBLE : View.GONE);
        if (visible) {
            binding.messageLayout.setHint(hint);
        } else if (binding.messageInput.getText() != null) {
            binding.messageInput.setText("");
        }
    }

    private void onLogoPicked(@Nullable Uri uri) {
        if (uri == null || !isAdded()) {
            return;
        }
        String dataUrl = QrLogoHelper.uriToPngDataUrl(requireContext(), uri);
        if (dataUrl == null) {
            Toast.makeText(requireContext(),
                    getString(R.string.generator_errors_svg_logo),
                    Toast.LENGTH_SHORT).show();
            return;
        }
        viewModel.setLogo("", dataUrl, 1f);
        viewModel.setLogoShape("circle");
        if (logoAdapter != null) {
            logoAdapter.setSelectedId("");
        }
        if (logoTabBinding != null) {
            logoTabBinding.logoShapeGroup.check(R.id.logoCircle);
            updateLogoGalleryUi(logoTabBinding);
            updateLogoSizePanel(logoTabBinding);
        }
    }

    private void onPdfPicked(@Nullable Uri uri) {
        if (uri == null || !isAdded()) {
            return;
        }
        pickedPdfName = queryDisplayName(uri);
        pdfUrlMode = false;
        binding.pdfModeGroup.check(R.id.pdfModeFile);
        refreshPdfPanels();
        uploadPdfAndContinue(uri, pickedPdfName);
    }

    private void uploadPdfAndContinue(Uri uri, String filename) {
        Toast.makeText(requireContext(),
                getString(R.string.generator_upload_uploading),
                Toast.LENGTH_SHORT).show();
        binding.pickPdfButton.setEnabled(false);
        binding.continueStyleButton.setEnabled(false);

        new Thread(() -> {
            String dataUrl = readUriAsDataUrl(uri, "application/pdf");
            if (dataUrl == null) {
                requireActivity().runOnUiThread(() -> {
                    if (!isAdded()) {
                        return;
                    }
                    binding.pickPdfButton.setEnabled(true);
                    binding.continueStyleButton.setEnabled(true);
                    Toast.makeText(requireContext(),
                            getString(R.string.generator_errors_pdf_read_failed),
                            Toast.LENGTH_SHORT).show();
                });
                return;
            }
            Map<String, Object> body = new HashMap<>();
            body.put("filename", filename != null ? filename : "document.pdf");
            body.put("dataUrl", dataUrl);
            DynamiQRApplication.getInstance().getQrRepository().uploadPdf(body)
                    .enqueue(new Callback<Map<String, Object>>() {
                        @Override
                        public void onResponse(Call<Map<String, Object>> call,
                                               Response<Map<String, Object>> response) {
                            if (!isAdded()) {
                                return;
                            }
                            binding.pickPdfButton.setEnabled(true);
                            binding.continueStyleButton.setEnabled(true);
                            if (!response.isSuccessful() || response.body() == null) {
                                Toast.makeText(requireContext(),
                                        getString(R.string.generator_errors_pdf_upload_failed),
                                        Toast.LENGTH_SHORT).show();
                                return;
                            }
                            String publicUrl = resolveUploadedPdfUrl(response.body());
                            if (publicUrl == null || publicUrl.isEmpty()) {
                                Toast.makeText(requireContext(),
                                        getString(R.string.generator_errors_pdf_upload_failed),
                                        Toast.LENGTH_SHORT).show();
                                return;
                            }
                            binding.contentInput.setText(publicUrl);
                            refreshPdfPanels();
                            Toast.makeText(requireContext(),
                                    getString(R.string.generator_preview_pdf_selected_title),
                                    Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                            if (!isAdded()) {
                                return;
                            }
                            binding.pickPdfButton.setEnabled(true);
                            binding.continueStyleButton.setEnabled(true);
                            Toast.makeText(requireContext(),
                                    getString(R.string.generator_errors_pdf_upload_failed),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        }).start();
    }

    @Nullable
    private String resolveUploadedPdfUrl(Map<String, Object> body) {
        String apiBase = apiBaseNoSlash();

        // Prefer path/publicId + the phone-reachable API base (never trust server localhost).
        Object path = body.get("path");
        if (path != null && !String.valueOf(path).trim().isEmpty()) {
            String p = String.valueOf(path).trim();
            return p.startsWith("/") ? apiBase + p : apiBase + "/" + p;
        }
        Object publicId = body.get("publicId");
        if (publicId != null && !String.valueOf(publicId).trim().isEmpty()) {
            return apiBase + "/api/pdf/" + String.valueOf(publicId).trim();
        }

        Object urlObj = body.get("url");
        if (urlObj == null) {
            return null;
        }
        String url = String.valueOf(urlObj).trim();
        if (url.isEmpty()) {
            return null;
        }
        return rewriteLocalhostToApiBase(url, apiBase);
    }

    private static String apiBaseNoSlash() {
        String base = BuildConfig.API_BASE_URL;
        if (base == null || base.isEmpty()) {
            return "";
        }
        return base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
    }

    /** Server often returns http://localhost:5000/... — unusable when scanning from the phone. */
    private static String rewriteLocalhostToApiBase(String url, String apiBase) {
        if (apiBase == null || apiBase.isEmpty()) {
            return url;
        }
        try {
            Uri parsed = Uri.parse(url);
            String host = parsed.getHost();
            if (host == null) {
                return url;
            }
            if (!"localhost".equalsIgnoreCase(host) && !"127.0.0.1".equals(host) && !"0.0.0.0".equals(host)) {
                return url;
            }
            String path = parsed.getEncodedPath();
            if (path == null || path.isEmpty()) {
                return apiBase;
            }
            return apiBase + (path.startsWith("/") ? path : "/" + path);
        } catch (Exception e) {
            return url.replace("http://localhost:5000", apiBase)
                    .replace("http://127.0.0.1:5000", apiBase)
                    .replace("http://localhost", apiBase)
                    .replace("http://127.0.0.1", apiBase);
        }
    }

    @Nullable
    private String readUriAsDataUrl(Uri uri, String mime) {
        try (InputStream in = requireContext().getContentResolver().openInputStream(uri)) {
            if (in == null) {
                return null;
            }
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] chunk = new byte[8192];
            int n;
            long total = 0;
            final long max = 10L * 1024L * 1024L;
            while ((n = in.read(chunk)) >= 0) {
                total += n;
                if (total > max) {
                    return null;
                }
                buffer.write(chunk, 0, n);
            }
            String base64 = Base64.encodeToString(buffer.toByteArray(), Base64.NO_WRAP);
            String type = mime != null ? mime : "application/octet-stream";
            return "data:" + type + ";base64," + base64;
        } catch (Exception e) {
            return null;
        }
    }

    private String queryDisplayName(Uri uri) {
        try (Cursor cursor = requireContext().getContentResolver()
                .query(uri, new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (idx >= 0) {
                    String name = cursor.getString(idx);
                    if (name != null && !name.isEmpty()) {
                        return name;
                    }
                }
            }
        } catch (Exception ignored) {
        }
        String last = uri.getLastPathSegment();
        return last != null ? last : "document.pdf";
    }

    private void applyGeneratorChrome() {
        binding.step1Label.setText(getString(R.string.generator_steps_content));
        binding.step2Label.setText(getString(R.string.generator_steps_style));
        binding.step3Label.setText(getString(R.string.generator_steps_export));
        binding.contentSectionTitle.setText(getString(R.string.generator_screen_content_title));
        binding.chooseTypeLabel.setText(getString(R.string.choose_qr_type));
        binding.moreTypesButton.setText(getString(R.string.type_switch));
        binding.fieldLabel.setText(getString(R.string.generator_fields_url));
        binding.fieldHint.setText(getString(R.string.generator_hints_url_scan));
        binding.linkModeLabel.setText(getString(R.string.link_mode_label));
        binding.styleSectionTitle.setText(getString(R.string.generator_screen_style_title));
        binding.exportSectionTitle.setText(getString(R.string.generator_screen_export_title));
        binding.saveCollectionLabel.setText(getString(R.string.generator_screen_save_to_collection_account));
        binding.saveNameLayout.setHint(getString(R.string.generator_placeholders_save_name_mobile));
        binding.continueStyleButton.setText(getString(R.string.generator_screen_continue_style));
        binding.continueExportButton.setText(getString(R.string.generator_screen_continue_export));
        binding.backContentButton.setText(getString(R.string.generator_screen_back_content));
        binding.backStyleButton.setText(getString(R.string.generator_screen_back_style));
        binding.shareButton.setText(getString(R.string.generator_screen_share_save));
        binding.saveButton.setText(getString(R.string.generator_screen_save_to_collection_account));
    }

    private void setupLinkMode() {
        binding.linkModeToggle.setDynamic("dynamic".equals(viewModel.getLinkMode()), false);
        binding.linkModeToggle.setOnModeChangeListener(dynamic ->
                viewModel.setLinkMode(dynamic ? "dynamic" : "static"));
        binding.linkModeHelpButton.setOnClickListener(v -> showStaticDynamicHelp());
    }

    private void showStaticDynamicHelp() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        DialogStaticDynamicHelpBinding sheet =
                DialogStaticDynamicHelpBinding.inflate(getLayoutInflater());
        dialog.setContentView(sheet.getRoot());
        sheet.helpHeading.setText(getString(R.string.generator_help_heading));
        sheet.helpIntro.setText(getString(R.string.generator_help_intro));
        sheet.helpStaticBadge.setText(getString(R.string.generator_help_static_badge));
        sheet.helpStaticTitle.setText(getString(R.string.generator_help_static_title));
        sheet.helpStaticBody.setText(getString(R.string.generator_help_static_body_mobile));
        sheet.helpDynamicBadge.setText(getString(R.string.generator_help_dynamic_badge));
        sheet.helpDynamicTitle.setText(getString(R.string.generator_help_dynamic_title));
        sheet.helpDynamicBody.setText(getString(R.string.generator_help_dynamic_body_mobile));
        sheet.helpTip.setText(getString(R.string.generator_help_tip));
        sheet.helpGotItButton.setText(getString(R.string.generator_help_got_it));
        sheet.helpCloseButton.setOnClickListener(v -> dialog.dismiss());
        sheet.helpGotItButton.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void setupTypeSelector() {
        refreshSelectedTypeCard();
        binding.moreTypesButton.setOnClickListener(v -> showMoreTypesSheet());
        binding.selectedTypeCard.setOnClickListener(v -> showMoreTypesSheet());
    }

    private void onTypeSelected(QrType type) {
        selectedType = type.getId();
        viewModel.setQrType(type.getId());
        updateFieldsForType(type.getId());
        refreshSelectedTypeCard();
    }

    private void refreshSelectedTypeCard() {
        QrType type = QrTypeProvider.findById(requireContext(), selectedType);
        binding.selectedTypeLabel.setText(type.getLabel());
        binding.selectedTypeIcon.setImageResource(type.getIconRes());
        binding.selectedTypeIcon.setColorFilter(
                ContextCompat.getColor(requireContext(), R.color.white));
        binding.selectedTypeLabel.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.white));
    }

    private void showMoreTypesSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        LayoutGeneratorMoreTypesBinding sheet = LayoutGeneratorMoreTypesBinding.inflate(getLayoutInflater());
        dialog.setContentView(sheet.getRoot());

        QrTypeSelectorAdapter moreAdapter = new QrTypeSelectorAdapter(
                QrTypeProvider.getSelectableTypes(requireContext()),
                selectedType,
                type -> {
                    onTypeSelected(type);
                    dialog.dismiss();
                });
        sheet.moreTypesRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        sheet.moreTypesRecycler.setAdapter(moreAdapter);
        dialog.show();
    }

    private void updateFieldsForType(String type) {
        boolean isWifi = "wifi".equals(type);
        binding.wifiFieldsSection.setVisibility(isWifi ? View.VISIBLE : View.GONE);
        binding.fieldLabel.setVisibility(isWifi ? View.GONE : View.VISIBLE);
        binding.contentLayout.setVisibility(isWifi ? View.GONE : View.VISIBLE);

        // Hide dynamic/static toggle for types that aren't URL/PDF destinations.
        boolean showLinkMode = "url".equals(type) || "pdf".equals(type);
        binding.linkModeSection.setVisibility(showLinkMode ? View.VISIBLE : View.GONE);

        switch (type) {
            case "email":
                binding.fieldLabel.setText(getString(R.string.generator_fields_email));
                binding.contentLayout.setHint(getString(R.string.generator_placeholders_email));
                binding.fieldHint.setText(getString(R.string.generator_hints_email_opens_short));
                binding.contentInput.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                applyContentFieldDirection(true);
                setMessageFieldVisible(false, null);
                break;
            case "phone":
                binding.fieldLabel.setText(getString(R.string.generator_fields_phone));
                binding.contentLayout.setHint(getString(R.string.generator_placeholders_phone));
                binding.fieldHint.setText(getString(R.string.generator_hints_phone_dial_short));
                binding.contentInput.setInputType(InputType.TYPE_CLASS_PHONE);
                applyContentFieldDirection(true);
                setMessageFieldVisible(false, null);
                break;
            case "whatsapp":
                binding.fieldLabel.setText(getString(R.string.generator_fields_phone));
                binding.contentLayout.setHint(getString(R.string.generator_placeholders_phone));
                binding.fieldHint.setText(getString(R.string.generator_hints_whatsapp_opens_short));
                binding.contentInput.setInputType(InputType.TYPE_CLASS_PHONE);
                applyContentFieldDirection(true);
                setMessageFieldVisible(true, getString(R.string.generator_placeholders_whatsapp_message));
                break;
            case "sms":
                binding.fieldLabel.setText(getString(R.string.generator_fields_phone));
                binding.contentLayout.setHint(getString(R.string.generator_placeholders_phone));
                binding.fieldHint.setText(getString(R.string.generator_hints_sms_opens_short));
                binding.contentInput.setInputType(InputType.TYPE_CLASS_PHONE);
                applyContentFieldDirection(true);
                setMessageFieldVisible(true, getString(R.string.generator_placeholders_sms_message));
                break;
            case "wifi":
                binding.fieldHint.setText(getString(R.string.generator_hints_wifi_connect_short));
                applyContentFieldDirection(false);
                setMessageFieldVisible(false, null);
                updateWifiPasswordVisibility();
                break;
            case "pdf":
                binding.fieldLabel.setText(getString(R.string.generator_fields_pdf_link));
                binding.contentLayout.setHint(getString(R.string.generator_placeholders_pdf_url));
                binding.fieldHint.setText(getString(R.string.generator_hints_paste_pdf_url_short));
                binding.contentInput.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
                applyContentFieldDirection(true);
                setMessageFieldVisible(false, null);
                break;
            case "contact":
                binding.fieldLabel.setText(getString(R.string.generator_types_contact));
                binding.contentLayout.setHint(getString(R.string.generator_placeholders_contact_name_example));
                binding.fieldHint.setText(getString(R.string.generator_hints_contact_save_short));
                binding.contentInput.setInputType(InputType.TYPE_CLASS_TEXT);
                applyContentFieldDirection(false);
                setMessageFieldVisible(false, null);
                break;
            default:
                binding.fieldLabel.setText(getString(R.string.generator_fields_url));
                binding.contentLayout.setHint(getString(R.string.generator_placeholders_url));
                binding.fieldHint.setText(getString(R.string.generator_hints_url_scan));
                binding.contentInput.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
                applyContentFieldDirection(true);
                setMessageFieldVisible(false, null);
                break;
        }
        refreshPdfPanels();
    }

    /** Phone / URL / email stay LTR so +972… and links read left-to-right in Hebrew UI. */
    private void applyContentFieldDirection(boolean ltr) {
        binding.contentInput.setTextDirection(ltr ? View.TEXT_DIRECTION_LTR : View.TEXT_DIRECTION_LOCALE);
        binding.contentInput.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
        binding.contentInput.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        binding.contentLayout.setLayoutDirection(ltr ? View.LAYOUT_DIRECTION_LTR : View.LAYOUT_DIRECTION_LOCALE);
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

    private void goToStyleStep() {
        if (validateContent()) {
            viewModel.goToStep(GeneratorViewModel.STEP_STYLE);
        }
    }

    private void showColorTab() {
        LayoutQrStyleColorBinding colorBinding = LayoutQrStyleColorBinding.inflate(getLayoutInflater());
        binding.stylePanel.tabContentContainer.addView(colorBinding.getRoot());
        colorBinding.modeSolid.setText(getString(R.string.generator_style_solid));
        colorBinding.modeGradient.setText(getString(R.string.generator_style_gradient));
        colorBinding.bgModeNone.setText(getString(R.string.generator_style_bg_none));
        colorBinding.bgModeSolid.setText(getString(R.string.generator_style_solid));
        colorBinding.bgModeGradient.setText(getString(R.string.generator_style_gradient));

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
                        ColorPickerSheet.show(requireContext(),
                                getString(R.string.generator_style_qr_color),
                                ColorProvider.getExtendedFgColors(), currentHex, hex -> {
                                    viewModel.setFgColor(hex);
                                    fgAdapterRef[0].setSelectedColor(hex);
                                });
                    }
                });
        colorBinding.fgColorRecyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 6));
        colorBinding.fgColorRecyclerView.setAdapter(fgAdapterRef[0]);
        colorBinding.fgColorRecyclerView.setClipChildren(false);
        colorBinding.fgColorRecyclerView.setClipToPadding(false);
        if (colorBinding.fgColorRecyclerView.getParent() instanceof ViewGroup) {
            ((ViewGroup) colorBinding.fgColorRecyclerView.getParent()).setClipChildren(false);
        }
        fixColorRecyclerHeight(colorBinding.fgColorRecyclerView, ColorProvider.getFgColors().size() + 1, 6);

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
                        ColorPickerSheet.show(requireContext(),
                                getString(R.string.generator_style_background),
                                ColorProvider.getExtendedBgColors(), currentHex, hex -> {
                                    viewModel.setBgColor(hex);
                                    bgAdapterRef[0].setSelectedColor(hex);
                                });
                    }
                });
        colorBinding.bgColorRecyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 6));
        colorBinding.bgColorRecyclerView.setAdapter(bgAdapterRef[0]);
        colorBinding.bgColorRecyclerView.setClipChildren(false);
        colorBinding.bgColorRecyclerView.setClipToPadding(false);
        if (colorBinding.bgColorRecyclerView.getParent() instanceof ViewGroup) {
            ((ViewGroup) colorBinding.bgColorRecyclerView.getParent()).setClipChildren(false);
        }
        fixColorRecyclerHeight(colorBinding.bgColorRecyclerView, ColorProvider.getBgColors().size() + 1, 6);

        GradientSwatchAdapter fgGradAdapter = new GradientSwatchAdapter(
                ColorProvider.getQrGradientPresets(), "brand-teal",
                preset -> viewModel.setGradient(preset.start, preset.end, preset.angle));
        colorBinding.fgGradientRecyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 5));
        colorBinding.fgGradientRecyclerView.setAdapter(fgGradAdapter);
        colorBinding.fgGradientRecyclerView.setClipChildren(false);
        colorBinding.fgGradientRecyclerView.setClipToPadding(false);
        fixColorRecyclerHeight(colorBinding.fgGradientRecyclerView,
                ColorProvider.getQrGradientPresets().size(), 5);

        GradientSwatchAdapter bgGradAdapter = new GradientSwatchAdapter(
                ColorProvider.getBgGradientPresets(), "peach-cream",
                preset -> {
                    viewModel.setBgGradient(preset.start, preset.end, preset.angle);
                    viewModel.setBgColor(preset.start);
                });
        colorBinding.bgGradientRecyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 4));
        colorBinding.bgGradientRecyclerView.setAdapter(bgGradAdapter);
        colorBinding.bgGradientRecyclerView.setClipChildren(false);
        colorBinding.bgGradientRecyclerView.setClipToPadding(false);
        fixColorRecyclerHeight(colorBinding.bgGradientRecyclerView,
                ColorProvider.getBgGradientPresets().size(), 4);

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
                item -> {
                    if (item != null) {
                        viewModel.setBodyShape(item.getId());
                    }
                });
        shapeBinding.bodyShapeRecyclerView.setAdapter(bodyAdapter);
        fixRecyclerHeight(shapeBinding.bodyShapeRecyclerView, 2);

        shapeBinding.eyeShapeRecyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 3));
        StyleThumbnailAdapter cornerAdapter = new StyleThumbnailAdapter(
                getCornerShapes(),
                viewModel.getSelectedCornerId(),
                item -> {
                    if (item != null) {
                        viewModel.setCornerShape(item.getId());
                    }
                });
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

    private void fixColorRecyclerHeight(androidx.recyclerview.widget.RecyclerView recyclerView,
                                        int itemCount, int columns) {
        recyclerView.setNestedScrollingEnabled(false);
        int rows = Math.max(1, (int) Math.ceil(itemCount / (float) columns));
        int itemHeight = (int) (56 * getResources().getDisplayMetrics().density);
        ViewGroup.LayoutParams params = recyclerView.getLayoutParams();
        params.height = itemHeight * rows;
        recyclerView.setLayoutParams(params);
    }

    private void showLogoTab() {
        LayoutQrStyleLogoBinding logoBinding = LayoutQrStyleLogoBinding.inflate(getLayoutInflater());
        logoTabBinding = logoBinding;
        binding.stylePanel.tabContentContainer.addView(logoBinding.getRoot());

        logoBinding.logoSourceTitle.setText(getString(R.string.generator_logo_mode_source_title));
        logoBinding.logoSourcePreset.setText(getString(R.string.generator_logo_mode_presets_short));
        logoBinding.logoSourceGallery.setText(getString(R.string.generator_logo_mode_gallery));
        logoBinding.logoPresetsLabel.setText(getString(R.string.generator_logo_mode_presets));
        logoBinding.pickLogoButton.setText(getString(R.string.generator_logo_mode_pick_from_device));
        logoBinding.removeLogoButton.setText(getString(R.string.generator_logo_mode_remove));
        logoBinding.logoSizeLabel.setText(getString(R.string.generator_logo_mode_size));
        logoBinding.logoSizeHint.setText(getString(R.string.generator_logo_mode_size_hint));
        logoBinding.logoShapeTitle.setText(getString(R.string.generator_logo_mode_shape_title));
        logoBinding.logoOverlay.setText(getString(R.string.generator_logo_mode_no_hole));
        logoBinding.logoSquare.setText(getString(R.string.generator_logo_mode_hole_square));
        logoBinding.logoCircle.setText(getString(R.string.generator_logo_mode_hole_circle));

        boolean galleryLogo = viewModel.hasLogo()
                && (viewModel.getLogoId() == null || viewModel.getLogoId().isEmpty());
        if (galleryLogo) {
            logoBinding.logoSourceGroup.check(R.id.logoSourceGallery);
            logoBinding.logoPresetPanel.setVisibility(View.GONE);
            logoBinding.logoGalleryPanel.setVisibility(View.VISIBLE);
        } else {
            logoBinding.logoSourceGroup.check(R.id.logoSourcePreset);
            logoBinding.logoPresetPanel.setVisibility(View.VISIBLE);
            logoBinding.logoGalleryPanel.setVisibility(View.GONE);
        }

        logoBinding.logoSourceGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                return;
            }
            boolean gallery = checkedIds.get(0) == R.id.logoSourceGallery;
            logoBinding.logoPresetPanel.setVisibility(gallery ? View.GONE : View.VISIBLE);
            logoBinding.logoGalleryPanel.setVisibility(gallery ? View.VISIBLE : View.GONE);
        });

        logoBinding.logoRecyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 4));
        String currentLogo = viewModel.getLogoId();
        if (currentLogo == null || "none".equals(currentLogo)) {
            currentLogo = "";
        }
        logoAdapter = new StyleThumbnailAdapter(
                getLogos(),
                currentLogo,
                item -> {
                    if (item == null) {
                        viewModel.clearLogo();
                        viewModel.setLogoShape("overlay");
                        logoBinding.logoShapeGroup.check(R.id.logoOverlay);
                        updateLogoSizePanel(logoBinding);
                        updateLogoGalleryUi(logoBinding);
                        return;
                    }
                    float inset = PresetLogos.insetForId(item.getId());
                    int resId = PresetLogos.rawResForId(requireContext(), item.getId());
                    String dataUrl = QrLogoHelper.rawSvgToDataUrl(requireContext(), resId, 512, inset);
                    // Inset is baked into the PNG; slider starts at 100% like the website.
                    viewModel.setLogo(item.getId(), dataUrl, 1f);
                    viewModel.setLogoShape("circle");
                    logoBinding.logoShapeGroup.check(R.id.logoCircle);
                    updateLogoSizePanel(logoBinding);
                    updateLogoGalleryUi(logoBinding);
                },
                true,
                true);
        logoBinding.logoRecyclerView.setAdapter(logoAdapter);
        fixRecyclerHeight(logoBinding.logoRecyclerView, 4);

        logoBinding.pickLogoButton.setOnClickListener(v -> logoPicker.launch("image/*"));
        logoBinding.removeLogoButton.setOnClickListener(v -> {
            viewModel.clearLogo();
            if (logoAdapter != null) {
                logoAdapter.setSelectedId("");
            }
            logoBinding.logoShapeGroup.check(R.id.logoOverlay);
            viewModel.setLogoShape("overlay");
            updateLogoGalleryUi(logoBinding);
            updateLogoSizePanel(logoBinding);
        });

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
            updateLogoSizePanel(logoBinding);
        });

        logoBinding.logoSizeSlider.clearOnChangeListeners();
        logoBinding.logoSizeSlider.setValue(viewModel.getLogoInsetScale());
        logoBinding.logoSizeSlider.addOnChangeListener((slider, value, fromUser) -> {
            if (fromUser) {
                viewModel.setLogoInsetScale(value);
            }
            logoBinding.logoSizeValue.setText(Math.round(value * 100) + "%");
        });
        logoBinding.logoSizeValue.setText(Math.round(viewModel.getLogoInsetScale() * 100) + "%");

        updateLogoGalleryUi(logoBinding);
        updateLogoSizePanel(logoBinding);
    }

    private void updateLogoGalleryUi(LayoutQrStyleLogoBinding logoBinding) {
        boolean hasCustom = viewModel.hasLogo()
                && (viewModel.getLogoId() == null || viewModel.getLogoId().isEmpty());
        logoBinding.pickedLogoLabel.setVisibility(hasCustom ? View.VISIBLE : View.GONE);
        logoBinding.removeLogoButton.setVisibility(viewModel.hasLogo() ? View.VISIBLE : View.GONE);
        if (hasCustom) {
            logoBinding.pickedLogoLabel.setText(
                    getString(R.string.generator_logo_mode_pick_from_device));
        }
    }

    private void updateLogoSizePanel(LayoutQrStyleLogoBinding logoBinding) {
        boolean show = viewModel.hasLogo() && !"overlay".equals(viewModel.getLogoShape());
        logoBinding.logoSizePanel.setVisibility(show ? View.VISIBLE : View.GONE);
        if (show) {
            float scale = viewModel.getLogoInsetScale();
            logoBinding.logoSizeSlider.setValue(scale);
            logoBinding.logoSizeValue.setText(Math.round(scale * 100) + "%");
        }
    }

    private void showStickerTab() {
        LayoutQrStyleStickerBinding stickerBinding = LayoutQrStyleStickerBinding.inflate(getLayoutInflater());
        binding.stylePanel.tabContentContainer.addView(stickerBinding.getRoot());
        stickerBinding.stickerRecyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 3));
        String currentSticker = viewModel.getStickerId();
        if (currentSticker == null || "none".equals(currentSticker)) {
            currentSticker = "";
        }
        StyleThumbnailAdapter stickerAdapter = new StyleThumbnailAdapter(
                getStickers(),
                currentSticker,
                item -> {
                    if (item == null) {
                        viewModel.setSticker("none");
                    } else {
                        viewModel.setSticker(item.getId());
                    }
                },
                false,
                true);
        stickerBinding.stickerRecyclerView.setAdapter(stickerAdapter);
        fixRecyclerHeight(stickerBinding.stickerRecyclerView, 6);
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
                    stickerInkFromViewModel(),
                    QrPreviewCompositor.DEFAULT_STAGE_PX);
            binding.qrPreviewImage.setImageBitmap(previewBitmap);
            binding.qrExportPreviewImage.setImageBitmap(previewBitmap);
        } catch (Exception ignored) {
        }
    }

    private void sharePreview() {
        Toast.makeText(requireContext(),
                getString(R.string.generator_preview_generating),
                Toast.LENGTH_SHORT).show();
        viewModel.generateExport(new GeneratorViewModel.ExportCallback() {
            @Override
            public void onSuccess(String dataUrl) {
                if (!isAdded()) {
                    return;
                }
                Bitmap exportBitmap = buildExportBitmap(dataUrl);
                if (exportBitmap == null) {
                    Toast.makeText(requireContext(),
                            getString(R.string.generator_errors_generate_failed),
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                shareBitmap(exportBitmap);
                if (exportBitmap != previewBitmap) {
                    exportBitmap.recycle();
                }
            }

            @Override
            public void onError(String message) {
                if (!isAdded()) {
                    return;
                }
                Toast.makeText(requireContext(),
                        message != null ? message : getString(R.string.generator_errors_generate_failed),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private Bitmap buildExportBitmap(String dataUrl) {
        try {
            String base64 = dataUrl.contains(",") ? dataUrl.substring(dataUrl.indexOf(',') + 1) : dataUrl;
            byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
            Bitmap qrBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            if (qrBitmap == null) {
                return null;
            }
            Bitmap composed = QrPreviewCompositor.compositeForExport(
                    requireContext(),
                    qrBitmap,
                    viewModel.getStickerId(),
                    viewModel.getBgColor(),
                    stickerInkFromViewModel());
            if (qrBitmap != composed) {
                qrBitmap.recycle();
            }
            return composed;
        } catch (Exception e) {
            return null;
        }
    }

    private QrPreviewCompositor.StickerInk stickerInkFromViewModel() {
        if ("gradient".equals(viewModel.getColorMode())) {
            return QrPreviewCompositor.StickerInk.gradient(
                    viewModel.getGradientStart(),
                    viewModel.getGradientEnd(),
                    viewModel.getGradientAngle());
        }
        return QrPreviewCompositor.StickerInk.solid(viewModel.getFgColor());
    }

    private void shareBitmap(Bitmap bitmap) {
        try {
            File cacheDir = new File(requireContext().getCacheDir(), "qr_exports");
            if (!cacheDir.exists()) {
                cacheDir.mkdirs();
            }
            File file = new File(cacheDir, "dynamiqr_" + System.currentTimeMillis() + ".png");
            try (FileOutputStream out = new FileOutputStream(file)) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                out.flush();
            }
            Uri uri = FileProvider.getUriForFile(
                    requireContext(),
                    requireContext().getPackageName() + ".fileprovider",
                    file);
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("image/png");
            share.putExtra(Intent.EXTRA_STREAM, uri);
            share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(share,
                    getString(R.string.generator_screen_share_save)));
        } catch (Exception e) {
            Toast.makeText(requireContext(),
                    getString(R.string.generator_errors_generate_failed),
                    Toast.LENGTH_SHORT).show();
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
