package com.ibndev.icebrowser.floatingparts.utilities;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;

public class VolumeButtonReceiver extends AccessibilityService {
    private static final String TAG = "VolumeButtonService";

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // No need to handle accessibility events for this purpose
    }

    @Override
    public void onInterrupt() {
        // Handle service interrupt
    }

    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                Log.d(TAG, "Volume Down Button Pressed");
                // Handle the volume down button press
                return true;
            }
        }
        return super.onKeyEvent(event);
    }
}


