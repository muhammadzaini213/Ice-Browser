package com.ibndev.icebrowser.browserparts.settings;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.ibndev.icebrowser.R;
import com.ibndev.icebrowser.browserparts.function.ClearBrowserData;
import com.ibndev.icebrowser.browserparts.function.DeleteAllBookmarks;
import com.ibndev.icebrowser.browserparts.topbar.tab.TabManager;

public class SettingsSheet {
    Activity activity;
    TabManager tabManager;
    ClearBrowserData clearBrowserData;
    DeleteAllBookmarks deleteAllBookmarks;

    public SettingsSheet(Activity activity, TabManager tabManager, SQLiteDatabase placesDB) {
        this.activity = activity;
        this.tabManager = tabManager;
        clearBrowserData = new ClearBrowserData(tabManager);
        deleteAllBookmarks = new DeleteAllBookmarks(activity, placesDB);
    }

    public void show() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(activity);
        @SuppressLint("InflateParams") View sheetView = LayoutInflater.from(activity).inflate(
                R.layout.browser_settings_layout,
                null
        );

        sheetView.findViewById(R.id.close_settings).setOnClickListener(view -> {
            bottomSheetDialog.dismiss();
        });

        sheetView.findViewById(R.id.clear_browser_data).setOnClickListener(view -> {
            clearBrowserData.clearHistoryCache();
            bottomSheetDialog.dismiss();
        });

        sheetView.findViewById(R.id.delete_all_bookmarks).setOnClickListener(view -> {
            deleteAllBookmarks.delete();
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.setContentView(sheetView);
        bottomSheetDialog.show();
    }
}
