package com.workverse.app.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class BarChartView extends View {

    private Paint targetPaint, achievedPaint, textPaint;
    private float targetValue = 30f; // Default Bi-monthly target
    private float achievedValue = 0f;

    public BarChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        targetPaint = new Paint();
        targetPaint.setColor(Color.parseColor("#1E88E5")); // Blue bar for Target
        targetPaint.setStyle(Paint.Style.FILL);

        achievedPaint = new Paint();
        achievedPaint.setColor(Color.parseColor("#43A047")); // Green bar for Achieved
        achievedPaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(32f);
        textPaint.setAntiAlias(true);
    }

    public void setChartData(float target, float achieved) {
        this.targetValue = target;
        this.achievedValue = achieved;
        invalidate(); // Redraw view
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float width = getWidth();
        float height = getHeight();
        float maxVal = Math.max(targetValue, achievedValue);
        if (maxVal == 0) maxVal = 1; // Prevent division by zero

        float barWidth = width / 4;
        float padding = 60f;

        // Draw Target Bar
        float targetHeight = (targetValue / maxVal) * (height - 120);
        RectF targetRect = new RectF(padding, height - targetHeight - 60, padding + barWidth, height - 60);
        canvas.drawRect(targetRect, targetPaint);
        canvas.drawText("Target: " + (int)targetValue, padding, height - targetHeight - 80, textPaint);

        // Draw Achieved Bar
        float achievedHeight = (achievedValue / maxVal) * (height - 120);
        RectF achievedRect = new RectF(padding + barWidth + 80, height - achievedHeight - 60, padding + (2 * barWidth) + 80, height - 60);
        canvas.drawRect(achievedRect, achievedPaint);
        canvas.drawText("Achieved: " + (int)achievedValue, padding + barWidth + 80, height - achievedHeight - 80, textPaint);
    }
}