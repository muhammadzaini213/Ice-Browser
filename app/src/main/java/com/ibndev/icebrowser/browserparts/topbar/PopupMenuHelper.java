package com.ibndev.icebrowser.browserparts.topbar;

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

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class PopupMenuHelper {

    private final Context context;
    private final SQLiteDatabase placesDb;
    private boolean isBookmarked = false;

    public PopupMenuHelper(Context context, SQLiteDatabase placesDb) {
        this.context = context;
        this.placesDb = placesDb;
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
                        Class<?> classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());
                        Method setForceIcons = classPopupHelper.getMethod("setForceShowIcon", boolean.class);
                        setForceIcons.invoke(menuPopupHelper, true);
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        isBookmarked = isUrlInBookmarks(currentUrl);

        if (isBookmarked) {
            popupMenu.getMenu().findItem(R.id.action_save_bookmark).setIcon(R.drawable.bookmark_saved);
            popupMenu.getMenu().findItem(R.id.action_save_bookmark).setTitle("Remove Bookmark");
        } else {
            popupMenu.getMenu().findItem(R.id.action_save_bookmark).setIcon(R.drawable.add_);
            popupMenu.getMenu().findItem(R.id.action_save_bookmark).setTitle("Save Bookmark");
        }

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                return onOptionsItemSelected(item, currentUrl, currentTitle);
            }
        });
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
        if (placesDb == null) return false;
        Cursor cursor = placesDb.rawQuery("SELECT 1 FROM bookmarks WHERE url = ?", new String[]{urlToCheck});

        boolean exists = cursor.moveToFirst();
        cursor.close();

        return exists;
    }

    private void addBookmark(String title, String url) {
        if (placesDb == null) return;
        ContentValues values = new ContentValues(2);
        values.put("title", title);
        values.put("url", url);
        placesDb.insert("bookmarks", null, values);
    }

    private void deleteBookmark(String urlToCheck) {
        if (placesDb == null) return;
        placesDb.execSQL("DELETE FROM bookmarks WHERE url = ?", new Object[]{urlToCheck});
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


