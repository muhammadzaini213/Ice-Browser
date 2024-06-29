package com.ibndev.icebrowser.setup.permission;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.pm.PackageManager;

public class RequestPermission {

    private final Activity activity;

    public RequestPermission(Activity activity) {
        this.activity = activity;
    }

    public boolean hasOrRequestPermission(String permission, String explanation, int requestCode) {
        if (activity.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) {
            // Permission already granted
            return true;
        }
        if (explanation != null && activity.shouldShowRequestPermissionRationale(permission)) {
            new AlertDialog.Builder(activity)
                    .setTitle("Permission Required")
                    .setMessage(explanation)
                    .setPositiveButton("OK", (dialog, which) -> activity.requestPermissions(new String[]{permission}, requestCode))
                    .show();
            return false;
        }
        activity.requestPermissions(new String[]{permission}, requestCode);
        return false;
    }
}

