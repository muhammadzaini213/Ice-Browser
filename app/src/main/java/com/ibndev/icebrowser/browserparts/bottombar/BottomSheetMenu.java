package com.ibndev.icebrowser.browserparts.bottombar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.CookieManager;
import android.widget.ImageView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.ibndev.icebrowser.R;
import com.ibndev.icebrowser.browserparts.settings.SettingsSheet;
import com.ibndev.icebrowser.browserparts.topbar.tab.TabManager;
import com.ibndev.icebrowser.browserparts.utilities.ShowAndHideKeyboard;

public class BottomSheetMenu {
    Activity activity;
    TabManager tabManager;
    BottomSheetMenuFunctions functions;
    ShowAndHideKeyboard showAndHideKeyboard;
    SettingsSheet settingsSheet;
    BottomSheetDialog bottomSheetDialog;
    View bottomSheetView;

    @SuppressLint("InflateParams")
    public BottomSheetMenu(Activity activity, TabManager tabManager, SQLiteDatabase bookmarkDatabase) {
        this.activity = activity;
        this.tabManager = tabManager;
        showAndHideKeyboard = new ShowAndHideKeyboard(activity);
        functions = new BottomSheetMenuFunctions(activity, tabManager, bookmarkDatabase);
        settingsSheet = new SettingsSheet(activity, tabManager, bookmarkDatabase);

        bottomSheetDialog = new BottomSheetDialog(activity);
                bottomSheetView = LayoutInflater.from(activity).inflate(
                R.layout.activity_main_bottomsheet_menu,
                null
        );
        bottomSheetDialog.setContentView(bottomSheetView);

    }

    public void show() {




        setModeButton(bottomSheetView, bottomSheetDialog);

        bottomSheetView.findViewById(R.id.main_bottomsheet_menu_open_bookmark).setOnClickListener(view -> {
            functions.showBookmarks();
            bottomSheetDialog.dismiss();
        });


        setCookieButton(bottomSheetView, bottomSheetDialog);


        bottomSheetView.findViewById(R.id.main_bottomsheet_menu_share).setOnClickListener(view -> {
            functions.share();
            bottomSheetDialog.dismiss();
        });

        bottomSheetView.findViewById(R.id.main_bottomsheet_menu_find).setOnClickListener(view -> {
            functions.find(showAndHideKeyboard);
            bottomSheetDialog.dismiss();
        });

        bottomSheetView.findViewById(R.id.main_bottomsheet_menu_overlay).setOnClickListener(view ->
                bottomSheetDialog.dismiss());

        bottomSheetView.findViewById(R.id.main_bottomsheet_menu_tab_info).setOnClickListener(view -> {
            functions.tabInfo();
            bottomSheetDialog.dismiss();
        });

        bottomSheetView.findViewById(R.id.main_bottomsheet_menu_settings).setOnClickListener(view -> {
            settingsSheet.show();
            bottomSheetDialog.dismiss();
        });

        bottomSheetView.findViewById(R.id.main_bottomsheet_menu_rate).setOnClickListener(view -> {
           bottomSheetDialog.dismiss();
        });

        bottomSheetView.findViewById(R.id.main_bottomsheet_menu_exit_app).setOnClickListener(view -> {
            activity.finishAffinity();
            System.exit(0);
        });

        bottomSheetDialog.show();
    }

    private void setModeButton(View bottomSheetView, BottomSheetDialog bottomSheetDialog) {
        TabManager.Tab tab = tabManager.getCurrentTab();
        tab.isDesktopUA = !tab.isDesktopUA;
        ImageView icon = bottomSheetView.findViewById(R.id.main_bottomsheet_menu_mode_icon);
        if (tab.isDesktopUA) {
            icon.setImageResource((R.drawable.bottomsheet_menu_mode_android_icon));
        } else {
            icon.setImageResource(R.drawable.bottomsheet_menu_mode_desktop_icon);
        }

        bottomSheetView.findViewById(R.id.main_bottomsheet_menu_mode).setOnClickListener(view -> {
            functions.switchUA(tab);
            bottomSheetDialog.dismiss();
        });
    }


    private void setCookieButton(View bottomSheetView, BottomSheetDialog bottomSheetDialog) {
        ImageView icon = bottomSheetView.findViewById(R.id.main_bottomsheet_menu_cookie_icon);
        CookieManager cookieManager = CookieManager.getInstance();
        boolean isCookieActive = cookieManager.acceptThirdPartyCookies(tabManager.getCurrentWebView());

        if (isCookieActive) {
            icon.setImageResource(R.drawable.bottomsheet_menu_cookie_active_icon);
        } else {
            icon.setImageResource(R.drawable.bottomsheet_menu_cookie_nonactive_icon);
        }

        bottomSheetView.findViewById(R.id.main_bottomsheet_menu_cookie).setOnClickListener(view -> {
            functions.cookie(cookieManager, isCookieActive);

            bottomSheetDialog.dismiss();
        });
    }
}
