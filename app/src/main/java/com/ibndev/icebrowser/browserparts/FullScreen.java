package com.ibndev.icebrowser.browserparts;

import android.view.View;
import android.view.Window;

public class FullScreen {
    private boolean isFullscreen;

    public FullScreen(Window window) {
        isFullscreen = false;
        final int flags = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        boolean fullscreenNow = (window.getDecorView().getSystemUiVisibility() & flags) == flags;
        if (fullscreenNow != isFullscreen) {
            window.getDecorView().setSystemUiVisibility(isFullscreen ? flags : 0);
        }
    }
}
