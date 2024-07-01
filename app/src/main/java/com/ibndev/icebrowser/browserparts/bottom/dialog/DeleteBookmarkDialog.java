package com.ibndev.icebrowser.browserparts.bottom.dialog;

import static com.ibndev.icebrowser.browserparts.bottom.sheet.bookmark.BookmarkDatabaseHelper.COLUMN_URL;
import static com.ibndev.icebrowser.browserparts.bottom.sheet.bookmark.BookmarkDatabaseHelper.TABLE_BOOKMARKS;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import com.ibndev.icebrowser.R;
import com.ibndev.icebrowser.browserparts.bottom.sheet.bookmark.BookmarkDatabaseHelper;
import com.ibndev.icebrowser.browserparts.bottom.sheet.bookmark.BookmarkSheet;

public class DeleteBookmarkDialog extends Dialog {
    Activity activity;
    String url;
    BookmarkSheet dialog;
    BookmarkDatabaseHelper dbHelper;
    SQLiteDatabase database;

    public DeleteBookmarkDialog(Activity activity, String url, BookmarkSheet dialog) {
        super(activity);
        this.activity = activity;
        this.url = url;
        this.dialog = dialog;

        dbHelper = new BookmarkDatabaseHelper(activity);
        database = dbHelper.getWritableDatabase();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_bookmark_delete_confirmation_dialog);

        findViewById(R.id.main_bookmark_delete_confirmation_dialog_yes).setOnClickListener(view -> delete(url));

        findViewById(R.id.main_bookmark_delete_confirmation_dialog_no).setOnClickListener(view -> {
            dismiss();
            dialog.show();
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void delete(String url) {
        if (database == null) return;
        database.delete(TABLE_BOOKMARKS, COLUMN_URL + " = ?", new String[]{url});
        dismiss();
        dialog.show();
    }

}

