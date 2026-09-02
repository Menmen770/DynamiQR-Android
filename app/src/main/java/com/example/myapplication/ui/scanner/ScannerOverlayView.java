package com.example.myapplication.ui.scanner;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;

public class ScannerOverlayView extends View {
    private Paint maskPaint;
    private Paint framePaint;
    private RectF frameRect;
    private float cornerRadius;

    public ScannerOverlayView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        maskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        maskPaint.setColor(Color.parseColor("#99000000"));

        framePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        framePaint.setColor(Color.WHITE);
        framePaint.setStyle(Paint.Style.STROKE);
        framePaint.setStrokeWidth(8);

        cornerRadius = 40;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        float size = Math.min(w, h) * 0.65f;
        float left = (w - size) / 2;
        float top = (h - size) / 2.5f;
        frameRect = new RectF(left, top, left + size, top + size);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (frameRect == null) return;

        // Draw mask
        int saveCount = canvas.saveLayer(0, 0, getWidth(), getHeight(), null);
        canvas.drawRect(0, 0, getWidth(), getHeight(), maskPaint);
        
        Paint eraser = new Paint(Paint.ANTI_ALIAS_FLAG);
        eraser.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        canvas.drawRoundRect(frameRect, cornerRadius, cornerRadius, eraser);
        canvas.restoreToCount(saveCount);

        // Draw frame
        canvas.drawRoundRect(frameRect, cornerRadius, cornerRadius, framePaint);
    }
}
