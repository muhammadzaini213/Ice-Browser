package com.ibndev.icebrowser.browserparts.function;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

import com.ibndev.icebrowser.R;
import com.ibndev.icebrowser.browserparts.topbar.tab.TabManager;

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
        if (bookmarkDatabase == null) return;
        Cursor cursor = bookmarkDatabase.rawQuery("SELECT title, url, id as _id FROM bookmarks", null);
        AutoCompleteTextView et = activity.findViewById(R.id.main_top_navbar_autocomplete);
        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setTitle(activity.getString(R.string.bookmarks))
                .setOnDismissListener(dlg -> cursor.close())
                .setCursor(cursor, (dlg, which) -> {
                    cursor.moveToPosition(which);
                    @SuppressLint("Range") String url = cursor.getString(cursor.getColumnIndex("url"));
                    et.setText(url);
                    tabManager.loadUrl(url, tabManager.getCurrentWebView());
                }, "title")
                .create();
        dialog.getListView().setOnItemLongClickListener((parent, view, position, id) -> {
            cursor.moveToPosition(position);
            @SuppressLint("Range") int rowId = cursor.getInt(cursor.getColumnIndex("_id"));
            @SuppressLint("Range") String title = cursor.getString(cursor.getColumnIndex("title"));
            @SuppressLint("Range") String url = cursor.getString(cursor.getColumnIndex("url"));
            dialog.dismiss();
            new AlertDialog.Builder(activity)
                    .setTitle(title)
                    .setItems(new String[]{
                                    activity.getString(R.string.rename),
                                    activity.getString(R.string.change_url),
                                    activity.getString(R.string.delete)},
                            (dlg, which) -> {
                                switch (which) {
                                    case 0: {
                                        EditText editView = new EditText(activity);
                                        editView.setText(title);
                                        new AlertDialog.Builder(activity)
                                                .setTitle(activity.getString(R.string.rename_bookmark))
                                                .setView(editView)
                                                .setPositiveButton(activity.getString(R.string.rename), (renameDlg, which1) ->
                                                        bookmarkDatabase.execSQL("UPDATE bookmarks SET title=? WHERE id=?",
                                                                new Object[]{editView.getText(), rowId}))
                                                .setNegativeButton(activity.getString(R.string.cancel), (renameDlg, which1) -> {
                                                })
                                                .show();
                                        break;
                                    }
                                    case 1: {
                                        EditText editView = new EditText(activity);
                                        editView.setText(url);
                                        new AlertDialog.Builder(activity)
                                                .setTitle(activity.getString(R.string.change_bookmark_url))
                                                .setView(editView)
                                                .setPositiveButton(activity.getString(R.string.change_url), (renameDlg, which1) ->
                                                        bookmarkDatabase.execSQL("UPDATE bookmarks SET url=? WHERE id=?",
                                                                new Object[]{editView.getText(), rowId}))
                                                .setNegativeButton(activity.getString(R.string.cancel), (renameDlg, which1) -> {
                                                })
                                                .show();
                                        break;
                                    }
                                    case 2:
                                        bookmarkDatabase.execSQL("DELETE FROM bookmarks WHERE id = ?",
                                                new Object[]{rowId});
                                        break;
                                }
                            })
                    .show();
            return true;
        });
        dialog.show();
    }

}
