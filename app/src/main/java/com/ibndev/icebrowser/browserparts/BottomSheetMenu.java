package com.ibndev.icebrowser.browserparts;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.http.SslCertificate;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.CookieManager;
import android.widget.EditText;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.ibndev.icebrowser.R;

public class BottomSheetMenu {

    public void show(Activity activity, TabManager tabManager, ShowAndHideKeyboard showAndHideKeyboard, SQLiteDatabase placesDB) {
        EditText searchEdit = activity.findViewById(R.id.searchEdit);
        ShowBookmarks showBookmarks = new ShowBookmarks(activity, tabManager, placesDB);

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(activity);
        View bottomSheetView = LayoutInflater.from(activity).inflate(
                R.layout.bottom_sheet_menu,
                null
        );

        bottomSheetView.findViewById(R.id.desktop).setOnClickListener(view -> {
            TabManager.Tab tab = tabManager.getCurrentTab();
            tab.isDesktopUA = !tab.isDesktopUA;
            tabManager.getCurrentWebView().getSettings().setUserAgentString(tab.isDesktopUA ? activity.getString(R.string.desktopUA) : null);
            tabManager.getCurrentWebView().getSettings().setUseWideViewPort(tab.isDesktopUA);
            tabManager.getCurrentWebView().reload();

            bottomSheetDialog.dismiss();
        });

        bottomSheetView.findViewById(R.id.bookmark).setOnClickListener(view -> {
            showBookmarks.showBookmarks();
            bottomSheetDialog.dismiss();
        });

        bottomSheetView.findViewById(R.id.adblock).setOnClickListener(view -> {

            bottomSheetDialog.dismiss();
        });

        bottomSheetView.findViewById(R.id.share).setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
            intent.putExtra(Intent.EXTRA_TEXT, tabManager.getCurrentWebView().getUrl());
            intent.setType("text/plain");
            activity.startActivity(Intent.createChooser(intent, "Share URL"));

            bottomSheetDialog.dismiss();
        });

        bottomSheetView.findViewById(R.id.find).setOnClickListener(view -> {
            searchEdit.setText("");
            activity.findViewById(R.id.searchPane).setVisibility(View.VISIBLE);
            searchEdit.requestFocus();
            showAndHideKeyboard.showKeyboard();

            bottomSheetDialog.dismiss();
        });

        bottomSheetView.findViewById(R.id.cookie).setOnClickListener(view -> {
            if (CookieManager.getInstance().acceptThirdPartyCookies(tabManager.getCurrentWebView())) {
                CookieManager cookieManager = CookieManager.getInstance();
                boolean newValue = !cookieManager.acceptThirdPartyCookies(tabManager.getCurrentWebView());
                cookieManager.setAcceptThirdPartyCookies(tabManager.getCurrentWebView(), newValue);
            }
            bottomSheetDialog.dismiss();
        });

        bottomSheetView.findViewById(R.id.tab_info).setOnClickListener(view -> {
            String s = "URL: " + tabManager.getCurrentWebView().getUrl() + "\n";
            s += "Title: " + tabManager.getCurrentWebView().getTitle() + "\n\n";
            SslCertificate certificate = tabManager.getCurrentWebView().getCertificate();
            s += certificate == null ? "Not secure" : "Certificate:\n" + WebCertificate.certificateToStr(certificate);

            new AlertDialog.Builder(activity)
                    .setTitle("Page info")
                    .setMessage(s)
                    .setPositiveButton("OK", (dialog, which) -> {
                    })
                    .show();

            bottomSheetDialog.dismiss();
        });

        bottomSheetView.findViewById(R.id.exit).setOnClickListener(view -> {
            activity.finishAffinity();
            System.exit(0);
        });

        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }
}
