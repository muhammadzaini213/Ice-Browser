package com.ibndev.icebrowser.floatingparts.gallery;

import android.os.Environment;
import android.view.View;
import android.widget.ImageView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ibndev.icebrowser.R;
import com.ibndev.icebrowser.floatingparts.FloatingWindow;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GalleryWindow implements FolderAdapter.OnFolderClickListener, ImageAdapter.OnImageClickListener {

    FloatingWindow floatingWindow;

    private RecyclerView mRecyclerView;
    private FolderAdapter folderAdapter;
    private List<File> mFolders;
    private List<File> mImageFiles;

    public GalleryWindow(FloatingWindow floatingWindow){
        this.floatingWindow = floatingWindow;
        mRecyclerView = floatingWindow.floatView.findViewById(R.id.window_gallery_recyclerview);
        mRecyclerView.setLayoutManager(new GridLayoutManager(floatingWindow.getApplicationContext(), 2));

        loadFolders();

        ImageView backBtn = floatingWindow.floatView.findViewById(R.id.window_gallery_back_button);


        backBtn.setOnClickListener(view -> {
            CustomImageView zoomImageView = floatingWindow.floatView.findViewById(R.id.fullscreenImageView);

            if(zoomImageView.getVisibility() == View.VISIBLE){
                zoomImageView = floatingWindow.floatView.findViewById(R.id.fullscreenImageView);
                zoomImageView.setVisibility(View.GONE);
            } else {
                loadFolders();
            }
        });

    }

    private void loadFolders() {
        // Load image folders

        if(mFolders == null) {
            Set<File> folderSet = new HashSet<>();
            File[] directories = new File(Environment.getExternalStorageDirectory().getAbsolutePath()).listFiles();
            if (directories != null) {
                for (File directory : directories) {
                    if (directory.isDirectory()) {
                        File[] files = directory.listFiles();
                        if (files != null) {
                            for (File file : files) {
                                if (file.isFile() && (file.getName().endsWith(".jpg") || file.getName().endsWith(".png"))) {
                                    folderSet.add(directory);
                                    break;
                                }
                            }
                        }
                    }
                }
            }

            mFolders = new ArrayList<>(folderSet);
        }

        folderAdapter = new FolderAdapter(floatingWindow.getApplicationContext(), mFolders, this);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(floatingWindow.getApplicationContext()));
        mRecyclerView.setAdapter(folderAdapter);
    }

    @Override
    public void onFolderClick(File folder) {
        loadImages(folder.getAbsolutePath());

        ImageView backBtn = floatingWindow.floatView.findViewById(R.id.window_gallery_back_button);
        backBtn.setVisibility(View.VISIBLE);
    }

    private void loadImages(String folderPath) {
        File folder = new File(folderPath);
        File[] files = folder.listFiles();

        mImageFiles = new ArrayList<>();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && (file.getName().endsWith(".jpg") || file.getName().endsWith(".png"))) {
                    mImageFiles.add(file);
                }
            }
        }

        mRecyclerView.setLayoutManager(new GridLayoutManager(floatingWindow.getApplicationContext(), 2));
        ImageAdapter imageAdapter = new ImageAdapter(floatingWindow.getApplicationContext(), mImageFiles, this);
        mRecyclerView.setAdapter(imageAdapter);
    }

    private void hideGallery(){

    }

    @Override
    public void onImageClick(File folder) {
        CustomImageView zoomImageView = floatingWindow.floatView.findViewById(R.id.fullscreenImageView);
        zoomImageView.setVisibility(View.VISIBLE);

        Glide.with(floatingWindow.getApplicationContext()).load(folder.getAbsolutePath()).into(zoomImageView);
    }
}
