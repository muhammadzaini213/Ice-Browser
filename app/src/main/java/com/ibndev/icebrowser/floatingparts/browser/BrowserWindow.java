package com.ibndev.icebrowser.floatingparts.browser;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.VideoController;
import com.google.android.gms.ads.VideoOptions;
import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.ibndev.icebrowser.R;
import com.ibndev.icebrowser.floatingparts.FloatingWindow;
import com.ibndev.icebrowser.floatingparts.utilities.WindowSearchAutoComplete;

import java.util.Objects;

public class BrowserWindow {

    FloatingWindow floatingWindow;

    public BrowserWindow(FloatingWindow floatingWindow, WindowTabManager tabManager) {
        this.floatingWindow = floatingWindow;

        new Thread(
                () -> {
                    MobileAds.initialize(floatingWindow.getApplicationContext(), initializationStatus -> {});
                })
                .start();


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
