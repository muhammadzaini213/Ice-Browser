package com.ibndev.icebrowser.browserparts.function;

import android.app.Activity;
import android.app.AlertDialog;
import android.database.sqlite.SQLiteDatabase;

public class DeleteAllBookmarks {

    public void delete(Activity activity, SQLiteDatabase placesDb) {
        if (placesDb == null) {
            new AlertDialog.Builder(activity)
                    .setTitle("Bookmarks error")
                    .setMessage("Can't open bookmarks database")
                    .setPositiveButton("OK", (dialog, which) -> {
                    })
                    .show();
            return;
        }
        new AlertDialog.Builder(activity)
                .setTitle("Delete all bookmarks?")
                .setMessage("This action cannot be undone")
                .setNegativeButton("Cancel", (dialog, which) -> {
                })
                .setPositiveButton("Delete All", (dialog, which) -> placesDb.execSQL("DELETE FROM bookmarks"))
                .show();
    }
}
