package com.ibndev.icebrowser.floatingparts.utilities;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class OverlayManager {

    private static final MutableLiveData<Boolean> overlayVisible = new MutableLiveData<>(true);

    public static LiveData<Boolean> getOverlayVisibility() {
        return overlayVisible;
    }

    public static void setOverlayVisibility(boolean visible) {
        overlayVisible.setValue(visible);
    }
}

