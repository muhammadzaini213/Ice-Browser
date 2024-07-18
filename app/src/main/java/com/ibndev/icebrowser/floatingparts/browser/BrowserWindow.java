package com.ibndev.icebrowser.floatingparts.browser;

import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ibndev.icebrowser.R;
import com.ibndev.icebrowser.floatingparts.FloatingWindow;
import com.ibndev.icebrowser.floatingparts.gallery.CustomImageView;
import com.ibndev.icebrowser.floatingparts.gallery.FolderAdapter;
import com.ibndev.icebrowser.floatingparts.gallery.GalleryWindow;
import com.ibndev.icebrowser.floatingparts.gallery.ImageAdapter;
import com.ibndev.icebrowser.floatingparts.utilities.WindowSearchAutoComplete;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BrowserWindow{

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

    }


}
