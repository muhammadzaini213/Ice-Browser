package com.ibndev.icebrowser.browserparts.bottom.sheet.settings;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.ibndev.icebrowser.R;
import com.ibndev.icebrowser.browserparts.top.tab.TabManager;

public class SettingsSheet {
    Activity activity;
    TabManager tabManager;
    ClearBrowserData clearBrowserData;
    DeleteAllBookmarks deleteAllBookmarks;

    public SettingsSheet(Activity activity, TabManager tabManager, SQLiteDatabase bookmarkDatabase) {
        this.activity = activity;
        this.tabManager = tabManager;
        clearBrowserData = new ClearBrowserData(tabManager);
        deleteAllBookmarks = new DeleteAllBookmarks(activity, bookmarkDatabase);
    }

    public void show() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(activity);
        @SuppressLint("InflateParams") View sheetView = LayoutInflater.from(activity).inflate(
                R.layout.activity_main_bottomsheet_settings,
                null
        );

        sheetView.findViewById(R.id.main_bottomsheet_settings_close_settings).setOnClickListener(view ->
                bottomSheetDialog.dismiss());

        sheetView.findViewById(R.id.main_bottomsheet_settings_clear_browser_data).setOnClickListener(view -> {
            clearBrowserData.clearHistoryCache();
            bottomSheetDialog.dismiss();
        });

        sheetView.findViewById(R.id.main_bottomsheet_settings_delete_all_bookmarks).setOnClickListener(view -> {
            deleteAllBookmarks.delete();
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.setContentView(sheetView);
        bottomSheetDialog.show();
    }
}
