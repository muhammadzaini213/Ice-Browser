package com.ibndev.icebrowser;

import static com.ibndev.icebrowser.browserparts.topbar.tab.TabManager.FORM_FILE_CHOOSER;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;

import com.ibndev.icebrowser.browserparts.bottombar.BottomBar;
import com.ibndev.icebrowser.browserparts.setup.FullScreen;
import com.ibndev.icebrowser.browserparts.setup.permission.PermissionCodes;
import com.ibndev.icebrowser.browserparts.topbar.PopupMenuHelper;
import com.ibndev.icebrowser.browserparts.topbar.TopBar;
import com.ibndev.icebrowser.browserparts.topbar.autocomplete.SearchAutoComplete;
import com.ibndev.icebrowser.browserparts.topbar.tab.TabManager;
import com.ibndev.icebrowser.browserparts.topbar.tab.TabRecyclerView;
import com.ibndev.icebrowser.browserparts.topbar.tab.TabsLayout;
import com.ibndev.icebrowser.browserparts.utilities.PlacesDbHelper;

import java.util.ArrayList;
import java.util.List;

public class WebActivity extends Activity {

    private final List<String> tabTitles = new ArrayList<>();
    SearchAutoComplete autoComplete;
    TabRecyclerView recyclerView;
    private SQLiteDatabase placesDb;
    private ValueCallback<Uri[]> fileUploadCallback;
    private boolean fileUploadCallbackShouldReset;
    private TabManager tabManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        placesDb = new PlacesDbHelper(this).getWritableDatabase();

        setContentView(R.layout.activity_main1);


        PopupMenuHelper menuHelper = new PopupMenuHelper(this, placesDb);

        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(visibility -> new FullScreen(getWindow()));


        tabManager = new TabManager(this);
        recyclerView = new TabRecyclerView(this, tabManager, tabTitles);
        autoComplete = new SearchAutoComplete(this, tabManager);


        new TabsLayout(this).tabsLayout(tabManager);
        new BottomBar(this, tabManager, placesDb);
        new TopBar(this, tabManager, recyclerView, tabTitles, menuHelper);

        AutoCompleteTextView et = findViewById(R.id.et);
        tabManager.newTab(et.getText().toString());
        tabManager.getCurrentWebView().setVisibility(View.VISIBLE);
        tabManager.getCurrentWebView().requestFocus();

    }


    @Override
    protected void onDestroy() {
        if (placesDb != null) {
            placesDb.close();
        }
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FORM_FILE_CHOOSER) {
            if (fileUploadCallback != null) {
                if (fileUploadCallbackShouldReset) {
                    fileUploadCallback.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, data));
                    fileUploadCallback = null;
                } else {
                    fileUploadCallbackShouldReset = true;
                }
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PermissionCodes.PERMISSION_REQUEST_DOWNLOAD) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
                new AlertDialog.Builder(this)
                        .setTitle("Permission Denied")
                        .setMessage("The app needs storage permission to download files.")
                        .setPositiveButton("OK", (dialog, which) -> {
                        })
                        .show();
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        String url = autoComplete.getUrlFromIntent(intent);
        if (!url.isEmpty()) {
            tabManager.newTab("google.com");
            tabManager.switchToTab(tabManager.tabs.size() - 1);
        }
    }

    @Override
    public void onBackPressed() {
        if (findViewById(R.id.fullScreenVideo).getVisibility() == View.VISIBLE && tabManager.fullScreenCallback[0] != null) {
            tabManager.fullScreenCallback[0].onCustomViewHidden();
        } else if (tabManager.getCurrentWebView().canGoBack()) {
            tabManager.getCurrentWebView().goBack();
        } else if (tabManager.tabs.size() > 1) {
            tabManager.closeCurrentTab();
        } else {
            super.onBackPressed();
        }
    }

    //TODO: Need it sometime


}
