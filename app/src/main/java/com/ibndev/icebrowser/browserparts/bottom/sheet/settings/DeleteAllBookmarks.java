package com.ibndev.icebrowser.browserparts.bottom.sheet.settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.database.sqlite.SQLiteDatabase;

import com.ibndev.icebrowser.R;
import com.ibndev.icebrowser.browserparts.bottom.sheet.bookmark.BookmarkDatabaseHelper;

public class DeleteAllBookmarks {
    Activity activity;
    BookmarkDatabaseHelper dbHelper;
    SQLiteDatabase database;

    public DeleteAllBookmarks(Activity activity) {
        this.activity = activity;
        dbHelper = new BookmarkDatabaseHelper(activity);
        database = dbHelper.getWritableDatabase();
    }

    public void delete() {
        if (database == null) {
            new AlertDialog.Builder(activity)
                    .setTitle(activity.getString(R.string.bookmarks_error))
                    .setMessage(activity.getString(R.string.cant_open_bookmark_database))
                    .setPositiveButton(activity.getString(R.string.ok), (dialog, which) -> {
                    })
                    .show();
            return;
        }
        new AlertDialog.Builder(activity)
                .setTitle(activity.getString(R.string.delete_all_bookmark_confirmation))
                .setMessage(activity.getString(R.string.delete_all_bookmark_confirmation_desc))
                .setNegativeButton(activity.getString(R.string.cancel), (dialog, which) -> {
                })
                .setPositiveButton(activity.getString(R.string.delete_all), (dialog, which) -> database.execSQL("DELETE FROM bookmarks"))
                .show();
    }
}
