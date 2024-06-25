package com.ibndev.icebrowser.browserparts.bottombar;

import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.view.View;

import com.ibndev.icebrowser.R;
import com.ibndev.icebrowser.browserparts.topbar.tab.TabManager;

public class BottomBar {
    public BottomBar(Activity activity, TabManager tabManager, SQLiteDatabase placesDb) {
        BottomSheetMenu bottomSheetMenu = new BottomSheetMenu(activity, tabManager, placesDb);

        View bottom_bar = activity.findViewById(R.id.bottom_bar);

        bottom_bar.findViewById(R.id.back_btn).setOnClickListener(view -> {
            if (tabManager.getCurrentWebView().canGoBack()) {
                tabManager.getCurrentWebView().goBack();
            }
        });

        bottom_bar.findViewById(R.id.forward_btn).setOnClickListener(view -> {
            if (tabManager.getCurrentWebView().canGoForward()) {
                tabManager.getCurrentWebView().goForward();
            }
        });

        bottom_bar.findViewById(R.id.home_btn).setOnClickListener(view -> tabManager.getCurrentWebView().loadUrl("https://google.com"));

        bottom_bar.findViewById(R.id.refresh_btn).setOnClickListener(view -> tabManager.getCurrentWebView().reload());

        bottom_bar.findViewById(R.id.menu_btn).setOnClickListener(view -> bottomSheetMenu.show());
    }

}
