package com.ibndev.icebrowser.scratch;

import android.content.Context;
import android.view.MotionEvent;

public class GestureDetector extends android.view.GestureDetector.SimpleOnGestureListener {
    private static final double FORBIDDEN_ZONE_MIN = Math.PI / 4 - Math.PI / 12;
    private static final double FORBIDDEN_ZONE_MAX = Math.PI / 4 + Math.PI / 12;
    private static final int MIN_VELOCITY_DP = 80;  // 0.5 inch/sec
    private static final int MIN_DISTANCE_DP = 80;  // 0.5 inch
    private final float MIN_VELOCITY_PX;
    private final float MIN_DISTANCE_PX;

    GestureDetector(Context context) {
        float density = context.getResources().getDisplayMetrics().density;
        MIN_VELOCITY_PX = MIN_VELOCITY_DP * density;
        MIN_DISTANCE_PX = MIN_DISTANCE_DP * density;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        float velocitySquared = velocityX * velocityX + velocityY * velocityY;
        if (velocitySquared < MIN_VELOCITY_PX * MIN_VELOCITY_PX) {
            // too slow
            return false;
        }

        float deltaX = e2.getX() - e1.getX();
        float deltaY = e2.getY() - e1.getY();

        if (Math.abs(deltaX) < MIN_DISTANCE_PX && Math.abs(deltaY) < MIN_DISTANCE_PX) {
            // small movement
            return false;
        }

        double angle = Math.atan2(Math.abs(deltaY), Math.abs(deltaX));
        if (angle > FORBIDDEN_ZONE_MIN && angle < FORBIDDEN_ZONE_MAX) {
            return false;
        }

        if (Math.abs(deltaX) > Math.abs(deltaY)) {
            if (deltaX > 0) {
                return onFlingRight();
            } else {
                return onFlingLeft();
            }
        } else {
            if (deltaY > 0) {
                return onFlingDown();
            } else {
                return onFlingUp();
            }
        }
    }

    boolean onFlingRight() {
        return true;
    }

    boolean onFlingLeft() {
        return true;
    }

    boolean onFlingUp() {
        return true;
    }

    boolean onFlingDown() {
        return true;
    }
}
