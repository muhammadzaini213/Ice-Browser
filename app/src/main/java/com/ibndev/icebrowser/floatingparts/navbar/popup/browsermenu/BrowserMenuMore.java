package com.ibndev.icebrowser.floatingparts.navbar.popup.browsermenu;

import android.content.Context;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import com.ibndev.icebrowser.R;
import com.ibndev.icebrowser.floatingparts.utilities.FloatingUtils;
import com.ibndev.icebrowser.floatingparts.FloatingWindow;

public class BrowserMenuMore {
    Context context;
    ViewGroup floatView;
    FloatingUtils utils;

    BrowserMenu1 menu1;
    BrowserMenu2 menu2;
    BrowserMenu3 menu3;

    public BrowserMenuMore(FloatingWindow floatingWindow, FloatingUtils utils) {
        context = floatingWindow.getApplicationContext();
        floatView = floatingWindow.floatView;
        this.utils = utils;

        menu1 = new BrowserMenu1(floatingWindow, utils);
        menu2 = new BrowserMenu2(floatingWindow, utils);
        menu3 = new BrowserMenu3(floatingWindow, utils);
    }

    public void showPopupMenu() {
        PopupMenu popupMenu = new PopupMenu(context, floatView.findViewById(R.id.window_main_top_navbar_layout_set_button));
        MenuInflater inflater = popupMenu.getMenuInflater();
        inflater.inflate(R.menu.window_browser_menu_more, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(this::onOptionsItemSelected);
        popupMenu.show();
    }

    private boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_menu_set_1) {
            menu1.showPopupMenu();
            return true;
        } else if (item.getItemId() == R.id.action_menu_set_2) {
            menu2.showPopupMenu();
            return true;
        } else if (item.getItemId() == R.id.action_menu_set_3) {
            menu3.showPopupMenu();
            return true;
        } else {
            return false;
        }
    }


}
