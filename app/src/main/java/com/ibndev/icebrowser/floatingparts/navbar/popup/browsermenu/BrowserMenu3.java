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

public class BrowserMenu3 {

    Context context;
    ViewGroup floatView;
    FloatingUtils utils;
    WindowTabManager tabManager;
    WebSettings webSettings;

    boolean popupActive;
    boolean smallTextActive;
    boolean normalTextActive;
    boolean bigTextActive;

    public BrowserMenu3(FloatingWindow floatingWindow, FloatingUtils utils) {
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
        inflater.inflate(R.menu.window_browser_menu_set_3, popupMenu.getMenu());

        initializeMenuChecked(popupMenu);

        popupMenu.setOnMenuItemClickListener(this::onOptionsItemSelected);
        popupMenu.show();
    }

    private void initializeMenuChecked(PopupMenu popupMenu) {
        MenuItem popupItem = popupMenu.getMenu().findItem(R.id.action_popup);
        MenuItem smallTextItem = popupMenu.getMenu().findItem(R.id.action_text_small);
        MenuItem normalTextItem = popupMenu.getMenu().findItem(R.id.action_text_normal);
        MenuItem bigTextItem = popupMenu.getMenu().findItem(R.id.action_text_big);

        popupActive = (webSettings.getJavaScriptCanOpenWindowsAutomatically()
                && webSettings.supportMultipleWindows());
        smallTextActive = (webSettings.getTextZoom() == 70);
        normalTextActive = (webSettings.getTextZoom() == 100);
        bigTextActive = (webSettings.getTextZoom() == 130);

        popupItem.setChecked(popupActive);
        smallTextItem.setChecked(smallTextActive);
        normalTextItem.setChecked(normalTextActive);
        bigTextItem.setChecked(bigTextActive);

    }

    private boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_popup) {
            tabManager.getCurrentWebView().getSettings().setSupportMultipleWindows(!popupActive);
            tabManager.getCurrentWebView().getSettings().setJavaScriptCanOpenWindowsAutomatically(!popupActive);
        } else if (item.getItemId() == R.id.action_text_small) {
            if (!smallTextActive) {
                tabManager.getCurrentWebView().getSettings().setTextZoom(70);
            }
        } else if (item.getItemId() == R.id.action_text_normal) {
            if (!normalTextActive) {
                tabManager.getCurrentWebView().getSettings().setTextZoom(100);
            }
        } else if (item.getItemId() == R.id.action_text_big) {
            if (!bigTextActive) {
                tabManager.getCurrentWebView().getSettings().setTextZoom(130);
            }
        }
        return true;
    }

}
