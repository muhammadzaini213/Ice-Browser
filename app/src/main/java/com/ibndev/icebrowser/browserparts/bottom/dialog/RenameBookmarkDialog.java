package com.ibndev.icebrowser.browserparts.bottom.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.ibndev.icebrowser.R;
import com.ibndev.icebrowser.browserparts.bottom.sheet.bookmark.BookmarkDatabaseHelper;
import com.ibndev.icebrowser.browserparts.bottom.sheet.bookmark.BookmarkSheet;

public class RenameBookmarkDialog extends Dialog {
    Activity activity;
    String title;
    int id;
    BookmarkSheet dialog;
    BookmarkDatabaseHelper dbHelper;
    SQLiteDatabase database;

    public RenameBookmarkDialog(Activity activity, String title, int id, BookmarkSheet dialog) {
        super(activity);
        this.activity = activity;
        this.title = title;
        this.id = id;
        this.dialog = dialog;

        dbHelper = new BookmarkDatabaseHelper(activity);
        database = dbHelper.getWritableDatabase();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_bookmark_rename_dialog);
        EditText input = findViewById(R.id.main_bookmark_rename_edittext);
        input.setText(title);
        input.requestFocus();
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        assert imm != null;
        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);

        findViewById(R.id.main_bookmark_rename_dialog_rename).setOnClickListener(view -> {
            String inputText = input.getText().toString();
            rename(inputText, id);

        });

        findViewById(R.id.main_bookmark_rename_dialog_cancel).setOnClickListener(view -> {
            dismiss();
            dialog.show();
        });
    }

    private void rename(String inputText, int id) {
        if (inputText == null) return;
        database.execSQL("UPDATE bookmarks SET title=? WHERE id=?",
                new Object[]{inputText, id});

        dismiss();
        dialog.show();
    }

}

