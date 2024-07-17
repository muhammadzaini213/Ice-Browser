package com.ibndev.icebrowser.floatingparts.gallery;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ibndev.icebrowser.R;

import java.io.File;
import java.util.List;

public class FolderAdapter extends RecyclerView.Adapter<FolderAdapter.FolderViewHolder> {

    private final Context mContext;
    private final List<File> mFolders;
    private final OnFolderClickListener mListener;

    public FolderAdapter(Context context, List<File> folders, OnFolderClickListener listener) {
        mContext = context;
        mFolders = folders;
        mListener = listener;
    }

    @NonNull
    @Override
    public FolderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.window_gallery_item_folder, parent, false);
        return new FolderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FolderViewHolder holder, int position) {
        File folder = mFolders.get(position);
        holder.folderName.setText(folder.getName());
        holder.itemView.setOnClickListener(v -> mListener.onFolderClick(folder));
    }

    @Override
    public int getItemCount() {
        return mFolders.size();
    }

    public static class FolderViewHolder extends RecyclerView.ViewHolder {
        public TextView folderName;

        public FolderViewHolder(@NonNull View itemView) {
            super(itemView);
            folderName = itemView.findViewById(R.id.folderName);
        }
    }

    public interface OnFolderClickListener {
        void onFolderClick(File folder);
    }
}
