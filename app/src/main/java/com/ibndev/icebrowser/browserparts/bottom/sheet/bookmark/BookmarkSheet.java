package com.ibndev.icebrowser.browserparts.bottom.sheet.bookmark;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class BookmarkSheet {
    Activity activity;
    TabManager tabManager;
    BottomSheetDialog bookmarkSheetDialog;
    View bottomSheetView;

    private static final String TABLE_BOOKMARKS = "bookmarks";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_URL = "url";
    private static final String COLUMN_FAVICON = "favicon";

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
        WebView webView = tabManager.getCurrentWebView();

        RecyclerView recyclerView = bottomSheetView.findViewById(R.id.bookmark_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));


        AutoCompleteTextView et = activity.findViewById(R.id.main_top_navbar_autocomplete);
        if (database == null) return;
        Cursor cursor = database.rawQuery("SELECT title, url, id as _id FROM bookmarks", null);
        BookmarkAdapter adapter = new BookmarkAdapter(cursor, new BookmarkAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String url) {
                et.setText(url);
                tabManager.loadUrl(url, tabManager.getCurrentWebView());
            }

            @Override
            public void onItemLongClick(int id, String title, String url) {
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
                                            database.execSQL("DELETE FROM bookmarks WHERE id = ?", new Object[]{id});
                                            cursor.requery(); // refresh cursor
//                                            adapter.notifyDataSetChanged(); // notify adapter
                                            break;
                                    }
                                })
                        .show();
            }
        });

        recyclerView.setAdapter(adapter);

        bookmarkSheetDialog.show();
    }

    private void createBookmarksTableIfNeeded() {
        String createTableQuery = "CREATE TABLE IF NOT EXISTS " + TABLE_BOOKMARKS + " (" +
                COLUMN_TITLE + " TEXT, " +
                COLUMN_URL + " TEXT, " +
                COLUMN_FAVICON + " BLOB" +
                ");";
        database.execSQL(createTableQuery);
    }

    Cursor cursor;
    private List<BookmarkData> getAllBookmarks() {
        List<BookmarkData> bookmarks = new ArrayList<>();

        try {
            if (database != null && database.isOpen()) {
                cursor = database.query(TABLE_BOOKMARKS,
                        new String[]{COLUMN_TITLE, COLUMN_URL, COLUMN_FAVICON},
                        null, null, null, null, null);

                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        String title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE));
                        String url = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_URL));
                        byte[] faviconBlob = cursor.getBlob(cursor.getColumnIndexOrThrow(COLUMN_FAVICON));
                        Bitmap favicon = BitmapFactory.decodeByteArray(faviconBlob, 0, faviconBlob.length);

                        bookmarks.add(new BookmarkData(title, url, favicon));
                    } while (cursor.moveToNext());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return bookmarks;
    }

    private void insertBookmarkIntoDatabase(BookmarkData bookmarkData) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, bookmarkData.getTitle());
        values.put(COLUMN_URL, bookmarkData.getUrl());
        values.put(COLUMN_FAVICON, bitmapToByteArray(bookmarkData.getFavicon()));

        database.insert(TABLE_BOOKMARKS, null, values);
    }

    private byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }
}
