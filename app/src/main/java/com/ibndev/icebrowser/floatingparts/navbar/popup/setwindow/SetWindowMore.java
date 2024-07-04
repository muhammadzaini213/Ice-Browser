package com.ibndev.icebrowser.floatingparts.navbar.popup.setwindow;

import android.content.Context;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import com.ibndev.icebrowser.R;
import com.ibndev.icebrowser.floatingparts.utilities.FloatingUtils;
import com.ibndev.icebrowser.floatingparts.FloatingWindow;
import com.ibndev.icebrowser.floatingparts.utilities.LayoutSetData;

public class SetWindowMore {

    Context context;
    ViewGroup floatView;
    FloatingUtils utils;

    public SetWindowMore(FloatingWindow floatingWindow, FloatingUtils utils) {
        context = floatingWindow.getApplicationContext();
        floatView = floatingWindow.floatView;
        this.utils = utils;
    }

    PopupMenu popupMenu;

    public void showPopupMenu() {
        if(popupMenu == null){
            popupMenu = new PopupMenu(context, floatView.findViewById(R.id.window_main_top_navbar_layout_set_button));
            MenuInflater inflater = popupMenu.getMenuInflater();
            inflater.inflate(R.menu.window_layout_set_menu_more, popupMenu.getMenu());
        }


        popupMenu.setOnMenuItemClickListener(this::onOptionsItemSelected);
        popupMenu.show();
    }

    private boolean onOptionsItemSelected(MenuItem item) {
        item.setChecked(!item.isChecked());
        if (item.isChecked()) {
           if(item.getItemId() == R.id.action_hidden_mode){
               LayoutSetData.isHiddenMode = true;
           } else if (item.getItemId() == R.id.action_static_bubble) {
               LayoutSetData.isStaticBubble = true;
           } else if (item.getItemId() == R.id.action_long_click) {
               LayoutSetData.isLongClick = true;
           } else if (item.getItemId() == R.id.action_no_focus) {
               LayoutSetData.isNonFocusable = true;
           }
            return true;
        } else {
            if(item.getItemId() == R.id.action_hidden_mode){
                LayoutSetData.isHiddenMode = false;
            } else if (item.getItemId() == R.id.action_static_bubble) {
                LayoutSetData.isStaticBubble = false;
            } else if (item.getItemId() == R.id.action_long_click) {
                LayoutSetData.isLongClick = false;
            } else if (item.getItemId() == R.id.action_no_focus) {
                LayoutSetData.isNonFocusable = false;
            }
            return false;

        }

    }
}
