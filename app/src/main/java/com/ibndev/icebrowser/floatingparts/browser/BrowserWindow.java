package com.ibndev.icebrowser.floatingparts.browser;

import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;

import com.ibndev.icebrowser.R;
import com.ibndev.icebrowser.browserparts.top.autocomplete.SearchAutoComplete;
import com.ibndev.icebrowser.floatingparts.FloatingWindow;
import com.ibndev.icebrowser.floatingparts.utilities.WindowSearchAutoComplete;

import java.util.ArrayList;
import java.util.List;

public class BrowserWindow {
    private final List<String> tabTitles = new ArrayList<>();
    private WindowTabManager tabManager;
    private SearchAutoComplete autoComplete;

    public BrowserWindow (FloatingWindow floatingWindow){

        tabManager = new WindowTabManager(floatingWindow);

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
