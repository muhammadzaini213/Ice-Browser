package com.ibndev.icebrowser.browserparts.bottom.sheet.bookmark;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.database.sqlite.SQLiteDatabase;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

import com.ibndev.icebrowser.R;
import com.ibndev.icebrowser.browserparts.top.tab.TabManager;

public class BookmarkAdapterListener {
    Activity activity;
    BookmarkAdapter.ViewHolder holder;

    private static final String TABLE_BOOKMARKS = "bookmarks";
    private static final String COLUMN_URL = "url";

    BookmarkDatabaseHelper dbHelper;
    SQLiteDatabase database;

    TabManager tabManager;

    public BookmarkAdapterListener(Activity activity, BookmarkAdapter.ViewHolder holder, TabManager tabManager) {
        this.activity = activity;
        this.holder = holder;
        this.tabManager = tabManager;

        dbHelper = new BookmarkDatabaseHelper(activity);
        database = dbHelper.getWritableDatabase();
    }

    public void onClick(String url) {
        holder.layout.setOnClickListener(view -> {
            AutoCompleteTextView et = activity.findViewById(R.id.main_top_navbar_autocomplete);
            et.setText(url);
            tabManager.loadUrl(url, tabManager.getCurrentWebView());
        });
    }

    public void openMenu(String title, String url, int id) {
        holder.menu.setOnClickListener(view -> {
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
                                                        database.execSQL("UPDATE bookmarks SET title=? WHERE id=?",
                                                                new Object[]{editView.getText(), id}))
                                                .setNegativeButton(activity.getString(R.string.cancel), null)
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
                                                        database.execSQL("UPDATE bookmarks SET url=? WHERE id=?",
                                                                new Object[]{editView.getText(), id}))
                                                .setNegativeButton(activity.getString(R.string.cancel), null)
                                                .show();
                                        break;
                                    }
                                    case 2:
                                        deleteBookmark(url);
                                        break;
                                }
                            })
                    .show();
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void deleteBookmark(String urlToCheck) {
        if (database == null) return;
        database.delete(TABLE_BOOKMARKS, COLUMN_URL + " = ?", new String[]{urlToCheck});
    }
}
