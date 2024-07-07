package com.ibndev.icebrowser.floatingparts.navbar;

import android.view.ViewGroup;
import android.widget.ImageView;

import com.ibndev.icebrowser.R;
import com.ibndev.icebrowser.browserparts.top.tab.TabManager;
import com.ibndev.icebrowser.floatingparts.browser.WindowTabManager;
import com.ibndev.icebrowser.floatingparts.utilities.FloatingUtils;
import com.ibndev.icebrowser.floatingparts.FloatingWindow;
import com.ibndev.icebrowser.floatingparts.navbar.popup.browsermenu.BrowserMenu;
import com.ibndev.icebrowser.floatingparts.navbar.popup.setwindow.SetWindow;

public class WindowNavbar {
    FloatingWindow floatingWindow;
    ViewGroup floatView;
    FloatingUtils utils;
    WindowTabManager tabManager;

    public WindowNavbar(FloatingWindow floatingWindow, FloatingUtils utils) {
        this.floatingWindow = floatingWindow;
        this.utils = utils;

        floatView = floatingWindow.floatView;
    }

    public void setNavbar(WindowTabManager tabManager) {
        this.tabManager = tabManager;
        ImageView setWindowBtn = floatView.findViewById(R.id.window_main_top_navbar_layout_set_button);
        SetWindow setWindow = new SetWindow(floatingWindow, utils);
        setWindowBtn.setOnClickListener(view -> setWindow.showPopupMenu());

        ImageView browserMenuBtn = floatView.findViewById(R.id.window_main_top_navbar_menu_button);
        BrowserMenu browserMenu = new BrowserMenu(floatingWindow, utils);
        browserMenuBtn.setOnClickListener(view -> browserMenu.showPopupMenu(tabManager));
    }

}
