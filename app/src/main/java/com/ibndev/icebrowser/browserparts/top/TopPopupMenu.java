package com.ibndev.icebrowser.browserparts.top;

import static com.ibndev.icebrowser.browserparts.bottom.sheet.bookmark.BookmarkDatabaseHelper.COLUMN_URL;
import static com.ibndev.icebrowser.browserparts.bottom.sheet.bookmark.BookmarkDatabaseHelper.TABLE_BOOKMARKS;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ibndev.icebrowser.R;
import com.ibndev.icebrowser.browserparts.bottom.sheet.bookmark.BookmarkDatabaseHelper;
import com.ibndev.icebrowser.utilities.Statics;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class TopPopupMenu {

    private final Context context;
    BookmarkDatabaseHelper dbHelper;
    SQLiteDatabase database;
    private boolean isBookmarked = false;

    public TopPopupMenu(Context context) {
        this.context = context;

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

        isBookmarked = isUrlInBookmarks(currentUrl);

        if (isBookmarked) {
            popupMenu.getMenu().findItem(R.id.action_save_bookmark).setIcon(R.drawable.top_menu_bookmark_saved_icon);
            popupMenu.getMenu().findItem(R.id.action_save_bookmark).setTitle(context.getString(R.string.remove_bookmark));
        } else {
            popupMenu.getMenu().findItem(R.id.action_save_bookmark).setIcon(R.drawable.top_menu_bookmark_add_icon);
            popupMenu.getMenu().findItem(R.id.action_save_bookmark).setTitle(context.getString(R.string.save_bookmark));
        }

        popupMenu.setOnMenuItemClickListener(item -> onOptionsItemSelected(item, currentUrl, currentTitle));
        popupMenu.show();
    }

    @SuppressLint("HardwareIds")
    private boolean onOptionsItemSelected(MenuItem item, String currentUrl, String currentTitle) {
        if (item.getItemId() == R.id.action_open_with) {
            openUrlInApp(currentUrl);
            return true;
        } else if (item.getItemId() == R.id.action_save_bookmark) {
            if (isBookmarked) {
                deleteBookmark(currentUrl);
                Toast.makeText(context, context.getString(R.string.bookmark_removed), Toast.LENGTH_SHORT).show();
            } else {
                addBookmark(currentTitle, currentUrl);
                Toast.makeText(context, context.getString(R.string.bookmark_saved), Toast.LENGTH_SHORT).show();
            }
            return true;
        } else if (item.getItemId() == R.id.action_license_id) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(context.getString(R.string.license_id_menu));
            final EditText editText = new EditText(context);
            builder.setView(editText);
            builder.setPositiveButton(context.getString(R.string.ok), (dialog, id) -> {
            });
            AlertDialog dialog = builder.create();
            dialog.show();
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v -> {
                String newValue = editText.getText().toString();
                if (newValue.isEmpty()) {
                    Toast.makeText(context, context.getString(R.string.license_empty), Toast.LENGTH_LONG).show();
                } else {
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    DocumentReference userDocRef = db.collection("users").document(newValue);

                    userDocRef.get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                boolean active = Boolean.TRUE.equals(document.getBoolean("isPremium"));
                                String android_id = document.getString("android_id");

                                if(active){

                                    assert android_id != null;
                                    if(android_id.equals("null")){
                                        Map<String, Object> userData = new HashMap<>();
                                        userData.put("android_id", Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID));
                                        userDocRef.update(userData);
                                    } else if (!android_id.equals(Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID))) {
                                        Toast.makeText(context, context.getString(R.string.license_used), Toast.LENGTH_LONG).show();
                                        return;
                                    }

                                    Statics.isPremium = true;
                                    SharedPreferences sharedPreferences = context.getSharedPreferences("ICE_BROWSER", Activity.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putBoolean("PREMIUM", true).apply();
                                    editor.putString("LICENSE_ID", newValue).apply();

                                    Toast.makeText(context, context.getString(R.string.license_id_activated), Toast.LENGTH_LONG).show();

                                } else {
                                    Toast.makeText(context, context.getString(R.string.license_deactivated), Toast.LENGTH_LONG).show();
                                }
                            } else {
                                Toast.makeText(context, context.getString(R.string.license_not_found), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
                dialog.dismiss();

            });
        } else {
            return false;
        }
        return false;
    }


        private boolean isUrlInBookmarks (String urlToCheck){
            if (database == null) return false;
            Cursor cursor = database.rawQuery("SELECT 1 FROM bookmarks WHERE url = ?", new String[]{urlToCheck});

            boolean exists = cursor.moveToFirst();
            cursor.close();

            return exists;
        }

        private void addBookmark (String title, String url){

            ContentValues values = new ContentValues();
            values.put(BookmarkDatabaseHelper.COLUMN_TITLE, title);
            values.put(COLUMN_URL, url);
            database.insert(TABLE_BOOKMARKS, null, values);

        }

        private void deleteBookmark (String urlToCheck){
            if (database == null) return;
            database.delete(TABLE_BOOKMARKS, COLUMN_URL + " = ?", new String[]{urlToCheck});
        }

        private void openUrlInApp (String url){
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            try {
                context.startActivity(i);
            } catch (ActivityNotFoundException e) {
                new AlertDialog.Builder(context)
                        .setTitle(context.getString(R.string.fail))
                        .setMessage(context.getString(R.string.open_with_fail))
                        .setPositiveButton(context.getString(R.string.ok), (dialog1, which1) -> {
                        })
                        .show();
            }
        }
    }


