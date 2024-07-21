package com.ibndev.icebrowser.floatingparts.navbar.popup.setwindow;

import android.app.NotificationManager;
import android.content.Context;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.ibndev.icebrowser.R;
import com.ibndev.icebrowser.floatingparts.FloatingWindow;
import com.ibndev.icebrowser.floatingparts.navbar.popup.browsermenu.CloseAllConfirmation;
import com.ibndev.icebrowser.floatingparts.utilities.FloatingUtils;
import com.ibndev.icebrowser.floatingparts.utilities.LayoutSetData;

public class SetWindowMore {

    Context context;
    ViewGroup floatView;
    FloatingUtils utils;
    FloatingWindow floatingWindow;
    SetAntiObscure setAntiObscure;
    PopupMenu popupMenu;

    public SetWindowMore(FloatingWindow floatingWindow, FloatingUtils utils) {
        this.floatingWindow = floatingWindow;
        context = floatingWindow.getApplicationContext();
        floatView = floatingWindow.floatView;
        this.utils = utils;
        setAntiObscure = new SetAntiObscure(floatingWindow, utils);
    }

    public void showPopupMenu() {
        if (popupMenu == null) {
            popupMenu = new PopupMenu(context, floatView.findViewById(R.id.window_main_top_navbar_layout_set_button));
            MenuInflater inflater = popupMenu.getMenuInflater();
            inflater.inflate(R.menu.window_layout_set_menu_more, popupMenu.getMenu());
        }

        initializeMenuChecked(popupMenu);


        popupMenu.setOnMenuItemClickListener(this::onOptionsItemSelected);
        popupMenu.show();
    }

    private void initializeMenuChecked(PopupMenu popupMenu) {
        MenuItem hiddenMode = popupMenu.getMenu().findItem(R.id.action_hidden_mode);
        MenuItem staticBubble = popupMenu.getMenu().findItem(R.id.action_static_bubble);
        MenuItem longClick = popupMenu.getMenu().findItem(R.id.action_long_click);
        MenuItem noFocus = popupMenu.getMenu().findItem(R.id.action_no_focus);
        MenuItem dnd = popupMenu.getMenu().findItem(R.id.action_dnd);
        MenuItem antiObscure = popupMenu.getMenu().findItem(R.id.action_anti_obscure);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        hiddenMode.setChecked(LayoutSetData.isHiddenMode);
        staticBubble.setChecked(LayoutSetData.isStaticBubble);
        longClick.setChecked(LayoutSetData.isLongClick);
        noFocus.setChecked(LayoutSetData.isNonFocusable);
        dnd.setChecked(notificationManager.getCurrentInterruptionFilter() == NotificationManager.INTERRUPTION_FILTER_NONE);
        antiObscure.setChecked((LayoutSetData.isAntiObscureVolume || LayoutSetData.isAntiObscureShake));


    }

    private boolean onOptionsItemSelected(MenuItem item) {
        item.setChecked(!item.isChecked());
        if (item.getItemId() == R.id.action_close_all_window) {
            new CloseAllConfirmation(floatingWindow, utils).showPopupMenu();
        } else if (item.getItemId() == R.id.action_anti_obscure) {
            setAntiObscure.showPopupMenu();
        }

        if (item.isChecked()) {
            if (item.getItemId() == R.id.action_hidden_mode) {
                LayoutSetData.isHiddenMode = true;
            } else if (item.getItemId() == R.id.action_static_bubble) {
                LayoutSetData.isStaticBubble = true;
            } else if (item.getItemId() == R.id.action_long_click) {
                LayoutSetData.isLongClick = true;
            } else if (item.getItemId() == R.id.action_no_focus) {
                LayoutSetData.isNonFocusable = true;
            } else if (item.getItemId() == R.id.action_dnd) {
                enableDND();
            }
            return true;
        } else {
            if (item.getItemId() == R.id.action_hidden_mode) {
                LayoutSetData.isHiddenMode = false;
            } else if (item.getItemId() == R.id.action_static_bubble) {
                LayoutSetData.isStaticBubble = false;
            } else if (item.getItemId() == R.id.action_long_click) {
                LayoutSetData.isLongClick = false;
            } else if (item.getItemId() == R.id.action_no_focus) {
                LayoutSetData.isNonFocusable = false;
            } else if (item.getItemId() == R.id.action_dnd) {
                disableDND();
            }
            return false;

        }

    }

    private void enableDND() {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager.isNotificationPolicyAccessGranted()) {
            notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE);
        } else {
            requestDNDPermission();
        }
    }


    private void disableDND() {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager.isNotificationPolicyAccessGranted()) {
            notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL);
        } else {
            requestDNDPermission();
        }
    }

    private void requestDNDPermission() {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (!notificationManager.isNotificationPolicyAccessGranted()) {
            Toast.makeText(context, context.getString(R.string.dnd_not_active), Toast.LENGTH_SHORT).show();
        }
    }
}
