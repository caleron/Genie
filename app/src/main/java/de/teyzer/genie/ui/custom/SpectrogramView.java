package de.teyzer.genie.ui.custom;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import java.util.List;
import java.util.Map;

import de.teyzer.genie.util.Util;

public class SpectrogramView extends View {
    private TextPaint mTextPaint;
    private Paint barPaint;
    private int contentWidth;
    private int contentHeight;

    private float scaleFactor = 1f;
    private float scaleOffsetX = 0f;
    private float scaleOffsetY = 0f;

    private float lastTouchX;
    private float lastTouchY;
    private float firstTouchX;
    private float firstTouchY;
    private boolean movedWhileTouch = false;

    ScaleGestureDetector scaleGestureDetector;

    List<Map.Entry<Double, Double>> list;
    private int paddingLeft;
    private int paddingBottom;
    private float drawMinFreq;
    private float drawMaxFreq;
    private float drawMinAmp;
    private float drawFreqRange;
    private float drawMaxDisplayAmp;
    private float drawBottomOffset;

    public SpectrogramView(Context context) {
        super(context);
        init(null, 0);
    }

    public SpectrogramView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public SpectrogramView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Set up a default TextPaint object
        mTextPaint = new TextPaint();
        mTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextAlign(Paint.Align.LEFT);

        barPaint = new Paint();
        barPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        barPaint.setStyle(Paint.Style.FILL);
        barPaint.setColor(Color.BLACK);

        scaleGestureDetector = new ScaleGestureDetector(getContext(), new ScaleListener());

        // Update TextPaint and text measurements from attributes
        mTextPaint.setTextSize(30);
        updateGraphBounds();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingRight = getPaddingRight();
        paddingBottom = getPaddingBottom();

        contentWidth = w - paddingLeft - paddingRight;
        contentHeight = h - paddingTop - paddingBottom;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleGestureDetector.onTouchEvent(event);

        final float x = event.getX();
        final float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                firstTouchX = lastTouchX = x;
                firstTouchY = lastTouchY = y;
                movedWhileTouch = false;
                break;

            case MotionEvent.ACTION_MOVE:
                if (Math.abs(x - firstTouchX) > 10 || Math.abs(y - firstTouchY) > 10) {
                    movedWhileTouch = true;
                }

                if (scaleGestureDetector.isInProgress())
                    return true;

                final float distanceX = (x - lastTouchX) / scaleFactor;
                final float distanceY = (y - lastTouchY) / scaleFactor;

                scaleOffsetX = Util.cap(scaleOffsetX - distanceX, 0, contentWidth - (contentWidth / scaleFactor));
                scaleOffsetY = Util.cap(scaleOffsetY - distanceY, 0, contentHeight - (contentHeight / scaleFactor));

                lastTouchX = x;
                lastTouchY = y;

                updateGraphBounds();
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                if (!movedWhileTouch) {
                    if (x < contentWidth * 0.1f && y > contentHeight * 0.9f) {
                        //wenn ecke unten links berührt, nach ganz links unten scrollen
                        scaleOffsetX = 0;
                        scaleOffsetY = contentHeight - (contentHeight / scaleFactor);
                        updateGraphBounds();
                    }
                }
        }

        return true;
    }

    private void updateGraphBounds() {
        final int MAX_FREQ = 20000;
        final int MIN_FREQ = 0;
        final int FREQ_RANGE = MAX_FREQ - MIN_FREQ;

        final int MAX_AMP = 1000;

        if (scaleFactor > 1) {
            float drawWidth = contentWidth / scaleFactor;
            float drawHeight = contentHeight / scaleFactor;

            //Minimal- und Maximalfrequenz bestimmen
            drawMinFreq = (MIN_FREQ + FREQ_RANGE * (scaleOffsetX / contentWidth));
            drawMaxFreq = (MAX_FREQ - FREQ_RANGE * Util.cap01((contentWidth - scaleOffsetX - drawWidth) / contentWidth));

            //Range anpassen
            drawFreqRange = drawMaxFreq - drawMinFreq;

            //untere Amplitudengrenze bestimmen
            drawMinAmp = MAX_AMP - ((scaleOffsetY + drawHeight) / contentHeight) * MAX_AMP;

            //obere Amplitudengrenze bestimmen
            drawMaxDisplayAmp = drawMinAmp + (MAX_AMP / scaleFactor);

            drawBottomOffset = contentHeight - scaleOffsetY - drawHeight;
        } else {
            drawMinFreq = MIN_FREQ;
            drawMaxFreq = MAX_FREQ;

            drawFreqRange = FREQ_RANGE;

            drawMinAmp = 0;
            drawMaxDisplayAmp = MAX_AMP;

            drawBottomOffset = 0;

        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawText("MinAmp = " + drawMinAmp, 10, 50, mTextPaint);
        canvas.drawText("MaxDisplayAmp = " + drawMaxDisplayAmp, 10, 100, mTextPaint);
        canvas.drawText("MinFreq = " + drawMinFreq, 10, 150, mTextPaint);
        canvas.drawText("MaxFreq = " + drawMaxFreq, 10, 200, mTextPaint);

        if (list != null) {
            int displayAmpCount = 0;

            for (Map.Entry<Double, Double> value : list) {
                double freq = value.getKey();

                if (freq >= drawMinFreq && freq <= drawMaxFreq)
                    displayAmpCount++;
            }

            float barWidth = Math.max(2, (contentWidth / displayAmpCount) / 1.5f);

            for (Map.Entry<Double, Double> value : list) {
                double freq = value.getKey();
                double amp = value.getValue();

                if (freq < drawMinFreq || freq > drawMaxFreq || amp < drawMinAmp)
                    continue;

                float left = (float) ((freq - drawMinFreq) / drawFreqRange) * contentWidth + paddingLeft;
                float right = left + barWidth;

                float barHeight = (float) ((amp / drawMaxDisplayAmp) * contentHeight) - drawBottomOffset;

                float bottom = contentHeight - paddingBottom;
                float top = Util.cap(bottom - barHeight, 0, contentHeight);

                canvas.drawRect(left, top, right, bottom, barPaint);
            }
        }
    }

    public void updateList(List<Map.Entry<Double, Double>> list) {
        this.list = list;
        postInvalidate();
    }


    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scaleFactor *= detector.getScaleFactor();

            //Faktor begrenzen
            scaleFactor = Util.cap(scaleFactor, 1.0f, 20.0f);

            //Zeichenbereich-Offset bestimmen
            //scaleOffsetX = Util.cap(scaleOffsetX + (detector.getFocusX() / scaleFactor) - ((contentWidth / scaleFactor) / 2), 0, contentWidth - (contentWidth / scaleFactor));
            //scaleOffsetY = Util.cap(scaleOffsetY + (detector.getFocusY() / scaleFactor) - ((contentHeight / scaleFactor) / 2), 0, contentWidth - (contentWidth / scaleFactor));

            updateGraphBounds();

            invalidate();
            return true;
        }

    }
}
