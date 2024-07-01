package com.ibndev.icebrowser.browserparts.bottom.sheet.bookmark;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.ibndev.icebrowser.R;
import com.ibndev.icebrowser.browserparts.top.tab.TabManager;

public class BookmarkSheet {
    private static final String TABLE_BOOKMARKS = "bookmarks";
    Activity activity;
    TabManager tabManager;
    BottomSheetDialog bookmarkSheetDialog;
    View bottomSheetView;
    BookmarkAdapter adapter;
    BookmarkDatabaseHelper dbHelper;
    SQLiteDatabase database;

    @SuppressLint("InflateParams")
    public BookmarkSheet(Activity activity, TabManager tabManager) {
        this.activity = activity;
        this.tabManager = tabManager;

        bookmarkSheetDialog = new BottomSheetDialog(activity);
        bottomSheetView = LayoutInflater.from(activity)
                .inflate(R.layout.activity_main_bottomsheet_bookmark, null);
        bookmarkSheetDialog.setContentView(bottomSheetView);

        dbHelper = new BookmarkDatabaseHelper(activity);
        database = dbHelper.getWritableDatabase();
    }

    public void show() {
        bottomSheetView.findViewById(R.id.main_bottomsheet_bookmark_close).setOnClickListener(view -> bookmarkSheetDialog.dismiss());
        RecyclerView recyclerView = bottomSheetView.findViewById(R.id.bookmark_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));

        if (database == null) return;
        Cursor cursor = database.rawQuery("SELECT title, url, id as _id FROM bookmarks", null);
        adapter = new BookmarkAdapter(activity, cursor, tabManager, this);

        recyclerView.setAdapter(adapter);

        if (isDatabaseEmpty()) {
            recyclerView.setVisibility(View.GONE);
            bottomSheetView.findViewById(R.id.main_bottomsheet_bookmark_empty_text).setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            bottomSheetView.findViewById(R.id.main_bottomsheet_bookmark_empty_text).setVisibility(View.GONE);
        }

        bookmarkSheetDialog.show();

    }

    private boolean isDatabaseEmpty() {
        if (database == null) return true;
        String query = "SELECT COUNT(*) FROM " + TABLE_BOOKMARKS;
        Cursor cursor = database.rawQuery(query, null);

        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();

        return count == 0;
    }

}
