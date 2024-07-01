package com.ibndev.icebrowser.browserparts.bottom.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import com.ibndev.icebrowser.R;
import com.ibndev.icebrowser.browserparts.bottom.sheet.bookmark.BookmarkAdapterListener;
import com.ibndev.icebrowser.browserparts.bottom.sheet.bookmark.BookmarkDatabaseHelper;
import com.ibndev.icebrowser.browserparts.bottom.sheet.bookmark.BookmarkSheet;

public class ActionBookmarkDialog extends Dialog {
    private final BookmarkSheet dialog;
    Activity activity;
    String url, title;
    int id;

    DeleteBookmarkDialog deleteDialog;
    RenameBookmarkDialog renameDialog;
    ChangeBookmarkUrlDialog changeUrlDialog;


    public ActionBookmarkDialog(BookmarkAdapterListener listener, String title, String url, int id) {
        super(listener.activity);
        this.activity = listener.activity;
        this.dialog = listener.dialog;
        this.title = title;
        this.url = url;
        this.id = id;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_bookmark_menu_dialog);

        findViewById(R.id.main_bookmark_menu_dialog_rename).setOnClickListener(view -> {
            if(renameDialog == null){
                renameDialog = new RenameBookmarkDialog(activity, title, id, dialog);
            }
            renameDialog.show();
            dismiss();
        });


        findViewById(R.id.main_bookmark_menu_dialog_change_url).setOnClickListener(view -> {
            if(changeUrlDialog == null){
                changeUrlDialog = new ChangeBookmarkUrlDialog(activity, url, id, dialog);
            }
            changeUrlDialog.show();
            dismiss();
        });


        findViewById(R.id.main_bookmark_menu_dialog_delete).setOnClickListener(view -> {
            if (deleteDialog == null) {
                deleteDialog = new DeleteBookmarkDialog(activity, url, dialog);
            }
            deleteDialog.show();
            dismiss();
        });

    }

}

