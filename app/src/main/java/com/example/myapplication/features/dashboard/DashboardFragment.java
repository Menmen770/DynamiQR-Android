package com.example.myapplication.features.dashboard;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.myapplication.DynamiQRApplication;
import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.core.base.BaseFragment;
import com.example.myapplication.core.factory.ViewModelFactory;
import com.example.myapplication.data.models.QrCode;
import com.example.myapplication.databinding.FragmentDashboardBinding;
import com.example.myapplication.databinding.LayoutFolderSheetBinding;
import com.example.myapplication.core.utils.QrPreviewLoader;
import com.example.myapplication.ui.adapters.QrAdapter;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;

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

        binding.pageHeader.setTitle("הקודים שלי");
        binding.pageHeader.setSubtitle("נהל, ערוך ועקוב אחרי כל קודי ה-QR שלך");

        adapter = new QrAdapter(new QrAdapter.QrActionListener() {
            @Override
            public void onDelete(QrCode qr) {
                viewModel.deleteQr(qr.getId(), () ->
                        Toast.makeText(requireContext(), "הקוד נמחק", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onEdit(QrCode qr) {
                QuickEditBottomSheetFragment.newInstance(qr)
                        .show(getChildFragmentManager(), "quick_edit");
            }

            @Override
            public void onDetails(QrCode qr) {
                StatsBottomSheetFragment.newInstance(qr)
                        .show(getChildFragmentManager(), "stats");
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

        sheet.createFolderBtn.setOnClickListener(v ->
                Toast.makeText(requireContext(), "תיקיות יתווספו בגרסה הבאה", Toast.LENGTH_SHORT).show());

        dialog.show();
    }

    private void updateFilterChips() {
        binding.activityFilterChip.setText(viewModel.getActivityFilter().getLabel());
        binding.viewLabelChip.setText("כל הקודים הפעילים");
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
