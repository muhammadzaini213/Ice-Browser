package com.ibndev.icebrowser.browserparts.bottom.sheet.menu;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.http.SslCertificate;
import android.view.View;
import android.webkit.CookieManager;
import android.widget.EditText;

import com.ibndev.icebrowser.R;
import com.ibndev.icebrowser.browserparts.bottom.sheet.bookmark.BookmarkSheet;
import com.ibndev.icebrowser.browserparts.top.tab.TabManager;
import com.ibndev.icebrowser.floatingparts.FloatingWindow;
import com.ibndev.icebrowser.setup.permission.OverlayPermission;
import com.ibndev.icebrowser.utilities.ShowAndHideKeyboard;
import com.ibndev.icebrowser.utilities.WebCertificate;

public class MenuSheetFun {
    Activity activity;
    TabManager tabManager;
    BookmarkSheet bookmarkSheet;

    public MenuSheetFun(Activity activity, TabManager tabManager) {
        this.activity = activity;
        this.tabManager = tabManager;
        bookmarkSheet = new BookmarkSheet(activity, tabManager);

    }

    public void switchUA(boolean isDesktopUA) {
        if (!isDesktopUA) {
            tabManager.getCurrentWebView().getSettings().setUserAgentString(activity.getString(R.string.desktopUA));
        } else {
            tabManager.getCurrentWebView().getSettings().setUserAgentString(null);
        }

        tabManager.getCurrentWebView().reload();
    }

    public void showBookmarks() {
        bookmarkSheet.show();
    }

    public void share() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        intent.putExtra(Intent.EXTRA_TEXT, tabManager.getCurrentWebView().getUrl());
        intent.setType("text/plain");
        activity.startActivity(Intent.createChooser(intent, "Share URL"));
    }

    public void find(ShowAndHideKeyboard showAndHideKeyboard) {
        EditText searchEdit = activity.findViewById(R.id.main_top_navbar_search_edit);
        searchEdit.setText("");
        activity.findViewById(R.id.main_top_navbar_search_pane).setVisibility(View.VISIBLE);
        searchEdit.requestFocus();
        showAndHideKeyboard.showKeyboard();
    }

    public void cookie(CookieManager cookieManager, boolean isCookieActive) {
        if (tabManager.getCurrentWebView() != null) {
            cookieManager.setAcceptThirdPartyCookies(tabManager.getCurrentWebView(), !isCookieActive);
        }
    }

    public void overlay(){
        OverlayPermission permission = new OverlayPermission(activity);
        if(!permission.isPermissionGranted()){
            permission.requestOverlayDisplayPermission();
            return;
        }
        Intent intent = new Intent(activity, FloatingWindow.class);
        activity.startService(intent);
    }

    public void tabInfo() {
        String s = activity.getString(R.string.url) + tabManager.getCurrentWebView().getUrl() + "\n";
        s += activity.getString(R.string.title) + tabManager.getCurrentWebView().getTitle() + "\n\n";
        SslCertificate certificate = tabManager.getCurrentWebView().getCertificate();
        s += certificate == null ? "Not secure" : "Certificate:\n" + WebCertificate.certificateToStr(certificate);

        AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
        alertDialog.setTitle(activity.getString(R.string.main_bottomsheet_menu_tab_info_icon_desc));
        alertDialog.setMessage(s);
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, activity.getString(R.string.ok), (dialogInterface, i) -> alertDialog.dismiss());

        alertDialog.show();
    }


}
