package com.dynamiqr.android.ui.components;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import com.dynamiqr.android.R;

/** Animated day/night toggle matching RN ThemeToggle. */
public class DayNightToggleView extends View {

    public interface OnCheckedChangeListener {
        void onCheckedChanged(boolean isNight);
    }

    private static final int TRACK_W_DP = 58;
    private static final int TRACK_H_DP = 30;
    private static final int PADDING_DP = 3;
    private static final int THUMB_DP = 24;
    private static final int LIGHT_TRACK = 0xFFFDF0C4;
    private static final int LIGHT_THUMB = 0xFFF5C518;
    private static final int DARK_TRACK = 0xFF042F44;
    private static final int DARK_THUMB = 0xFF3DB8E8;

    private final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint thumbPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final ArgbEvaluator evaluator = new ArgbEvaluator();

    private float progress;
    private boolean isNight;
    private ValueAnimator animator;
    private OnCheckedChangeListener listener;

    private Drawable sunIcon;
    private Drawable moonIcon;

    public DayNightToggleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        sunIcon = ContextCompat.getDrawable(context, R.drawable.ic_sun);
        moonIcon = ContextCompat.getDrawable(context, R.drawable.ic_moon);
        setClickable(true);
        setFocusable(true);
        setContentDescription(context.getString(R.string.settings_display_mode));
    }

    public void setNight(boolean night, boolean animate) {
        if (isNight == night && !animate) {
            progress = night ? 1f : 0f;
            invalidate();
            return;
        }
        isNight = night;
        float target = night ? 1f : 0f;
        if (!animate) {
            progress = target;
            invalidate();
            return;
        }
        if (animator != null) {
            animator.cancel();
        }
        animator = ValueAnimator.ofFloat(progress, target);
        animator.setDuration(320);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addUpdateListener(a -> {
            progress = (float) a.getAnimatedValue();
            invalidate();
        });
        animator.start();
    }

    public boolean isNight() {
        return isNight;
    }

    public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
        this.listener = listener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP && isEnabled()) {
            boolean next = !isNight;
            setNight(next, true);
            if (listener != null) {
                listener.onCheckedChanged(next);
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
        int w = dp(TRACK_W_DP);
        int h = dp(TRACK_H_DP);
        setMeasuredDimension(w, h);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float width = getWidth();
        float height = getHeight();
        float radius = height / 2f;

        int trackColor = (Integer) evaluator.evaluate(progress, LIGHT_TRACK, DARK_TRACK);
        int thumbColor = (Integer) evaluator.evaluate(progress, LIGHT_THUMB, DARK_THUMB);
        trackPaint.setColor(trackColor);
        thumbPaint.setColor(thumbColor);

        canvas.drawRoundRect(0, 0, width, height, radius, radius, trackPaint);

        float pad = dp(PADDING_DP);
        float thumb = dp(THUMB_DP);
        float travel = width - pad * 2f - thumb;
        float thumbLeft = pad + travel * progress;

        canvas.drawCircle(thumbLeft + thumb / 2f, height / 2f, thumb / 2f, thumbPaint);

        float icon = dp(14);
        float iconLeft = thumbLeft + (thumb - icon) / 2f;
        float iconTop = (height - icon) / 2f;

        float sunAlpha = progress <= 0.4f ? 1f - (progress / 0.4f) : 0f;
        float moonAlpha = progress >= 0.6f ? (progress - 0.6f) / 0.4f : 0f;

        if (sunIcon != null && sunAlpha > 0.01f) {
            sunIcon.setAlpha(Math.round(sunAlpha * 255));
            sunIcon.setBounds(Math.round(iconLeft), Math.round(iconTop),
                    Math.round(iconLeft + icon), Math.round(iconTop + icon));
            sunIcon.draw(canvas);
        }
        if (moonIcon != null && moonAlpha > 0.01f) {
            moonIcon.setAlpha(Math.round(moonAlpha * 255));
            moonIcon.setBounds(Math.round(iconLeft), Math.round(iconTop),
                    Math.round(iconLeft + icon), Math.round(iconTop + icon));
            moonIcon.draw(canvas);
        }
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
