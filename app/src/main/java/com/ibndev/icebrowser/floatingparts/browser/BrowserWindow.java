package com.ibndev.icebrowser.floatingparts.browser;

import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ibndev.icebrowser.R;
import com.ibndev.icebrowser.floatingparts.FloatingWindow;
import com.ibndev.icebrowser.floatingparts.gallery.CustomImageView;
import com.ibndev.icebrowser.floatingparts.gallery.FolderAdapter;
import com.ibndev.icebrowser.floatingparts.gallery.ImageAdapter;
import com.ibndev.icebrowser.floatingparts.utilities.WindowSearchAutoComplete;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BrowserWindow implements FolderAdapter.OnFolderClickListener, ImageAdapter.OnImageClickListener{

    FloatingWindow floatingWindow;
    public BrowserWindow(FloatingWindow floatingWindow, WindowTabManager tabManager) {
        this.floatingWindow = floatingWindow;
        tabManager.newTab("google.com");

        tabManager.getCurrentWebView().setVisibility(View.VISIBLE);

        new WindowSearchAutoComplete(floatingWindow, tabManager);
        ViewGroup floatView = floatingWindow.floatView;

        AutoCompleteTextView autoCompleteTextView = floatView.findViewById(R.id.window_main_top_navbar_autocomplete);
        ImageView hideSearch = floatView.findViewById(R.id.window_main_top_navbar_hide_search_button);
        ImageView showSearch = floatView.findViewById(R.id.window_main_top_navbar_show_search_button);

        hideSearch.setOnClickListener(view -> {
            hideSearch.setVisibility(View.GONE);
            autoCompleteTextView.setVisibility(View.GONE);

            showSearch.setVisibility(View.VISIBLE);
        });

        showSearch.setOnClickListener(view -> {
            showSearch.setVisibility(View.GONE);
            autoCompleteTextView.setVisibility(View.VISIBLE);

            hideSearch.setVisibility(View.VISIBLE);
        });


        mRecyclerView = floatView.findViewById(R.id.window_gallery_recyclerview);
        mRecyclerView.setLayoutManager(new GridLayoutManager(floatingWindow.getApplicationContext(), 2));

        loadFolders();
    }

    private RecyclerView mRecyclerView;
    private FolderAdapter folderAdapter;
    private List<File> mFolders;

    private void loadFolders() {
        // Load image folders
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
        folderAdapter = new FolderAdapter(floatingWindow.getApplicationContext(), mFolders, this);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(floatingWindow.getApplicationContext()));
        mRecyclerView.setAdapter(folderAdapter);

    }

    @Override
    public void onFolderClick(File folder) {
        loadImages(folder.getAbsolutePath());
    }

    private List<File> mImageFiles;

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


    @Override
    public void onImageClick(File folder) {
        CustomImageView zoomImageView = floatingWindow.floatView.findViewById(R.id.fullscreenImageView);

        zoomImageView.setVisibility(View.VISIBLE);

        Glide.with(floatingWindow.getApplicationContext()).load(folder.getAbsolutePath()).into(zoomImageView);
    }
}
