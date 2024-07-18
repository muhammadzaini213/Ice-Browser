package com.ibndev.icebrowser.floatingparts.navbar.popup.browsermenu;

import android.content.Context;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import com.ibndev.icebrowser.R;
import com.ibndev.icebrowser.floatingparts.FloatingWindow;
import com.ibndev.icebrowser.floatingparts.utilities.FloatingUtils;
import com.ibndev.icebrowser.floatingparts.utilities.LayoutSetData;
import com.ibndev.icebrowser.floatingparts.utilities.OverlayManager;

public class CloseAllConfirmation {
    Context context;
    ViewGroup floatView;
    FloatingUtils utils;
    FloatingWindow floatingWindow;

    public CloseAllConfirmation(FloatingWindow floatingWindow, FloatingUtils utils) {
        this.floatingWindow = floatingWindow;
        context = floatingWindow.getApplicationContext();
        floatView = floatingWindow.floatView;
        this.utils = utils;
    }

    PopupMenu popupMenu;

    public void showPopupMenu() {
        if (popupMenu == null) {
            popupMenu = new PopupMenu(context, floatView.findViewById(R.id.window_main_top_navbar_layout_set_button));
            MenuInflater inflater = popupMenu.getMenuInflater();
            inflater.inflate(R.menu.window_close_all_confirmation, popupMenu.getMenu());
        }
        popupMenu.setOnMenuItemClickListener(this::onOptionsItemSelected);
        popupMenu.show();
    }

    private boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_yes){
            OverlayManager.setOverlayVisibility(false);
        }
        return true;
    }
}
