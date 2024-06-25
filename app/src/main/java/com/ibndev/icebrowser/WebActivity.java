package com.ibndev.icebrowser;

import static com.ibndev.icebrowser.browserparts.TabManager.FORM_FILE_CHOOSER;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;

import com.ibndev.icebrowser.browserparts.BottomBar;
import com.ibndev.icebrowser.browserparts.FullScreen;
import com.ibndev.icebrowser.browserparts.PlacesDbHelper;
import com.ibndev.icebrowser.browserparts.PopupMenuHelper;
import com.ibndev.icebrowser.browserparts.SearchAutoComplete;
import com.ibndev.icebrowser.browserparts.ShowAndHideKeyboard;
import com.ibndev.icebrowser.browserparts.TabManager;
import com.ibndev.icebrowser.browserparts.TabRecyclerView;
import com.ibndev.icebrowser.browserparts.TabsLayout;
import com.ibndev.icebrowser.browserparts.TopBar;

import java.util.ArrayList;
import java.util.List;

public class WebActivity extends Activity {

    private static final String TAG = WebActivity.class.getSimpleName();
    final int PERMISSION_REQUEST_DOWNLOAD = 3;
    private final List<String> tabTitles = new ArrayList<>();
    ShowAndHideKeyboard showAndHideKeyboard;
//    private boolean isFullscreen;
    private SQLiteDatabase placesDb;
    private ValueCallback<Uri[]> fileUploadCallback;
    private boolean fileUploadCallbackShouldReset;
    private TabManager tabManager;




    SearchAutoComplete autoComplete;
    TabRecyclerView recyclerView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            private final Thread.UncaughtExceptionHandler defaultUEH = Thread.getDefaultUncaughtExceptionHandler();

            @Override
            public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
                defaultUEH.uncaughtException(t, e);
            }
        });

        try {
            placesDb = new PlacesDbHelper(this).getWritableDatabase();
        } catch (SQLiteException e) {
            Log.e(TAG, "Can't open database", e);
        }
        setContentView(R.layout.activity_main1);


        showAndHideKeyboard = new ShowAndHideKeyboard(this);
        PopupMenuHelper menuHelper = new PopupMenuHelper(this, placesDb);

        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(visibility -> new FullScreen(getWindow()));


        tabManager = new TabManager(this, showAndHideKeyboard);

        TabsLayout tabsLayout = new TabsLayout(this);
        tabsLayout.tabsLayout(tabManager);

        recyclerView = new TabRecyclerView(this, tabManager, tabTitles);

        new BottomBar(this, tabManager, placesDb);
        new TopBar(this, tabManager, recyclerView, tabTitles, menuHelper);

        autoComplete = new SearchAutoComplete(this, tabManager);


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
        if (requestCode == PERMISSION_REQUEST_DOWNLOAD) {
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
//    private void deleteAllBookmarks() {
//        if (placesDb == null) {
//            new AlertDialog.Builder(this)
//                    .setTitle("Bookmarks error")
//                    .setMessage("Can't open bookmarks database")
//                    .setPositiveButton("OK", (dialog, which) -> {
//                    })
//                    .show();
//            return;
//        }
//        new AlertDialog.Builder(this)
//                .setTitle("Delete all bookmarks?")
//                .setMessage("This action cannot be undone")
//                .setNegativeButton("Cancel", (dialog, which) -> {
//                })
//                .setPositiveButton("Delete All", (dialog, which) -> placesDb.execSQL("DELETE FROM bookmarks"))
//                .show();
//    }
//
//    private void clearHistoryCache() {
//        WebView v = getCurrentWebView();
//        v.clearCache(true);
//        v.clearFormData();
//        v.clearHistory();
//        CookieManager.getInstance().removeAllCookies(null);
//        WebStorage.getInstance().deleteAllData();
//    }
    //TODO: Maybe you need this
//    final MenuAction[] menuActions = new MenuAction[]{
//            new MenuAction("Night mode", R.drawable.night, this::toggleNightMode, () -> isNightMode),
//            new MenuAction("Full screen", R.drawable.fullscreen, this::toggleFullscreen, () -> isFullscreen),
//
//            new MenuAction("Delete all bookmarks", 0, this::deleteAllBookmarks),
//
//            new MenuAction("Clear history and cache", 0, this::clearHistoryCache),
//    };
}
