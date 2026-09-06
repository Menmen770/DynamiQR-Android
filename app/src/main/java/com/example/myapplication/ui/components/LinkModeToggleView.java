package com.example.myapplication.ui.components;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import com.example.myapplication.R;
import com.example.myapplication.core.i18n.AppI18n;

/**
 * בורר סטטי/דינמי עם פס מחליק — כמו QrLinkModeToggle באתר / RN.
 */
public class LinkModeToggleView extends View {

    public interface OnModeChangeListener {
        void onModeChanged(boolean dynamic);
    }

    private static final int TRACK_H_DP = 44;
    private static final float PAD_DP = 4f;

    private final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint sliderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF trackRect = new RectF();
    private final RectF sliderRect = new RectF();

    private boolean dynamic;
    private float progress;
    private ValueAnimator animator;
    private OnModeChangeListener listener;

    private int trackColor;
    private int trackBorder;
    private int sliderColor;
    private int activeText;
    private int inactiveText;

    public LinkModeToggleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setClickable(true);
        setFocusable(true);
        resolveColors();
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setFakeBoldText(true);
        textPaint.setTextSize(dp(14));
        sliderPaint.setShadowLayer(dp(6), 0, dp(2), 0x330A9396);
        setLayerType(LAYER_TYPE_SOFTWARE, null);
    }

    private void resolveColors() {
        trackColor = ContextCompat.getColor(getContext(), R.color.primary_overlay_12);
        trackBorder = ContextCompat.getColor(getContext(), R.color.primary_overlay_22);
        sliderColor = ContextCompat.getColor(getContext(), R.color.surface);
        activeText = ContextCompat.getColor(getContext(), R.color.primary);
        inactiveText = ContextCompat.getColor(getContext(), R.color.sub_text);
        sliderPaint.setColor(sliderColor);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        resolveColors();
    }

    public void setDynamic(boolean dynamic, boolean animate) {
        this.dynamic = dynamic;
        float target = dynamic ? 1f : 0f;
        if (!animate) {
            progress = target;
            invalidate();
            return;
        }
        if (animator != null) {
            animator.cancel();
        }
        animator = ValueAnimator.ofFloat(progress, target);
        animator.setDuration(380);
        animator.setInterpolator(new OvershootInterpolator(1.15f));
        animator.addUpdateListener(a -> {
            progress = (float) a.getAnimatedValue();
            invalidate();
        });
        animator.start();
    }

    public boolean isDynamic() {
        return dynamic;
    }

    public void setOnModeChangeListener(OnModeChangeListener listener) {
        this.listener = listener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP && isEnabled()) {
            float mid = getWidth() / 2f;
            boolean isRtl = getLayoutDirection() == LAYOUT_DIRECTION_RTL;
            boolean nextDynamic = isRtl ? event.getX() < mid : event.getX() >= mid;
            if (nextDynamic != dynamic) {
                setDynamic(nextDynamic, true);
                if (listener != null) {
                    listener.onModeChanged(nextDynamic);
                }
            }
            performClick();
            return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.UNSPECIFIED) {
            width = dp(220);
        }
        setMeasuredDimension(width, dp(TRACK_H_DP));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float w = getWidth();
        float h = getHeight();
        float radius = h / 2f;
        float pad = dp(PAD_DP);

        trackRect.set(0, 0, w, h);
        trackPaint.setStyle(Paint.Style.FILL);
        trackPaint.setColor(trackColor);
        canvas.drawRoundRect(trackRect, radius, radius, trackPaint);
        trackPaint.setStyle(Paint.Style.STROKE);
        trackPaint.setStrokeWidth(dp(1));
        trackPaint.setColor(trackBorder);
        canvas.drawRoundRect(trackRect, radius, radius, trackPaint);

        boolean isRtl = getLayoutDirection() == LAYOUT_DIRECTION_RTL;
        float half = (w - pad * 2f) / 2f;
        // progress 0 = static, 1 = dynamic
        // LTR: static left, dynamic right | RTL: static right, dynamic left
        float sliderLeft = isRtl
                ? pad + (1f - progress) * half
                : pad + progress * half;
        sliderRect.set(sliderLeft, pad, sliderLeft + half, h - pad);
        float sliderRadius = (h - pad * 2f) / 2f;
        sliderPaint.setColor(sliderColor);
        canvas.drawRoundRect(sliderRect, sliderRadius, sliderRadius, sliderPaint);

        float baseline = h / 2f - (textPaint.descent() + textPaint.ascent()) / 2f;
        float staticCx = isRtl ? pad + half + half / 2f : pad + half / 2f;
        float dynamicCx = isRtl ? pad + half / 2f : pad + half + half / 2f;

        textPaint.setColor(dynamic ? inactiveText : activeText);
        canvas.drawText(staticLabel(), staticCx, baseline, textPaint);
        textPaint.setColor(dynamic ? activeText : inactiveText);
        canvas.drawText(dynamicLabel(), dynamicCx, baseline, textPaint);
    }

    private String staticLabel() {
        return AppI18n.t(getContext(), "generator", "linkMode.static",
                getResources().getString(R.string.link_mode_static));
    }

    private String dynamicLabel() {
        return AppI18n.t(getContext(), "generator", "linkMode.dynamic",
                getResources().getString(R.string.link_mode_dynamic));
    }

    private int dp(float value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
