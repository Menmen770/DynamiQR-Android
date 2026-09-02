package com.example.myapplication.ui.generator;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import com.example.myapplication.databinding.FragmentGeneratorBinding;

public class GeneratorFragment extends Fragment {
    private FragmentGeneratorBinding binding;
    private int currentStep = 1;
    private String selectedTypeId = "url";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentGeneratorBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.header.setTitle("צור קוד חדש");
        binding.header.setSubtitle("בחר סוג, הזן תוכן ועצב את ה-QR שלך");

        setupTypeSelector();

        binding.nextButton.setOnClickListener(v -> handleNext());
    }

    private void setupTypeSelector() {
        QrTypeSelectorAdapter adapter = new QrTypeSelectorAdapter(
            QrTypeProvider.getTypes(), 
            selectedTypeId, 
            type -> selectedTypeId = type.getId()
        );
        binding.typeRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        binding.typeRecyclerView.setAdapter(adapter);
    }

    private void handleNext() {
        if (currentStep == 1) {
            String text = binding.contentInput.getText().toString();
            if (text.isEmpty()) {
                Toast.makeText(getContext(), "אנא הזן תוכן", Toast.LENGTH_SHORT).show();
                return;
            }
            currentStep = 2;
            updateUiForStep2(text);
        } else {
            Toast.makeText(getContext(), "הקוד נשמר!", Toast.LENGTH_SHORT).show();
            requireActivity().onBackPressed();
        }
    }

    private void updateUiForStep2(String text) {
        binding.stepTitle.setText("עיצוב וייצוא");
        binding.typeRecyclerView.setVisibility(View.GONE);
        binding.contentLayout.setVisibility(View.GONE);
        binding.qrPreview.setVisibility(View.VISIBLE);
        binding.nextButton.setText("שמור באוסף שלי");
        
        try {
            android.graphics.Bitmap bitmap = (android.graphics.Bitmap) qrcode.QRCode.ofRoundedSquares()
                    .withSize(15)
                    .build(text)
                    .render()
                    .nativeImage();
            binding.qrPreview.setImageBitmap(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
