package com.workverse.app.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class DonutChartView extends View {

    private Paint backgroundPaint, foregroundPaint;
    private RectF rectF = new RectF();
    private float progress = 0f;
    private int progressColor = Color.GRAY;

    public DonutChartView(Context context) {
        super(context);
        init();
    }

    public DonutChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        float strokeWidth = dpToPx(6);

        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setStyle(Paint.Style.STROKE);
        backgroundPaint.setColor(Color.parseColor("#E0E0E0"));
        backgroundPaint.setStrokeWidth(strokeWidth);

        foregroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        foregroundPaint.setStyle(Paint.Style.STROKE);
        foregroundPaint.setStrokeCap(Paint.Cap.ROUND);
        foregroundPaint.setStrokeWidth(strokeWidth);
        foregroundPaint.setColor(progressColor);
    }

    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        float stroke = backgroundPaint.getStrokeWidth();
        rectF.set(stroke / 2f, stroke / 2f, w - stroke / 2f, h - stroke / 2f);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawOval(rectF, backgroundPaint);
        float sweepAngle = 360f * (progress / 100f);
        canvas.drawArc(rectF, -90, sweepAngle, false, foregroundPaint);
    }

    public void setProgress(float progress) {
        this.progress = Math.max(0, Math.min(100, progress));
        invalidate();
    }

    public void setProgressColor(int color) {
        this.progressColor = color;
        foregroundPaint.setColor(color);
        invalidate();
    }
}