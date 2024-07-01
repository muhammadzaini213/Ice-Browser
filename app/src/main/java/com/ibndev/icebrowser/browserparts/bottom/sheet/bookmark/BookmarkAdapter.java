package com.ibndev.icebrowser.browserparts.bottom.sheet.bookmark;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.ibndev.icebrowser.R;
import com.ibndev.icebrowser.browserparts.top.tab.TabManager;
import com.squareup.picasso.Picasso;

public class BookmarkAdapter extends RecyclerView.Adapter<BookmarkAdapter.ViewHolder> {
    private final Cursor cursor;
    Activity activity;
    TabManager tabManager;
    BookmarkSheet bookmarkSheetDialog;

    public BookmarkAdapter(Activity activity, Cursor cursor, TabManager tabManager, BookmarkSheet bookmarkSheetDialog) {
        this.cursor = cursor;
        this.activity = activity;
        this.tabManager = tabManager;
        this.bookmarkSheetDialog = bookmarkSheetDialog;
    }

    @NonNull
    @Override
    public BookmarkAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_main_bottomsheet_bookmark_adapter, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (cursor.moveToPosition(position)) {
            @SuppressLint("Range") String title = cursor.getString(cursor.getColumnIndex("title"));
            @SuppressLint("Range") String url = cursor.getString(cursor.getColumnIndex("url"));
            @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex("_id"));

            holder.title.setText(title);
            holder.url.setText(url);
            Picasso.get()
                    .load("https://www.google.com/s2/favicons?domain=" + url + "&sz=256")
                    .placeholder(R.drawable.tabs_favicon_not_found) // optional placeholder image
                    .into(holder.favicon, new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {
                        }

                        @Override
                        public void onError(Exception e) {
                        }
                    });

            BookmarkAdapterListener listener = new BookmarkAdapterListener(activity, holder, tabManager, bookmarkSheetDialog);
            listener.onClick(url);
            listener.openMenu(title, url, id);
        }
    }

    @Override
    public int getItemCount() {
        return cursor.getCount();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public TextView url;
        public ImageView favicon;
        public ImageView menu;
        public ConstraintLayout layout;

        public ViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.main_bookmark_item_adapter_tab_title);
            url = view.findViewById(R.id.main_bookmark_item_adapter_tab_url);
            favicon = view.findViewById(R.id.main_bookmark_item_adapter_favicon);
            menu = view.findViewById(R.id.main_bookmark_item_adapter_menu);
            layout = view.findViewById(R.id.main_bottomsheet_bookmark_adapter_layout);
        }
    }
}
