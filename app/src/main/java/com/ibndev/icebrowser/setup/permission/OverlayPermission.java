package com.ibndev.icebrowser.setup.permission;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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

    public boolean isOverlayPermissionGranted() {
        return Settings.canDrawOverlays(activity);
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

    public void requestStoragePermission() {

        AlertDialog alertDialog = new AlertDialog.Builder(activity).create();

        alertDialog.setCancelable(true);
        alertDialog.setTitle(activity.getString(R.string.storage_permission_required));
        alertDialog.setMessage(activity.getString(R.string.storage_need));
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, activity.getString(R.string.ok), (dialogInterface, i) -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ActivityCompat.requestPermissions(activity, new String[]{
                        Manifest.permission.READ_MEDIA_IMAGES,
                        Manifest.permission.READ_MEDIA_VIDEO
                }, 1);
            } else {
                ActivityCompat.requestPermissions(activity, new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE
                }, 1);

                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
                intent.setData(uri);
                activity.startActivity(intent);

            }
            alertDialog.dismiss();
        });

        alertDialog.show();
    }

    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

}
