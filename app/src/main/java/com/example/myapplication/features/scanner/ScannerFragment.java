package com.example.myapplication.features.scanner;

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
import com.example.myapplication.core.base.BaseFragment;
import com.example.myapplication.core.i18n.AppI18n;
import com.example.myapplication.databinding.FragmentScannerBinding;
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
        binding.permissionTitle.setText(AppI18n.t(requireContext(), "common", "scanner.permissionTitle",
                "Camera permission required"));
        binding.permissionText.setText(AppI18n.t(requireContext(), "common", "scanner.permissionText",
                "To scan QR codes, please allow camera access"));
        binding.requestPermissionButton.setText(AppI18n.t(requireContext(), "common", "scanner.allowAccess",
                "Allow access"));
        binding.scannerTitle.setText(AppI18n.t(requireContext(), "common", "scanner.title", "Scan QR"));
        binding.scannerHint.setText(AppI18n.t(requireContext(), "common", "scanner.hint",
                "Place the QR code inside the frame"));
        binding.torchButton.setText(AppI18n.t(requireContext(), "common", "scanner.torchOn", "Turn flash on"));
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
                    ? AppI18n.t(requireContext(), "common", "scanner.torchOff", "Turn flash off")
                    : AppI18n.t(requireContext(), "common", "scanner.torchOn", "Turn flash on"));
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
                                AppI18n.t(requireContext(), "common", "scanner.errorTitle", "Camera error"),
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
                        AppI18n.t(requireContext(), "common", "scanner.permissionTitle",
                                "Camera permission required"),
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
