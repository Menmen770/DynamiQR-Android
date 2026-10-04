package com.dynamiqr.android.features.scanner;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import com.dynamiqr.android.R;
import com.dynamiqr.android.core.base.BaseFragment;
import com.dynamiqr.android.databinding.FragmentScannerBinding;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ScannerFragment extends BaseFragment<FragmentScannerBinding> {

    private static final int PERMISSION_REQUEST_CODE = 10;
    private static final long SCAN_COOLDOWN_MS = 2500;

    private ExecutorService cameraExecutor;
    private Camera camera;
    private boolean torchOn;
    private long lastScanAt;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected FragmentScannerBinding inflateBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentScannerBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        cameraExecutor = Executors.newSingleThreadExecutor();
        applyScannerChrome();

        binding.torchButton.setOnClickListener(v -> toggleTorch());

        if (allPermissionsGranted()) {
            startCamera();
        } else {
            binding.permissionCard.setVisibility(View.VISIBLE);
            binding.cameraContainer.setVisibility(View.GONE);
            binding.requestPermissionButton.setOnClickListener(v ->
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CODE));
        }
    }

    private void applyScannerChrome() {
        binding.permissionTitle.setText(getString(R.string.common_scanner_permission_title));
        binding.permissionText.setText(getString(R.string.common_scanner_permission_text));
        binding.requestPermissionButton.setText(getString(R.string.common_scanner_allow_access));
        binding.scannerTitle.setText(getString(R.string.scanner_title));
        binding.scannerHint.setText(getString(R.string.scanner_hint));
        binding.torchButton.setText(getString(R.string.common_scanner_torch_on));
    }

    private boolean allPermissionsGranted() {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void toggleTorch() {
        if (camera != null && camera.getCameraInfo().hasFlashUnit()) {
            torchOn = !torchOn;
            camera.getCameraControl().enableTorch(torchOn);
            binding.torchButton.setText(torchOn
                    ? getString(R.string.common_scanner_torch_off)
                    : getString(R.string.common_scanner_torch_on));
        }
    }

    private void startCamera() {
        binding.permissionCard.setVisibility(View.GONE);
        binding.cameraContainer.setVisibility(View.VISIBLE);

        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(requireContext());
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(binding.previewView.getSurfaceProvider());

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                imageAnalysis.setAnalyzer(cameraExecutor, image -> {
                    android.media.Image mediaImage = image.getImage();
                    if (mediaImage != null) {
                        InputImage inputImage = InputImage.fromMediaImage(
                                mediaImage, image.getImageInfo().getRotationDegrees());
                        BarcodeScanning.getClient().process(inputImage)
                                .addOnSuccessListener(barcodes -> handleBarcodes(barcodes))
                                .addOnCompleteListener(task -> image.close());
                    } else {
                        image.close();
                    }
                });

                cameraProvider.unbindAll();
                camera = cameraProvider.bindToLifecycle(
                        this, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageAnalysis);
            } catch (ExecutionException | InterruptedException e) {
                mainHandler.post(() ->
                        Toast.makeText(requireContext(),
                                getString(R.string.common_scanner_error_title),
                                Toast.LENGTH_SHORT).show());
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private void handleBarcodes(java.util.List<Barcode> barcodes) {
        long now = System.currentTimeMillis();
        if (now - lastScanAt < SCAN_COOLDOWN_MS) {
            return;
        }
        for (Barcode barcode : barcodes) {
            String value = barcode.getRawValue();
            if (value != null && !value.isEmpty()) {
                lastScanAt = now;
                mainHandler.post(() -> onBarcodeScanned(value));
                break;
            }
        }
    }

    private void onBarcodeScanned(String value) {
        if (value.startsWith("http://") || value.startsWith("https://")) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(value)));
        } else {
            Toast.makeText(requireContext(), value, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (allPermissionsGranted()) {
                startCamera();
            } else {
                Toast.makeText(requireContext(),
                        getString(R.string.common_scanner_permission_title),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        cameraExecutor.shutdown();
    }
}
