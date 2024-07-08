package com.ibndev.icebrowser.floatingparts.navbar.popup.browsermenu;

import android.content.Context;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import com.ibndev.icebrowser.R;
import com.ibndev.icebrowser.floatingparts.FloatingWindow;
import com.ibndev.icebrowser.floatingparts.browser.WindowTabManager;
import com.ibndev.icebrowser.floatingparts.utilities.FloatingUtils;


public class TabsMenu {

    Context context;
    ViewGroup floatView;
    FloatingUtils utils;


    public TabsMenu(FloatingWindow floatingWindow, FloatingUtils utils) {
        context = floatingWindow.getApplicationContext();
        floatView = floatingWindow.floatView;
        this.utils = utils;
    }

    // Method to show the popup menu
    public void showTabs(WindowTabManager tabManager) {

        PopupMenu popupMenu = new PopupMenu(context, floatView.findViewById(R.id.window_main_top_navbar_layout_set_button));
        Menu menu = popupMenu.getMenu();

        menu.add(Menu.NONE, 123456, Menu.NONE, context.getString(R.string.main_tabs_new_tab_title));

        for (int i = 0; i < tabManager.tabs.size(); i++) {
            MenuItem item = menu.add(Menu.NONE, i, Menu.NONE, tabManager.tabs.get(i).webview.getTitle())
                    .setCheckable(true);
            if (i == tabManager.currentTabIndex) {
                item.setChecked(true);
            }
        }


        menu.add(Menu.NONE, 654321, Menu.NONE, context.getString(R.string.main_tabs_close_tab));

        // Set item click listener
        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == 123456) {
                // Handle "New tab" action
                tabManager.newTab("google.com");
                tabManager.switchToTab(tabManager.tabs.size() - 1);
            } else if (itemId == 654321) {
                tabManager.closeCurrentTab();
            } else {
                // Handle tab switch
                int position = item.getItemId(); // Adjust position based on the ID offset
                tabManager.switchToTab(position);
            }
            return true;
        });

        // Show the popup menu
        popupMenu.show();
    }
}
