package com.ibndev.icebrowser.floatingparts.navbar.popup.browsermenu;

import android.content.Context;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import com.ibndev.icebrowser.R;
import com.ibndev.icebrowser.floatingparts.FloatingWindow;
import com.ibndev.icebrowser.floatingparts.browser.WindowTabManager;
import com.ibndev.icebrowser.floatingparts.utilities.FloatingUtils;

import java.util.ArrayList;
import java.util.List;

public class BrowserMenu {

    Context context;
    ViewGroup floatView;
    FloatingUtils utils;
    WindowTabManager tabManager;
    BrowserMenuMore more;
    TabsMenu tabsMenu;

    public BrowserMenu(FloatingWindow floatingWindow, FloatingUtils utils) {
        context = floatingWindow.getApplicationContext();
        floatView = floatingWindow.floatView;
        this.utils = utils;
        more = new BrowserMenuMore(floatingWindow, utils);
        tabsMenu = new TabsMenu(floatingWindow, utils);
    }

    public void showPopupMenu(WindowTabManager tabManager) {
        this.tabManager = tabManager;

        PopupMenu popupMenu = new PopupMenu(context, floatView.findViewById(R.id.window_main_top_navbar_layout_set_button));
        MenuInflater inflater = popupMenu.getMenuInflater();
        inflater.inflate(R.menu.window_browser_menu, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(this::onOptionsItemSelected);
        popupMenu.show();
    }

    private boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_previous) {
            if (tabManager.getCurrentWebView().canGoBack()) {
                tabManager.getCurrentWebView().goBack();
            }
            return true;
        } else if (item.getItemId() == R.id.action_forward) {
            if (tabManager.getCurrentWebView().canGoForward()) {
                tabManager.getCurrentWebView().goForward();
            }
            return true;

        } else if (item.getItemId() == R.id.action_home) {
            tabManager.getCurrentWebView().loadUrl("google.com");
            return true;

        } else if (item.getItemId() == R.id.action_refresh) {
            tabManager.getCurrentWebView().reload();
            return true;

        } else if (item.getItemId() == R.id.action_tabs) {
            tabsMenu.showTabs(tabManager);
            return true;

        } else if (item.getItemId() == R.id.action_browser_menu_more) {
            more.showPopupMenu(tabManager);
            return true;
        } else {
            return false;
        }

    }

}
