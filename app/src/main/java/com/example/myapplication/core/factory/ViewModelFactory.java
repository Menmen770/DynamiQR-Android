package com.example.myapplication.core.factory;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.example.myapplication.data.repository.QrRepository;
import com.example.myapplication.features.dashboard.DashboardViewModel;
import com.example.myapplication.features.generator.GeneratorViewModel;

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
