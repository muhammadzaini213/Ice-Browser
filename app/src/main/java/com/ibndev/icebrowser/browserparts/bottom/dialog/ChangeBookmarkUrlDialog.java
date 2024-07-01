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

public class ChangeBookmarkUrlDialog extends Dialog {
    Activity activity;
    String url;
    int id;
    BookmarkSheet dialog;
    BookmarkDatabaseHelper dbHelper;
    SQLiteDatabase database;

    public ChangeBookmarkUrlDialog(Activity activity, String url, int id, BookmarkSheet dialog) {
        super(activity);
        this.activity = activity;
        this.url = url;
        this.id = id;
        this.dialog = dialog;

        dbHelper = new BookmarkDatabaseHelper(activity);
        database = dbHelper.getWritableDatabase();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_bookmark_changeurl_dialog);

        EditText input = findViewById(R.id.main_bookmark_change_url_edittext);
        input.setText(url);
        input.requestFocus();
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        assert imm != null;
        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);

        findViewById(R.id.main_bookmark_change_url_dialog_change_url).setOnClickListener(view -> {
            String inputText = input.getText().toString();
            changeUrl(inputText, id);

        });

        findViewById(R.id.main_bookmark_change_url_dialog_cancel).setOnClickListener(view -> {
            dismiss();
            dialog.show();
        });
    }

    private void changeUrl(String inputText, int id) {
        if (inputText == null) return;
        database.execSQL("UPDATE bookmarks SET url=? WHERE id=?",
                new Object[]{inputText, id});
        dismiss();
        dialog.show();
    }

}

