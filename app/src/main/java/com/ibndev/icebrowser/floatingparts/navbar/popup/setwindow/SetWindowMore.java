package com.ibndev.icebrowser.floatingparts.navbar.popup.setwindow;

import android.content.Context;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import com.ibndev.icebrowser.R;
import com.ibndev.icebrowser.floatingparts.FloatingWindow;
import com.ibndev.icebrowser.floatingparts.navbar.popup.browsermenu.CloseAllConfirmation;
import com.ibndev.icebrowser.floatingparts.utilities.FloatingUtils;
import com.ibndev.icebrowser.floatingparts.utilities.LayoutSetData;

public class SetWindowMore {

    Context context;
    ViewGroup floatView;
    FloatingUtils utils;
    LayoutSetData layoutSetData;
    FloatingWindow floatingWindow;

    public SetWindowMore(FloatingWindow floatingWindow, FloatingUtils utils) {
        this.floatingWindow = floatingWindow;
        context = floatingWindow.getApplicationContext();
        floatView = floatingWindow.floatView;
        layoutSetData = utils.layoutSetData;
        this.utils = utils;
    }

    PopupMenu popupMenu;

    public void showPopupMenu() {
        if (popupMenu == null) {
            popupMenu = new PopupMenu(context, floatView.findViewById(R.id.window_main_top_navbar_layout_set_button));
            MenuInflater inflater = popupMenu.getMenuInflater();
            inflater.inflate(R.menu.window_layout_set_menu_more, popupMenu.getMenu());
        }


        popupMenu.setOnMenuItemClickListener(this::onOptionsItemSelected);
        popupMenu.show();
    }

    private boolean onOptionsItemSelected(MenuItem item) {
        item.setChecked(!item.isChecked());
        if (item.getItemId() == R.id.action_close_all_window) {
            new CloseAllConfirmation(floatingWindow, utils).showPopupMenu();
        }
        if (item.isChecked()) {
            if (item.getItemId() == R.id.action_hidden_mode) {
                layoutSetData.isHiddenMode = true;
            } else if (item.getItemId() == R.id.action_static_bubble) {
                layoutSetData.isStaticBubble = true;
            } else if (item.getItemId() == R.id.action_long_click) {
                layoutSetData.isLongClick = true;
            } else if (item.getItemId() == R.id.action_no_focus) {
                layoutSetData.isNonFocusable = true;
            } else if (item.getItemId() == R.id.action_anti_obscure) {
                layoutSetData.isAntiObscureVolume = true;
            }
            return true;
        } else {
            if (item.getItemId() == R.id.action_hidden_mode) {
                layoutSetData.isHiddenMode = false;
            } else if (item.getItemId() == R.id.action_static_bubble) {
                layoutSetData.isStaticBubble = false;
            } else if (item.getItemId() == R.id.action_long_click) {
                layoutSetData.isLongClick = false;
            } else if (item.getItemId() == R.id.action_no_focus) {
                layoutSetData.isNonFocusable = false;
            } else if (item.getItemId() == R.id.action_anti_obscure) {
                layoutSetData.isAntiObscureVolume = false;
            }
            return false;

        }

    }
}
