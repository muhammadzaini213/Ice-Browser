package com.ibndev.icebrowser.browserparts.top;

import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.ibndev.icebrowser.R;
import com.ibndev.icebrowser.browserparts.bottom.sheet.bookmark.BookmarkDatabaseHelper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class TopPopupMenu {

    private final Context context;
    private final SQLiteDatabase bookmarkDatabase;
    private boolean isBookmarked = false;

    BookmarkDatabaseHelper dbHelper;
    SQLiteDatabase database;

    public TopPopupMenu(Context context, SQLiteDatabase bookmarkDatabase) {
        this.context = context;
        this.bookmarkDatabase = bookmarkDatabase;

        dbHelper = new BookmarkDatabaseHelper(context);
        database = dbHelper.getWritableDatabase();
    }

    public void showPopupMenu(View view, String currentUrl, String currentTitle) {
        PopupMenu popupMenu = new PopupMenu(context, view);
        MenuInflater inflater = popupMenu.getMenuInflater();
        inflater.inflate(R.menu.top_bar_menu, popupMenu.getMenu());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            popupMenu.setForceShowIcon(true);
        } else {
            try {
                Field[] fields = popupMenu.getClass().getDeclaredFields();
                for (Field field : fields) {
                    if ("mPopup".equals(field.getName())) {
                        field.setAccessible(true);
                        Object menuPopupHelper = field.get(popupMenu);
                        assert menuPopupHelper != null;
                        Class<?> classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());
                        Method setForceIcons = classPopupHelper.getMethod("setForceShowIcon", boolean.class);
                        setForceIcons.invoke(menuPopupHelper, true);
                        break;
                    }
                }
            } catch (Exception ignored) {
            }
        }

//        isBookmarked = isUrlInBookmarks(currentUrl);
//
//        if (isBookmarked) {
//            popupMenu.getMenu().findItem(R.id.action_save_bookmark).setIcon(R.drawable.top_menu_bookmark_saved_icon);
//            popupMenu.getMenu().findItem(R.id.action_save_bookmark).setTitle("Remove Bookmark");
//        } else {
//            popupMenu.getMenu().findItem(R.id.action_save_bookmark).setIcon(R.drawable.top_menu_bookmark_add_icon);
//            popupMenu.getMenu().findItem(R.id.action_save_bookmark).setTitle("Save Bookmark");
//        }

        popupMenu.setOnMenuItemClickListener(item -> onOptionsItemSelected(item, currentUrl, currentTitle));
        popupMenu.show();
    }

    private boolean onOptionsItemSelected(MenuItem item, String currentUrl, String currentTitle) {
        if (item.getItemId() == R.id.action_open_with) {
            openUrlInApp(currentUrl);
            return true;
        } else if (item.getItemId() == R.id.action_save_bookmark) {
            if (isBookmarked) {
                deleteBookmark(currentUrl);
                Toast.makeText(context, "Bookmark removed", Toast.LENGTH_SHORT).show();
            } else {
                addBookmark(currentTitle, currentUrl);
                Toast.makeText(context, "Bookmark saved", Toast.LENGTH_SHORT).show();
            }
            return true;
        } else {
            return false;
        }
    }

    private boolean isUrlInBookmarks(String urlToCheck) {
        if (bookmarkDatabase == null) return false;
        Cursor cursor = bookmarkDatabase.rawQuery("SELECT 1 FROM bookmarks WHERE url = ?", new String[]{urlToCheck});

        boolean exists = cursor.moveToFirst();
        cursor.close();

        return exists;
    }

    private void addBookmark(String title, String url) {
//        if (bookmarkDatabase == null) return;
//        ContentValues values = new ContentValues(2);
//        values.put("title", title);
//        values.put("url", url);
//        bookmarkDatabase.insert("bookmarks", null, values);


        ContentValues values = new ContentValues();
        values.put(BookmarkDatabaseHelper.COLUMN_TITLE, title);
        values.put(BookmarkDatabaseHelper.COLUMN_URL, url);
        database.insert(BookmarkDatabaseHelper.TABLE_BOOKMARKS, null, values);

    }

    private void deleteBookmark(String urlToCheck) {
        if (bookmarkDatabase == null) return;
        bookmarkDatabase.execSQL("DELETE FROM bookmarks WHERE url = ?", new Object[]{urlToCheck});
    }

    private void openUrlInApp(String url) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        try {
            context.startActivity(i);
        } catch (ActivityNotFoundException e) {
            new AlertDialog.Builder(context)
                    .setTitle("Open in app")
                    .setMessage("No app can open this URL.")
                    .setPositiveButton("OK", (dialog1, which1) -> {
                    })
                    .show();
        }
    }
}


