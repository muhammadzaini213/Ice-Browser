package com.ibndev.icebrowser.browserparts.bottom.sheet.bookmark;

import static com.ibndev.icebrowser.browserparts.bottom.sheet.bookmark.BookmarkDatabaseHelper.COLUMN_URL;
import static com.ibndev.icebrowser.browserparts.bottom.sheet.bookmark.BookmarkDatabaseHelper.TABLE_BOOKMARKS;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.ibndev.icebrowser.R;
import com.ibndev.icebrowser.browserparts.top.tab.TabManager;

public class BookmarkSheet {
    Activity activity;
    TabManager tabManager;
    BottomSheetDialog bookmarkSheetDialog;
    View bottomSheetView;

    private static final String TABLE_BOOKMARKS = "bookmarks";
    private static final String COLUMN_URL = "url";

    BookmarkDatabaseHelper dbHelper;
    SQLiteDatabase database;

    public BookmarkSheet(Activity activity, TabManager tabManager, SQLiteDatabase bookmarkDatabase) {
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
        BookmarkAdapter adapter = new BookmarkAdapter(activity, cursor, new BookmarkAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String url) {

            }

            @Override
            public void onItemLongClick(int id, String title, String url) {

            }

        }, tabManager);

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
