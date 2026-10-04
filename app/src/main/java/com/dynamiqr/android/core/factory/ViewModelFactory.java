package com.dynamiqr.android.core.factory;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.dynamiqr.android.data.repository.QrRepository;
import com.dynamiqr.android.features.dashboard.DashboardViewModel;
import com.dynamiqr.android.features.generator.GeneratorViewModel;

public class ViewModelFactory implements ViewModelProvider.Factory {

    private final QrRepository qrRepository;

    public ViewModelFactory(QrRepository qrRepository) {
        this.qrRepository = qrRepository;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(DashboardViewModel.class)) {
            return (T) new DashboardViewModel(qrRepository);
        }
        if (modelClass.isAssignableFrom(GeneratorViewModel.class)) {
            return (T) new GeneratorViewModel(qrRepository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}
