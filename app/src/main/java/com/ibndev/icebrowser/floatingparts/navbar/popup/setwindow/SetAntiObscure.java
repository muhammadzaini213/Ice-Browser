package com.ibndev.icebrowser.floatingparts.navbar.popup.setwindow;

import android.content.Context;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import com.ibndev.icebrowser.R;
import com.ibndev.icebrowser.floatingparts.FloatingWindow;
import com.ibndev.icebrowser.floatingparts.utilities.FloatingUtils;
import com.ibndev.icebrowser.floatingparts.utilities.LayoutSetData;

public class SetAntiObscure {

    Context context;
    ViewGroup floatView;
    FloatingUtils utils;
    FloatingWindow floatingWindow;
    PopupMenu popupMenu;

    boolean antiObscureVolumeActive;
    boolean antiObscureShakeActive;

    public SetAntiObscure(FloatingWindow floatingWindow, FloatingUtils utils) {
        this.floatingWindow = floatingWindow;
        context = floatingWindow.getApplicationContext();
        floatView = floatingWindow.floatView;
        this.utils = utils;
    }

    public void showPopupMenu() {
        if (popupMenu == null) {
            popupMenu = new PopupMenu(context, floatView.findViewById(R.id.window_main_top_navbar_layout_set_button));
            MenuInflater inflater = popupMenu.getMenuInflater();
            inflater.inflate(R.menu.window_anti_obscure_options, popupMenu.getMenu());
        }

        initializeMenuChecked(popupMenu);
        popupMenu.setOnMenuItemClickListener(this::onOptionsItemSelected);

        popupMenu.show();
    }

    private void initializeMenuChecked(PopupMenu popupMenu) {
        MenuItem antiObscureVolume = popupMenu.getMenu().findItem(R.id.action_anti_obscure_volume);
        MenuItem antiObscureShake = popupMenu.getMenu().findItem(R.id.action_anti_obscure_shake);

        antiObscureVolume.setChecked(LayoutSetData.isAntiObscureVolume);
        antiObscureShake.setChecked(LayoutSetData.isAntiObscureShake);

    }

    private boolean onOptionsItemSelected(MenuItem item) {
        item.setChecked(!item.isChecked());
        if (item.isChecked()) {
            if (item.getItemId() == R.id.action_anti_obscure_volume) {
                LayoutSetData.isAntiObscureVolume = true;
                LayoutSetData.isAntiObscureShake = false;

                MenuItem antiObscureShake = popupMenu.getMenu().findItem(R.id.action_anti_obscure_shake);
                antiObscureShake.setChecked(false);
            } else if (item.getItemId() == R.id.action_anti_obscure_shake) {
                LayoutSetData.isAntiObscureShake = true;
                LayoutSetData.isAntiObscureVolume = false;

                MenuItem antiObscureVolume = popupMenu.getMenu().findItem(R.id.action_anti_obscure_volume);
                antiObscureVolume.setChecked(false);
            }
            return true;
        } else {
            if (item.getItemId() == R.id.action_anti_obscure_volume) {
                LayoutSetData.isAntiObscureVolume = false;
            } else if (item.getItemId() == R.id.action_anti_obscure_shake) {
                LayoutSetData.isAntiObscureShake = false;
            }
            return false;

        }

    }
}
