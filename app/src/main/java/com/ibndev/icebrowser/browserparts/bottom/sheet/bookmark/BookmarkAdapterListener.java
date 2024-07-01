package com.ibndev.icebrowser.browserparts.bottom.sheet.bookmark;

import android.app.Activity;
import android.app.AlertDialog;
import android.database.sqlite.SQLiteDatabase;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

import com.ibndev.icebrowser.R;
import com.ibndev.icebrowser.browserparts.bottom.dialog.ActionBookmarkDialog;
import com.ibndev.icebrowser.browserparts.top.tab.TabManager;

public class BookmarkAdapterListener {
    public Activity activity;
    public BookmarkSheet dialog;
    public BookmarkDatabaseHelper dbHelper;
    public SQLiteDatabase database;
    BookmarkAdapter.ViewHolder holder;
    TabManager tabManager;
    ActionBookmarkDialog actionBookmarkDialog;

    public BookmarkAdapterListener(Activity activity, BookmarkAdapter.ViewHolder holder, TabManager tabManager, BookmarkSheet dialog) {
        this.activity = activity;
        this.holder = holder;
        this.tabManager = tabManager;
        this.dialog = dialog;

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
            if (actionBookmarkDialog == null) {
                actionBookmarkDialog = new ActionBookmarkDialog(this, title, url, id);
                actionBookmarkDialog.show();
            } else {
                actionBookmarkDialog.show();
            }
        });
    }
}
