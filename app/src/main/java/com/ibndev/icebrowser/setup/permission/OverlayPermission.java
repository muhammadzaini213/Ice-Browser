package com.ibndev.icebrowser.setup.permission;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.widget.Toast;

import com.ibndev.icebrowser.R;
import com.ibndev.icebrowser.floatingparts.FloatingWindow;

public class OverlayPermission {
    Activity activity;

    public OverlayPermission(Activity activity) {
        this.activity = activity;
    }

    /*Requesting overlay permission, .*/
    public void requestOverlayDisplayPermission() {

        AlertDialog alertDialog = new AlertDialog.Builder(activity).create();

        alertDialog.setCancelable(true);
        alertDialog.setTitle(activity.getString(R.string.overlay_permission_required));
        alertDialog.setMessage(activity.getString(R.string.overlay_need));
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, activity.getString(R.string.ok), (dialogInterface, i) -> {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" +
                            activity.getPackageName()));

            activity.startActivity(intent);
            alertDialog.dismiss();
        });

        alertDialog.show();
    }

    public void requestDndPermission() {

        AlertDialog alertDialog = new AlertDialog.Builder(activity).create();

        alertDialog.setCancelable(true);
        alertDialog.setTitle(activity.getString(R.string.dnd_permission_required));
        alertDialog.setMessage(activity.getString(R.string.dnd_need));
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, activity.getString(R.string.yes), (dialogInterface, i) -> {
            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
            activity.startActivity(intent);
        });
        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, activity.getString(R.string.no), ((dialogInterface, i) -> {
            alertDialog.dismiss();
            Toast.makeText(activity, activity.getString(R.string.dnd_cancelled), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(activity, FloatingWindow.class);
            activity.startService(intent);
        }));

        alertDialog.show();

    }


    public boolean isDndPermissionGranted() {
        NotificationManager notificationManager = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
        return notificationManager.isNotificationPolicyAccessGranted();
    }


    public boolean isOverlayPermissionGranted() {
        return Settings.canDrawOverlays(activity);
    }
}
