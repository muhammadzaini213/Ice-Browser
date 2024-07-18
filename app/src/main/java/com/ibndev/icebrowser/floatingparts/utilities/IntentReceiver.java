package com.ibndev.icebrowser.floatingparts.utilities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ibndev.icebrowser.floatingparts.FloatingWindow;

public class IntentReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent serviceIntent = new Intent(context, FloatingWindow.class);
        context.stopService(serviceIntent);
        OverlayManager.setOverlayVisibility(false);
    }
}
