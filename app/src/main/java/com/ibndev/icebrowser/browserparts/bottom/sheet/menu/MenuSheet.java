package com.ibndev.icebrowser.browserparts.bottom.sheet.menu;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.CookieManager;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.ibndev.icebrowser.R;
import com.ibndev.icebrowser.browserparts.bottom.dialog.ExitDialog;
import com.ibndev.icebrowser.browserparts.bottom.sheet.settings.SettingsSheet;
import com.ibndev.icebrowser.browserparts.top.tab.TabManager;
import com.ibndev.icebrowser.utilities.ShowAndHideKeyboard;

import java.util.Objects;

public class MenuSheet {
    Activity activity;
    TabManager tabManager;
    MenuSheetFun functions;
    ShowAndHideKeyboard showAndHideKeyboard;
    SettingsSheet settingsSheet;
    BottomSheetDialog bottomSheetDialog;
    View bottomSheetView;


    ExitDialog exitDialog;

    @SuppressLint("InflateParams")
    public MenuSheet(Activity activity, TabManager tabManager) {
        this.activity = activity;
        this.tabManager = tabManager;
        showAndHideKeyboard = new ShowAndHideKeyboard(activity);
        functions = new MenuSheetFun(activity, tabManager);
        settingsSheet = new SettingsSheet(activity, tabManager);


        if (bottomSheetDialog == null) {
            bottomSheetDialog = new BottomSheetDialog(activity);
            bottomSheetView = LayoutInflater.from(activity).inflate(
                    R.layout.activity_main_bottomsheet_menu,
                    null
            );
            bottomSheetDialog.setContentView(bottomSheetView);
        }


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

        bottomSheetView.findViewById(R.id.main_bottomsheet_menu_overlay).setOnClickListener(view -> {
            functions.overlay();
            bottomSheetDialog.dismiss();
        });

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
            if (exitDialog == null) {
                exitDialog = new ExitDialog(activity);
                exitDialog.show();
            } else {
                exitDialog.show();
            }
            bottomSheetDialog.dismiss();
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
