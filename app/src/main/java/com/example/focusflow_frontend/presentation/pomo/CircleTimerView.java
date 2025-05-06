// CircleTimerView.java
package com.example.focusflow_frontend.presentation.pomo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class CircleTimerView extends View {

    private Paint backgroundPaint;
    private Paint progressPaint;
    private RectF rectF;
    private float progress = 0f; // 0 -> 1

    public CircleTimerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setColor(0xFFE0E0E0); // Light gray
        backgroundPaint.setStyle(Paint.Style.STROKE);
        backgroundPaint.setStrokeWidth(20);

        progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressPaint.setColor(0xFFFF4081); // Pink
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(20);

        rectF = new RectF();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float padding = 40;
        rectF.set(padding, padding, getWidth() - padding, getHeight() - padding);

        canvas.drawOval(rectF, backgroundPaint);
        canvas.drawArc(rectF, -90, progress * 360, false, progressPaint);
    }

    public void setProgress(float progress) {
        this.progress = progress;
        invalidate();
    }

    public float getProgress() {
        return progress;
    }
}
