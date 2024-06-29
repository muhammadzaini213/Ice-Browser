package com.ibndev.icebrowser.browserparts.bottom.sheet.settings;

import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;

import com.ibndev.icebrowser.browserparts.bottom.sheet.bookmark.BookmarkSheet;
import com.ibndev.icebrowser.browserparts.top.tab.TabManager;

public class ShowBookmarks {
    Activity activity;
    TabManager tabManager;
    SQLiteDatabase bookmarkDatabase;

    public ShowBookmarks(Activity activity, TabManager tabManager, SQLiteDatabase bookmarkDatabase) {
        this.activity = activity;
        this.tabManager = tabManager;
        this.bookmarkDatabase = bookmarkDatabase;
    }

    public void showBookmarks() {
        new BookmarkSheet(activity, tabManager, bookmarkDatabase).show();
    }

}
