package com.ibndev.icebrowser.utilities;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.webkit.CookieManager;
import android.webkit.URLUtil;

import com.ibndev.icebrowser.R;
import com.ibndev.icebrowser.setup.permission.PermissionCodes;
import com.ibndev.icebrowser.setup.permission.StoragePermission;

public class DownloadHelper {

    private final Activity activity;
    private final StoragePermission permissionHelper;

    public DownloadHelper(Activity activity) {
        this.activity = activity;
        this.permissionHelper = new StoragePermission(activity);

    }

    public void startDownload(String url, String filename) {
        if (!permissionHelper.hasOrRequestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                null,
                PermissionCodes.PERMISSION_REQUEST_DOWNLOAD)) {
            return;
        }
        if (filename == null) {
            filename = URLUtil.guessFileName(url, null, null);
        }
        DownloadManager.Request request;
        try {
            request = new DownloadManager.Request(Uri.parse(url));
        } catch (IllegalArgumentException e) {
            new AlertDialog.Builder(activity)
                    .setTitle(activity.getString(R.string.download_fail))
                    .setMessage(url)
                    .setPositiveButton(activity.getString(R.string.ok), (dialog1, which1) -> {
                    })
                    .show();
            return;
        }
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);
        String cookie = CookieManager.getInstance().getCookie(url);
        if (cookie != null) {
            request.addRequestHeader(activity.getString(R.string.cookie), cookie);
        }
        DownloadManager dm = (DownloadManager) activity.getSystemService(Context.DOWNLOAD_SERVICE);
        assert dm != null;
        dm.enqueue(request);
    }
}
