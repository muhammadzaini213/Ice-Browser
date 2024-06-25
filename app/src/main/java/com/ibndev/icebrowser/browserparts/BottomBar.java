package com.ibndev.icebrowser.browserparts;

import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.view.View;

import com.ibndev.icebrowser.R;

public class BottomBar {
    public BottomBar(Activity activity, TabManager tabManager, SQLiteDatabase placesDb) {
        ShowAndHideKeyboard showAndHideKeyboard = new ShowAndHideKeyboard(activity);
        BottomSheetMenu bottomSheetMenu = new BottomSheetMenu();

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

        bottom_bar.findViewById(R.id.menu_btn).setOnClickListener(view -> bottomSheetMenu.show(activity, tabManager, showAndHideKeyboard, placesDb));
    }

}
