package com.ibndev.icebrowser.browserparts.bottombar.bottomsheetmenu;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.ColorStateList;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.CookieManager;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.ibndev.icebrowser.R;
import com.ibndev.icebrowser.browserparts.settings.SettingsSheet;
import com.ibndev.icebrowser.browserparts.topbar.tab.TabManager;
import com.ibndev.icebrowser.browserparts.utilities.ShowAndHideKeyboard;

import java.util.Objects;

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

        bottomSheetView.findViewById(R.id.main_bottomsheet_menu_close).setOnClickListener(view -> bottomSheetDialog.dismiss());

        setModeButton(bottomSheetView);

        bottomSheetView.findViewById(R.id.main_bottomsheet_menu_open_bookmark).setOnClickListener(view -> {
            functions.showBookmarks();
            bottomSheetDialog.dismiss();
        });


        setCookieButton(bottomSheetView);

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

        bottomSheetView.findViewById(R.id.main_bottomsheet_menu_rate).setOnClickListener(view -> bottomSheetDialog.dismiss());

        bottomSheetView.findViewById(R.id.main_bottomsheet_menu_exit_app).setOnClickListener(view -> {
            activity.finishAffinity();
            System.exit(0);
        });

        bottomSheetDialog.show();
    }

    private void setModeButton(View bottomSheetView) {
        ConstraintLayout layout = bottomSheetView.findViewById(R.id.main_bottomsheet_menu_mode);
        boolean isDesktopUA = Objects.equals(tabManager.getCurrentWebView().getSettings().getUserAgentString(), activity.getString(R.string.desktopUA));
        if (isDesktopUA) {
            layout.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(activity, R.color.background_active)));
        } else {
            layout.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(activity, R.color.background_normal)));
        }

        bottomSheetView.findViewById(R.id.main_bottomsheet_menu_mode).setOnClickListener(view -> {
            functions.switchUA(isDesktopUA);
            setModeButton(bottomSheetView);
        });
    }


    private void setCookieButton(View bottomSheetView) {
        ConstraintLayout layout = bottomSheetView.findViewById(R.id.main_bottomsheet_menu_cookie);

        CookieManager cookieManager = CookieManager.getInstance();
        boolean isCookieActive = cookieManager.acceptThirdPartyCookies(tabManager.getCurrentWebView());

        if (isCookieActive) {
            layout.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(activity, R.color.background_active)));
        } else {
            layout.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(activity, R.color.background_normal)));
        }

        bottomSheetView.findViewById(R.id.main_bottomsheet_menu_cookie).setOnClickListener(view -> {
            functions.cookie(cookieManager, isCookieActive);
            setCookieButton(bottomSheetView);
        });
    }
}