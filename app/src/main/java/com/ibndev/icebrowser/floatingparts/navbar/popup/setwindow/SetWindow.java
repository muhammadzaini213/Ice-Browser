package com.ibndev.icebrowser.floatingparts.navbar.popup.setwindow;

import android.content.Context;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import com.ibndev.icebrowser.R;
import com.ibndev.icebrowser.floatingparts.FloatingWindow;
import com.ibndev.icebrowser.floatingparts.utilities.FloatingUtils;

public class SetWindow
{
    Context context;
    ViewGroup floatView;
    FloatingUtils utils;
    FloatingWindow floatingWindow;
    SetWindowMore more;

    public SetWindow(FloatingWindow floatingWindow, FloatingUtils utils){
        this.floatingWindow = floatingWindow;
        context = floatingWindow.getApplicationContext();
        floatView = floatingWindow.floatView;
        this.utils = utils;
        more = new SetWindowMore(floatingWindow, utils);
    }

    public void showPopupMenu() {
        PopupMenu popupMenu = new PopupMenu(context, floatView.findViewById(R.id.window_main_top_navbar_layout_set_button));
        MenuInflater inflater = popupMenu.getMenuInflater();
        inflater.inflate(R.menu.window_layout_set_menu, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(this::onOptionsItemSelected);
        popupMenu.show();
    }

    private boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_hide) {
            utils.hideFloating();
            return true;
        } else if (item.getItemId() == R.id.action_topper) {
            utils.bypassFloating();
            return true;
        } else if (item.getItemId() == R.id.action_new_window) {
            floatingWindow.createWindow();
            return true;
        } else if (item.getItemId() == R.id.action_more) {
            more.showPopupMenu();
            return true;
        } else {
            return false;
        }

    }
}