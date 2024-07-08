package com.ibndev.icebrowser.setup.permission;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;

import androidx.appcompat.app.AlertDialog;

import com.ibndev.icebrowser.R;

public class OverlayPermission {
    Activity activity;

    public OverlayPermission(Activity activity){
        this.activity = activity;
    }

    /*Requesting overlay permission, .*/
    public void requestOverlayDisplayPermission() {

        AlertDialog alertDialog = new AlertDialog.Builder(activity).create();

        alertDialog.setCancelable(true);
        alertDialog.setTitle(activity.getString(R.string.permission_req));
        alertDialog.setMessage("");
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, activity.getString(R.string.ok), (dialogInterface, i) -> {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" +
                            activity.getPackageName()));

            activity.startActivity(intent);
            alertDialog.dismiss();
        });

        alertDialog.show();
    }

    public boolean isPermissionGranted() {
        return Settings.canDrawOverlays(activity);
    }
}