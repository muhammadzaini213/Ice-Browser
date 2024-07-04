package com.ibndev.icebrowser.floatingparts.utilities;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

public class TouchableWrapper extends FrameLayout {

    private OnTouchListener outsideTouchListener;
    private OnTouchListener insideTouchListener;

    public TouchableWrapper(Context context) {
        super(context);
    }

    public TouchableWrapper(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TouchableWrapper(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setOutsideTouchListener(OnTouchListener listener) {
        this.outsideTouchListener = listener;
    }

    public void setInsideTouchListener(OnTouchListener listener) {
        this.insideTouchListener = listener;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (outsideTouchListener != null && ev.getAction() == MotionEvent.ACTION_OUTSIDE) {
            outsideTouchListener.onTouch(this, ev);
            return true;
        }
        if (insideTouchListener != null) {
            insideTouchListener.onTouch(this, ev);
        }
        return super.dispatchTouchEvent(ev);
    }
}
