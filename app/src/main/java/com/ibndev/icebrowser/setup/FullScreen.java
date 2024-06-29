package com.ibndev.icebrowser.setup;

import android.view.View;
import android.view.Window;

public class FullScreen {

    public FullScreen(Window window) {
        boolean isFullscreen = false;
        final int flags = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        boolean fullscreenNow = (window.getDecorView().getSystemUiVisibility() & flags) == flags;
        if (fullscreenNow != isFullscreen) {
            window.getDecorView().setSystemUiVisibility(0);
        }
    }
}
