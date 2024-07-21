package com.ibndev.icebrowser.floatingparts.gallery;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import androidx.appcompat.widget.AppCompatImageView;

public class CustomImageView extends AppCompatImageView {

    private final Matrix matrix = new Matrix();
    private final float[] matrixValues = new float[9];
    private float scale = 1f;
    private final float minScale = 1f;
    private final float maxScale = 5f;
    private float lastX, lastY;
    private final ScaleGestureDetector scaleGestureDetector;
    private final GestureDetector gestureDetector;

    public CustomImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleListener());
        gestureDetector = new GestureDetector(context, new GestureListener());
        setScaleType(ScaleType.MATRIX);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleGestureDetector.onTouchEvent(event);
        gestureDetector.onTouchEvent(event);
        return true;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scale *= detector.getScaleFactor();
            scale = Math.max(minScale, Math.min(scale, maxScale));

            matrix.setScale(scale, scale, detector.getFocusX(), detector.getFocusY());
            setImageMatrix(matrix);
            return true;
        }
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            matrix.postTranslate(-distanceX, -distanceY);
            setImageMatrix(matrix);
            return true;
        }
    }
}