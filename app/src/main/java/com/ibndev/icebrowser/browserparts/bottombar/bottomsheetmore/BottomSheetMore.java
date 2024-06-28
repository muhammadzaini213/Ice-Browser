package com.ibndev.icebrowser.browserparts.bottombar.bottomsheetmore;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.ibndev.icebrowser.R;
import com.ibndev.icebrowser.browserparts.topbar.tab.TabManager;

public class BottomSheetMore {
    Activity activity;
    TabManager tabManager;
    BottomSheetDialog bottomSheetDialog;
    View bottomSheetView;
    BottomSheetMoreFunctions functions;

    @SuppressLint("InflateParams")
    public BottomSheetMore(Activity activity, TabManager tabManager) {
        this.activity = activity;
        this.tabManager = tabManager;

        functions = new BottomSheetMoreFunctions(activity, tabManager);
        bottomSheetDialog = new BottomSheetDialog(activity);
        bottomSheetView = LayoutInflater.from(activity).inflate(
                R.layout.activity_main_tabs_bottomsheet_more,
                null
        );
        bottomSheetDialog.setContentView(bottomSheetView);

    }

    public void show() {
        WebView currentWebview = tabManager.getCurrentWebView();
        WebSettings currentWebviewSettings = currentWebview.getSettings();

        bottomSheetView.findViewById(R.id.main_tabs_more_bottomsheet_close).setOnClickListener(view -> bottomSheetDialog.dismiss());

        javascript(currentWebviewSettings);
        cache(currentWebviewSettings);
        css();
        domStorage(currentWebviewSettings);

        overview(currentWebviewSettings);
        zoom(currentWebviewSettings);
        image(currentWebviewSettings);
        loader(currentWebviewSettings);

        popup(currentWebviewSettings);
        textSmall(currentWebviewSettings);
        textNormal(currentWebviewSettings);
        textBig(currentWebviewSettings);

        bottomSheetDialog.show();
    }

    private void javascript(WebSettings webSettings) {
        ConstraintLayout layout = layout(R.id.main_tabs_more_toggle_javascript);
        boolean isActive = webSettings.getJavaScriptEnabled();

        setActiveBackground(layout, isActive);

        layout.setOnClickListener(view -> {
            functions.toggleJavascript(isActive);
            javascript(webSettings);
        });
    }

    private void css() {
        ConstraintLayout layout = layout(R.id.main_tabs_more_toggle_css);
        String javascript =
                "(function() { " +
                        "    var cssEnabled = false; " +
                        "    document.querySelectorAll('link[rel=\"stylesheet\"]').forEach(function(link) {" +
                        "        if (!link.disabled) { " +
                        "            cssEnabled = true; " +
                        "        } " +
                        "    }); " +
                        "    document.querySelectorAll('style').forEach(function(style) {" +
                        "        if (!style.disabled) { " +
                        "            cssEnabled = true; " +
                        "        } " +
                        "    }); " +
                        "    return cssEnabled; " +
                        "})();";

        // Evaluate JavaScript and handle result
        tabManager.getCurrentWebView().evaluateJavascript(javascript, value -> {
            // 'value' contains the result from JavaScript (true/false)
            boolean cssEnabled = Boolean.parseBoolean(value);
            setActiveBackground(layout, cssEnabled);

            layout.setOnClickListener(view -> {
                functions.toggleCSS(cssEnabled);
                css(); // Recursive call to refresh CSS state after toggle
            });
        });
    }



    private void cache(WebSettings webSettings) {
        ConstraintLayout layout = layout(R.id.main_tabs_more_toggle_cache);
        boolean isActive = webSettings.getCacheMode() == WebSettings.LOAD_DEFAULT;

        setActiveBackground(layout, isActive);

        layout.setOnClickListener(view -> {
            functions.toggleCache(isActive);
            cache(webSettings);
        });
    }

    private void domStorage(WebSettings webSettings) {
        ConstraintLayout layout = layout(R.id.main_tabs_more_toggle_dom_storage);
        boolean isActive = webSettings.getDomStorageEnabled();

        setActiveBackground(layout, isActive);

        layout.setOnClickListener(view -> {
            functions.toggleDomStorage(isActive);
            domStorage(webSettings);
        });
    }

    private void overview(WebSettings webSettings) {
        ConstraintLayout layout = layout(R.id.main_tabs_more_toggle_overview);
        boolean isActive = webSettings.getUseWideViewPort();

        setActiveBackground(layout, isActive);

        layout.setOnClickListener(view -> {
            functions.toggleOverview(isActive);
            overview(webSettings);
        });
    }

    private void zoom(WebSettings webSettings) {
        ConstraintLayout layout = layout(R.id.main_tabs_more_toggle_zoom);
        boolean isActive = webSettings.supportZoom();

        setActiveBackground(layout, isActive);

        layout.setOnClickListener(view -> {
            functions.toggleZoom(isActive);
            zoom(webSettings);
        });
    }

    private void image(WebSettings webSettings) {
        ConstraintLayout layout = layout(R.id.main_tabs_more_toggle_image);
        boolean isActive = webSettings.getLoadsImagesAutomatically();

        setActiveBackground(layout, isActive);

        layout.setOnClickListener(view -> {
            functions.toggleImage(isActive);
            image(webSettings);
        });
    }

    private void loader(WebSettings webSettings) {
        ConstraintLayout layout = layout(R.id.main_tabs_more_toggle_loader);
        boolean isActive = webSettings.getBlockNetworkLoads();

        setActiveBackground(layout, !isActive);

        layout.setOnClickListener(view -> {
            functions.toggleLoader(!isActive);
            loader(webSettings);
        });
    }

    private void popup(WebSettings webSettings){
        ConstraintLayout layout = layout(R.id.main_tabs_more_toggle_popup);
        boolean isActive = (webSettings.getJavaScriptCanOpenWindowsAutomatically()
                && webSettings.supportMultipleWindows());

        setActiveBackground(layout, isActive);

        layout.setOnClickListener(view -> {
            functions.togglePopup(isActive);
            popup(webSettings);
        });
    }

    private void textSmall(WebSettings webSettings) {
        ConstraintLayout layout = layout(R.id.main_tabs_more_toggle_text_decrease);
        boolean isActive = (webSettings.getTextZoom() == 70);

        setActiveBackground(layout, isActive);

        layout.setOnClickListener(view -> {
            functions.toggleSmallText(isActive);
            text(webSettings);
        });
    }

    private void textNormal(WebSettings webSettings) {
        ConstraintLayout layout = layout(R.id.main_tabs_more_toggle_text_normal);
        boolean isActive = (webSettings.getTextZoom() == 100);

        setActiveBackground(layout, isActive);

        layout.setOnClickListener(view -> {
            functions.toggleNormalText(isActive);
            text(webSettings);
        });
    }

    private void textBig(WebSettings webSettings) {
        ConstraintLayout layout = layout(R.id.main_tabs_more_toggle_text_increase);
        boolean isActive = (webSettings.getTextZoom() == 130);

        setActiveBackground(layout, isActive);

        layout.setOnClickListener(view -> {
            functions.toggleBigText(isActive);
            text(webSettings);
        });
    }

    private void text(WebSettings webSettings){
        textBig(webSettings);
        textNormal(webSettings);
        textSmall(webSettings);
    }


    private void setActiveBackground(ConstraintLayout layout, boolean isActive) {
        if (isActive) {
            layout.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(activity, R.color.background_active)));
        } else {
            layout.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(activity, R.color.background_normal)));
        }
    }

    private ConstraintLayout layout(int id) {
        return bottomSheetView.findViewById(id);
    }
}
