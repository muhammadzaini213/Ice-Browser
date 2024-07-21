package com.ibndev.icebrowser.floatingparts.navbar.popup.browsermenu;

import android.content.Context;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.PopupMenu;

import com.ibndev.icebrowser.R;
import com.ibndev.icebrowser.floatingparts.FloatingWindow;
import com.ibndev.icebrowser.floatingparts.browser.WindowTabManager;
import com.ibndev.icebrowser.floatingparts.utilities.FloatingUtils;

public class BrowserMenu2 {

    Context context;
    ViewGroup floatView;
    FloatingUtils utils;
    WindowTabManager tabManager;
    WebSettings webSettings;

    boolean overviewActive;
    boolean zoomActive;
    boolean imageActive;
    boolean loaderActive;

    public BrowserMenu2(FloatingWindow floatingWindow, FloatingUtils utils) {
        context = floatingWindow.getApplicationContext();
        floatView = floatingWindow.floatView;
        this.utils = utils;
    }

    public void showPopupMenu(WindowTabManager tabManager) {
        this.tabManager = tabManager;

        WebView currentWebview = tabManager.getCurrentWebView();
        webSettings = currentWebview.getSettings();

        PopupMenu popupMenu = new PopupMenu(context, floatView.findViewById(R.id.window_main_top_navbar_layout_set_button));
        MenuInflater inflater = popupMenu.getMenuInflater();
        inflater.inflate(R.menu.window_browser_menu_set_2, popupMenu.getMenu());

        initializeMenuChecked(popupMenu);

        popupMenu.setOnMenuItemClickListener(this::onOptionsItemSelected);
        popupMenu.show();
    }

    private void initializeMenuChecked(PopupMenu popupMenu) {
        MenuItem overviewItem = popupMenu.getMenu().findItem(R.id.action_overview);
        MenuItem zoomItem = popupMenu.getMenu().findItem(R.id.action_zoom);
        MenuItem imageItem = popupMenu.getMenu().findItem(R.id.action_image);
        MenuItem loaderItem = popupMenu.getMenu().findItem(R.id.action_loader);

        overviewActive = webSettings.getUseWideViewPort();
        zoomActive = webSettings.supportZoom();
        imageActive = webSettings.getLoadsImagesAutomatically();
        loaderActive = webSettings.getBlockNetworkLoads();

        overviewItem.setChecked(overviewActive);
        zoomItem.setChecked(zoomActive);
        imageItem.setChecked(imageActive);
        loaderItem.setChecked(loaderActive);

    }


    private boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_overview) {
            tabManager.getCurrentWebView().getSettings().setUseWideViewPort(!overviewActive);
        } else if (item.getItemId() == R.id.action_zoom) {
            tabManager.getCurrentWebView().getSettings().setSupportZoom(!zoomActive);
        } else if (item.getItemId() == R.id.action_image) {
            tabManager.getCurrentWebView().getSettings().setLoadsImagesAutomatically(!imageActive);
        } else if (item.getItemId() == R.id.action_loader) {
            tabManager.getCurrentWebView().getSettings().setBlockNetworkLoads(!loaderActive);
        }
        return true;
    }

}
