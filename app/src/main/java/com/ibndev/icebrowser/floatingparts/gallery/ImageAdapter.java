package com.ibndev.icebrowser.floatingparts.gallery;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ibndev.icebrowser.R;

import java.io.File;
import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {

    private final Context mContext;
    private final List<File> mImageFiles;
    private final OnImageClickListener mListener;

    public ImageAdapter(Context context, List<File> imageFiles, OnImageClickListener listener) {
        mContext = context;
        mImageFiles = imageFiles;
        mListener = listener;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.window_gallery_item_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        File imageFile = mImageFiles.get(position);
        Glide.with(mContext).load(imageFile).into(holder.imageView);

        holder.itemView.setOnClickListener(v -> mListener.onImageClick(imageFile));
    }

    @Override
    public int getItemCount() {
        return mImageFiles.size();
    }

    public interface OnImageClickListener {
        void onImageClick(File folder);
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.gallery_image);
        }
    }
}

