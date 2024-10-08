package com.ibndev.icebrowser.browserparts.utilities;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.webkit.CookieManager;
import android.webkit.URLUtil;

import com.ibndev.icebrowser.browserparts.setup.permission.PermissionCodes;
import com.ibndev.icebrowser.browserparts.setup.permission.RequestPermission;

public class DownloadHelper {

    private final Activity activity;
    private final RequestPermission permissionHelper;

    public DownloadHelper(Activity activity) {
        this.activity = activity;
        this.permissionHelper = new RequestPermission(activity);

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
                    .setTitle("Can't Download URL")
                    .setMessage(url)
                    .setPositiveButton("OK", (dialog1, which1) -> {
                    })
                    .show();
            return;
        }
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);
        String cookie = CookieManager.getInstance().getCookie(url);
        if (cookie != null) {
            request.addRequestHeader("Cookie", cookie);
        }
        DownloadManager dm = (DownloadManager) activity.getSystemService(Context.DOWNLOAD_SERVICE);
        assert dm != null;
        dm.enqueue(request);
    }
}
