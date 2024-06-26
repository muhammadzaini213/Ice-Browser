package com.ibndev.icebrowser.browserparts.function;

import android.app.Activity;
import android.app.AlertDialog;
import android.database.sqlite.SQLiteDatabase;

import com.ibndev.icebrowser.R;

public class DeleteAllBookmarks {
    Activity activity;
    SQLiteDatabase bookmarkDatabase;

    public DeleteAllBookmarks(Activity activity, SQLiteDatabase bookmarkDatabase) {
        this.activity = activity;
        this.bookmarkDatabase = bookmarkDatabase;
    }

    public void delete() {
        if (bookmarkDatabase == null) {
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
                .setPositiveButton(activity.getString(R.string.delete_all), (dialog, which) -> bookmarkDatabase.execSQL("DELETE FROM bookmarks"))
                .show();
    }
}
