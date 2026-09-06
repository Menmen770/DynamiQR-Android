package com.example.myapplication.features.dashboard;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.myapplication.DynamiQRApplication;
import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.core.base.BaseFragment;
import com.example.myapplication.core.factory.ViewModelFactory;
import com.example.myapplication.core.i18n.AppI18n;
import com.example.myapplication.core.utils.QrPreviewLoader;
import com.example.myapplication.data.models.FolderState;
import com.example.myapplication.data.models.QrCode;
import com.example.myapplication.databinding.FragmentDashboardBinding;
import com.example.myapplication.databinding.LayoutFolderPickerBinding;
import com.example.myapplication.databinding.LayoutFolderSheetBinding;
import com.example.myapplication.databinding.LayoutSimplePromptBinding;
import com.example.myapplication.ui.adapters.QrAdapter;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import java.io.File;
import java.io.FileOutputStream;

public class DashboardFragment extends BaseFragment<FragmentDashboardBinding> {

    private static final long SEARCH_DEBOUNCE_MS = 350;

    private QrAdapter adapter;
    private QrPreviewLoader previewLoader;
    private DashboardViewModel viewModel;
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable pendingSearch;

    @Override
    protected FragmentDashboardBinding inflateBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentDashboardBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        DynamiQRApplication app = DynamiQRApplication.getInstance();
        ViewModelFactory factory = new ViewModelFactory(app.getQrRepository());
        viewModel = new ViewModelProvider(this, factory).get(DashboardViewModel.class);
        previewLoader = new QrPreviewLoader(app.getQrRepository());

        binding.pageHeader.setTitle(AppI18n.t(requireContext(), "dashboard", "page.title",
                getString(R.string.dashboard_title)));
        binding.pageHeader.setSubtitle(AppI18n.t(requireContext(), "dashboard", "page.subtitleMobile",
                getString(R.string.dashboard_promo_desc)));
        applyDashboardChrome();

        adapter = new QrAdapter(new QrAdapter.QrActionListener() {
            @Override
            public void onDelete(QrCode qr) {
                confirmDelete(qr);
            }

            @Override
            public void onEdit(QrCode qr) {
                QuickEditBottomSheetFragment.newInstance(qr)
                        .show(getChildFragmentManager(), "quick_edit");
            }

            @Override
            public void onRename(QrCode qr) {
                showRenameDialog(qr);
            }

            @Override
            public void onToggleActive(QrCode qr, boolean active) {
                viewModel.setActive(qr.getId(), active, null);
            }

            @Override
            public void onStats(QrCode qr) {
                if (!"dynamic".equals(qr.getLinkMode())) {
                    Toast.makeText(requireContext(),
                            AppI18n.t(requireContext(), "dashboard", "card.statsDynamicOnly",
                                    "Stats are available for dynamic codes only"),
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                StatsBottomSheetFragment.newInstance(qr)
                        .show(getChildFragmentManager(), "stats");
            }

            @Override
            public void onChangeFolder(QrCode qr) {
                showFolderPicker(qr);
            }

            @Override
            public void onShare(QrCode qr) {
                shareQr(qr);
            }

            @Override
            public String folderNameFor(QrCode qr) {
                return viewModel.folderNameForQr(qr.getId());
            }
        });
        adapter.setPreviewLoader(previewLoader);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setAdapter(adapter);

        observeViewModel();
        setupSearch();
        setupFilters();

        binding.swipeRefresh.setColorSchemeResources(R.color.primary);
        binding.swipeRefresh.setOnRefreshListener(() -> {
            if (previewLoader != null) {
                previewLoader.clearCache();
            }
            viewModel.loadQrs(binding.searchInput.getText().toString());
        });

        binding.emptyCreateButton.setOnClickListener(v -> navigateToGenerator());

        if (savedInstanceState == null) {
            viewModel.loadQrs(null);
        }
    }

    private void applyDashboardChrome() {
        binding.searchLayout.setHint(AppI18n.t(requireContext(), "dashboard", "page.searchPlaceholder",
                "Search by code name…"));
        binding.filterSheetChip.setText(AppI18n.t(requireContext(), "dashboard", "page.filterAndFolders",
                "Filter & folders"));
        binding.emptyTitle.setText(AppI18n.t(requireContext(), "dashboard", "empty.filterTitle",
                "No codes in this filter"));
        binding.emptySubtitle.setText(AppI18n.t(requireContext(), "dashboard", "empty.viewEmpty",
                "No codes in this view."));
        binding.emptyCreateButton.setText(AppI18n.t(requireContext(), "dashboard", "page.createNew",
                "Create new QR"));
        updateFilterChips();
    }

    private void shareQr(QrCode qr) {
        Toast.makeText(requireContext(),
                AppI18n.t(requireContext(), "generator", "preview.generating", "Preparing high-quality file…"),
                Toast.LENGTH_SHORT).show();
        previewLoader.loadExportBitmap(requireContext(), qr, new QrPreviewLoader.ExportCallback() {
            @Override
            public void onReady(Bitmap bitmap) {
                if (!isAdded()) {
                    if (bitmap != null) {
                        bitmap.recycle();
                    }
                    return;
                }
                try {
                    File cacheDir = new File(requireContext().getCacheDir(), "qr_exports");
                    if (!cacheDir.exists()) {
                        cacheDir.mkdirs();
                    }
                    String safeName = qr.getDisplayName() != null
                            ? qr.getDisplayName().replaceAll("[^a-zA-Z0-9-_]", "_")
                            : "dynamiqr";
                    File file = new File(cacheDir, safeName + "_" + System.currentTimeMillis() + ".png");
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
                            getString(R.string.action_share)));
                } catch (Exception e) {
                    Toast.makeText(requireContext(),
                            AppI18n.t(requireContext(), "generator", "errors.generateFailed", "Share failed"),
                            Toast.LENGTH_SHORT).show();
                } finally {
                    bitmap.recycle();
                }
            }

            @Override
            public void onError(String message) {
                if (!isAdded()) {
                    return;
                }
                Toast.makeText(requireContext(),
                        AppI18n.t(requireContext(), "generator", "errors.generateFailed", "Share failed"),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmDelete(QrCode qr) {
        String name = qr.getDisplayName() != null ? qr.getDisplayName()
                : AppI18n.t(requireContext(), "dashboard", "card.codeFallback", "Code");
        new AlertDialog.Builder(requireContext())
                .setTitle(AppI18n.t(requireContext(), "dashboard", "card.deleteTitle", "Delete code"))
                .setMessage(AppI18n.t(requireContext(), "dashboard", "card.deleteConfirmShort",
                        "Delete this saved code? It cannot be restored.").replace("this saved code", "\"" + name + "\""))
                .setNegativeButton(AppI18n.t(requireContext(), "dashboard", "actions.cancel", "Cancel"), null)
                .setPositiveButton(AppI18n.t(requireContext(), "dashboard", "actions.delete", "Delete"), (d, w) ->
                        viewModel.deleteQr(qr.getId(), () ->
                                Toast.makeText(requireContext(),
                                        AppI18n.t(requireContext(), "dashboard", "card.deleted", "Code deleted"),
                                        Toast.LENGTH_SHORT).show()))
                .show();
    }

    private void showRenameDialog(QrCode qr) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        LayoutSimplePromptBinding sheet = LayoutSimplePromptBinding.inflate(getLayoutInflater());
        dialog.setContentView(sheet.getRoot());
        sheet.promptTitle.setText(AppI18n.t(requireContext(), "dashboard", "card.renameTitle", "Rename"));
        sheet.promptDescription.setText(AppI18n.t(requireContext(), "dashboard", "card.renameHint",
                "Shown in your saved codes list"));
        sheet.promptInput.setHint(AppI18n.t(requireContext(), "dashboard", "card.renamePlaceholder", "Code name"));
        sheet.promptInput.setText(qr.getDisplayName() != null ? qr.getDisplayName() : "");
        sheet.promptInput.setSelection(sheet.promptInput.getText() != null
                ? sheet.promptInput.getText().length() : 0);
        sheet.confirmBtn.setText(AppI18n.t(requireContext(), "dashboard", "actions.save", "Save"));
        sheet.cancelBtn.setOnClickListener(v -> dialog.dismiss());
        sheet.confirmBtn.setOnClickListener(v -> {
            String name = sheet.promptInput.getText() != null
                    ? sheet.promptInput.getText().toString().trim() : "";
            if (name.isEmpty()) {
                Toast.makeText(requireContext(),
                        AppI18n.t(requireContext(), "dashboard", "card.renameRequired", "Name is required"),
                        Toast.LENGTH_SHORT).show();
                return;
            }
            viewModel.renameQr(qr.getId(), name, () ->
                    Toast.makeText(requireContext(),
                            AppI18n.t(requireContext(), "dashboard", "card.renamed", "Name updated"),
                            Toast.LENGTH_SHORT).show());
            dialog.dismiss();
        });
        dialog.show();
    }

    private void showFolderPicker(QrCode qr) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        LayoutFolderPickerBinding sheet = LayoutFolderPickerBinding.inflate(getLayoutInflater());
        dialog.setContentView(sheet.getRoot());

        FolderState state = viewModel.getFolderState().getValue();
        String currentId = null;
        if (state != null) {
            currentId = state.getAssignments().get(qr.getId());
        }

        addFolderOption(sheet.folderOptions,
                AppI18n.t(requireContext(), "dashboard", "sidebar.unfiled", "Unfiled"),
                currentId == null, () -> {
            viewModel.assignFolder(qr.getId(), null, () -> adapter.notifyDataSetChanged());
            dialog.dismiss();
        });

        if (state != null) {
            for (FolderState.FolderItem folder : state.getFolders()) {
                boolean selected = folder.getId().equals(currentId);
                addFolderOption(sheet.folderOptions, folder.getName(), selected, () -> {
                    viewModel.assignFolder(qr.getId(), folder.getId(), () -> adapter.notifyDataSetChanged());
                    dialog.dismiss();
                });
            }
        }

        sheet.createFolderInPicker.setOnClickListener(v -> {
            dialog.dismiss();
            showCreateFolderDialog(qr);
        });

        dialog.show();
    }

    private void showCreateFolderDialog(QrCode assignAfterCreate) {
        EditText input = new EditText(requireContext());
        input.setHint(AppI18n.t(requireContext(), "dashboard", "sidebar.folderNamePlaceholder", "Folder name"));
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        int pad = (int) (20 * getResources().getDisplayMetrics().density);
        input.setPadding(pad, pad, pad, pad);

        new AlertDialog.Builder(requireContext())
                .setTitle(AppI18n.t(requireContext(), "dashboard", "sidebar.newFolderTitle", "New folder"))
                .setView(input)
                .setNegativeButton(AppI18n.t(requireContext(), "dashboard", "actions.cancel", "Cancel"), null)
                .setPositiveButton(AppI18n.t(requireContext(), "dashboard", "sidebar.createFolderConfirm", "Create folder"),
                        (d, w) -> {
                    String name = input.getText() != null ? input.getText().toString().trim() : "";
                    viewModel.createFolder(name, () -> {
                        FolderState state = viewModel.getFolderState().getValue();
                        if (assignAfterCreate != null && state != null) {
                            for (FolderState.FolderItem folder : state.getFolders()) {
                                if (name.equals(folder.getName())) {
                                    viewModel.assignFolder(assignAfterCreate.getId(), folder.getId(),
                                            () -> adapter.notifyDataSetChanged());
                                    break;
                                }
                            }
                        }
                        Toast.makeText(requireContext(),
                                AppI18n.t(requireContext(), "dashboard", "sidebar.folderCreated", "Folder created"),
                                Toast.LENGTH_SHORT).show();
                    });
                })
                .show();
    }

    private void addFolderOption(LinearLayout container, String label, boolean selected, Runnable onClick) {
        TextView option = new TextView(requireContext());
        option.setText(label);
        option.setTextSize(15);
        option.setPadding(dp(14), dp(14), dp(14), dp(14));
        option.setBackgroundResource(selected ? R.drawable.bg_stats_btn : R.drawable.bg_folder_chip);
        option.setTextColor(ContextCompat.getColor(requireContext(),
                selected ? R.color.primary : R.color.text_main));
        option.setTypeface(option.getTypeface(), android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = dp(8);
        option.setLayoutParams(lp);
        option.setOnClickListener(v -> onClick.run());
        container.addView(option);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private void setupSearch() {
        binding.searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (pendingSearch != null) {
                    searchHandler.removeCallbacks(pendingSearch);
                }
                pendingSearch = () -> viewModel.loadQrs(s.toString().trim());
                searchHandler.postDelayed(pendingSearch, SEARCH_DEBOUNCE_MS);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void setupFilters() {
        binding.filterSheetChip.setOnClickListener(v -> showFilterSheet());
        binding.activityFilterChip.setOnClickListener(v -> showFilterSheet());
        updateFilterChips();
    }

    private void showFilterSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        LayoutFolderSheetBinding sheet = LayoutFolderSheetBinding.inflate(getLayoutInflater());
        dialog.setContentView(sheet.getRoot());

        DashboardViewModel.ActivityFilter current = viewModel.getActivityFilter();
        if (current == DashboardViewModel.ActivityFilter.ALL) {
            sheet.chipAll.setChecked(true);
        } else if (current == DashboardViewModel.ActivityFilter.ACTIVE) {
            sheet.chipActive.setChecked(true);
        } else {
            sheet.chipInactive.setChecked(true);
        }

        sheet.statusChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                return;
            }
            int id = checkedIds.get(0);
            if (id == R.id.chipActive) {
                viewModel.setActivityFilter(DashboardViewModel.ActivityFilter.ACTIVE);
            } else if (id == R.id.chipInactive) {
                viewModel.setActivityFilter(DashboardViewModel.ActivityFilter.INACTIVE);
            } else {
                viewModel.setActivityFilter(DashboardViewModel.ActivityFilter.ALL);
            }
            updateFilterChips();
            dialog.dismiss();
        });

        sheet.createFolderBtn.setOnClickListener(v -> {
            dialog.dismiss();
            showCreateFolderDialog(null);
        });

        dialog.show();
    }

    private void updateFilterChips() {
        DashboardViewModel.ActivityFilter filter = viewModel.getActivityFilter();
        String activityLabel;
        if (filter == DashboardViewModel.ActivityFilter.ACTIVE) {
            activityLabel = AppI18n.t(requireContext(), "dashboard", "sidebar.active", "Active");
        } else if (filter == DashboardViewModel.ActivityFilter.INACTIVE) {
            activityLabel = AppI18n.t(requireContext(), "dashboard", "sidebar.inactive", "Inactive");
        } else {
            activityLabel = AppI18n.t(requireContext(), "dashboard", "sidebar.all", "All");
        }
        binding.activityFilterChip.setText(activityLabel);
        binding.viewLabelChip.setText(AppI18n.t(requireContext(), "dashboard", "sidebar.allCodes", "All codes"));
    }

    private void observeViewModel() {
        viewModel.getQrs().observe(getViewLifecycleOwner(), qrs -> {
            boolean isEmpty = qrs == null || qrs.isEmpty();
            binding.emptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            binding.recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
            if (qrs != null) {
                adapter.setItems(qrs);
            }
        });

        viewModel.getFolderState().observe(getViewLifecycleOwner(), state ->
                adapter.notifyDataSetChanged());

        viewModel.isLoading().observe(getViewLifecycleOwner(), isLoading -> {
            boolean loading = Boolean.TRUE.equals(isLoading);
            binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
            binding.swipeRefresh.setRefreshing(loading && binding.swipeRefresh.isRefreshing());
            if (!loading) {
                binding.swipeRefresh.setRefreshing(false);
            }
        });

        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToGenerator() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).navigateToTab(R.id.nav_generator);
        }
    }

    @Override
    public void onDestroyView() {
        if (pendingSearch != null) {
            searchHandler.removeCallbacks(pendingSearch);
        }
        super.onDestroyView();
    }
}
