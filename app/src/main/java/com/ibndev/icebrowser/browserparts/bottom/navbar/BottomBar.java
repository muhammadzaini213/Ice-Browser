package com.ibndev.icebrowser.browserparts.bottom.navbar;

import android.app.Activity;
import android.view.View;

import com.ibndev.icebrowser.R;
import com.ibndev.icebrowser.browserparts.bottom.sheet.menu.MenuSheet;
import com.ibndev.icebrowser.browserparts.top.tab.TabManager;

public class BottomBar {
    public BottomBar(Activity activity, TabManager tabManager) {
        MenuSheet menuSheet = new MenuSheet(activity, tabManager);

        View bottom_bar = activity.findViewById(R.id.main_bottom_bar);

        bottom_bar.findViewById(R.id.main_bottom_navbar_prev_btn).setOnClickListener(view -> {
            if (tabManager.getCurrentWebView().canGoBack()) {
                tabManager.getCurrentWebView().goBack();
            }
        });

        bottom_bar.findViewById(R.id.main_bottom_navbar_next_btn).setOnClickListener(view -> {
            if (tabManager.getCurrentWebView().canGoForward()) {
                tabManager.getCurrentWebView().goForward();
            }
        });

        bottom_bar.findViewById(R.id.main_bottom_navbar_home_btn).setOnClickListener(view -> tabManager.getCurrentWebView().loadUrl("https://google.com"));

        bottom_bar.findViewById(R.id.main_bottom_navbar_refresh_btn).setOnClickListener(view -> tabManager.getCurrentWebView().reload());

        bottom_bar.findViewById(R.id.main_bottom_navbar_menu_btn).setOnClickListener(view -> menuSheet.show());
    }

}
