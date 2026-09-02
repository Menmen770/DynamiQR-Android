package com.example.myapplication.ui.custom;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;
import com.example.myapplication.data.models.QrOptions;

public class QrRendererView extends View {
    private QrOptions options;
    private Paint paint;
    private Paint bgPaint;
    private boolean[][] matrix;

    public QrRendererView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        options = new QrOptions();
    }

    public void setOptions(QrOptions options) {
        this.options = options;
        this.matrix = generateMatrix(options.getContent());
        invalidate();
    }

    private boolean[][] generateMatrix(String content) {
        int size = 25;
        boolean[][] m = new boolean[size][size];
        int seed = content != null ? content.hashCode() : 0;
        java.util.Random random = new java.util.Random(seed);
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if ((i < 7 && j < 7) || (i < 7 && j > size - 8) || (i > size - 8 && j < 7)) {
                    m[i][j] = (i == 0 || i == 6 || j == 0 || j == 6 || (i >= 2 && i <= 4 && j >= 2 && j <= 4));
                } else {
                    m[i][j] = random.nextBoolean();
                }
            }
        }
        return m;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (matrix == null) return;
        int size = matrix.length;
        float cellSize = (float) getWidth() / size;
        bgPaint.setColor(Color.parseColor(options.getBgColor()));
        canvas.drawRect(0, 0, getWidth(), getHeight(), bgPaint);
        if (options.getQrColorMode().equals("gradient")) {
            String[] colors = options.getGradientColors();
            Shader shader = new LinearGradient(0, 0, getWidth(), getHeight(),
                    Color.parseColor(colors[0]), Color.parseColor(colors[1]), Shader.TileMode.CLAMP);
            paint.setShader(shader);
        } else {
            paint.setShader(null);
            paint.setColor(Color.parseColor(options.getFgColor()));
        }
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                if (matrix[row][col]) {
                    drawModule(canvas, row, col, cellSize, size);
                }
            }
        }
    }

    private void drawModule(Canvas canvas, int row, int col, float cellSize, int matrixSize) {
        float left = col * cellSize;
        float top = row * cellSize;
        RectF rect = new RectF(left + cellSize * 0.05f, top + cellSize * 0.05f, left + cellSize * 0.95f, top + cellSize * 0.95f);
        boolean isEye = (row < 7 && col < 7) || (row < 7 && col > matrixSize - 8) || (row > matrixSize - 8 && col < 7);
        if (isEye) {
            drawEye(canvas, rect, cellSize);
        } else {
            drawBody(canvas, left, top, cellSize, rect);
        }
    }

    private void drawBody(Canvas canvas, float left, float top, float cellSize, RectF rect) {
        switch (options.getBodyShape()) {
            case DOTS:
                canvas.drawCircle(left + cellSize / 2, top + cellSize / 2, cellSize / 2 * 0.8f, paint);
                break;
            case ROUNDED:
                canvas.drawRoundRect(rect, cellSize * 0.4f, cellSize * 0.4f, paint);
                break;
            case EXTRA_ROUNDED:
                canvas.drawCircle(left + cellSize / 2, top + cellSize / 2, cellSize / 2 * 0.95f, paint);
                break;
            case CLASSY:
                Path path = new Path();
                path.moveTo(left + cellSize / 2, top + cellSize * 0.1f);
                path.lineTo(left + cellSize * 0.9f, top + cellSize / 2);
                path.lineTo(left + cellSize / 2, top + cellSize * 0.9f);
                path.lineTo(left + cellSize * 0.1f, top + cellSize / 2);
                path.close();
                canvas.drawPath(path, paint);
                break;
            default:
                canvas.drawRect(rect, paint);
                break;
        }
    }

    private void drawEye(Canvas canvas, RectF rect, float cellSize) {
        // More professional eye drawing
        if (options.getEyeShape() == QrOptions.EyeShape.ROUNDED) {
            canvas.drawRoundRect(rect, cellSize * 0.3f, cellSize * 0.3f, paint);
        } else if (options.getEyeShape() == QrOptions.EyeShape.DOT) {
            canvas.drawCircle(rect.centerX(), rect.centerY(), cellSize / 2 * 0.9f, paint);
        } else {
            canvas.drawRect(rect, paint);
        }
    }
}
