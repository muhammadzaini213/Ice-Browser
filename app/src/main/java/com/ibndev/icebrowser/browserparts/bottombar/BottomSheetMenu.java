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
import com.ibndev.icebrowser.browserparts.topbar.tab.TabManager;
import com.ibndev.icebrowser.browserparts.utilities.ShowAndHideKeyboard;

public class BottomSheetMenu {
    Activity activity;
    TabManager tabManager;
    BottomSheetFunctions functions;
    ShowAndHideKeyboard showAndHideKeyboard;

    public BottomSheetMenu(Activity activity, TabManager tabManager, SQLiteDatabase placesDB) {
        this.activity = activity;
        this.tabManager = tabManager;
        showAndHideKeyboard = new ShowAndHideKeyboard(activity);
        functions = new BottomSheetFunctions(activity, tabManager, placesDB);
    }

    public void show() {

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(activity);
        @SuppressLint("InflateParams") View bottomSheetView = LayoutInflater.from(activity).inflate(
                R.layout.bottom_sheet_menu,
                null
        );


        setModeButton(bottomSheetView, bottomSheetDialog);

        bottomSheetView.findViewById(R.id.bookmark).setOnClickListener(view -> {
            functions.showBookmarks();
            bottomSheetDialog.dismiss();
        });



        setCookieButton(bottomSheetView, bottomSheetDialog);


        bottomSheetView.findViewById(R.id.share).setOnClickListener(view -> {
            functions.share();
            bottomSheetDialog.dismiss();
        });

        bottomSheetView.findViewById(R.id.find).setOnClickListener(view -> {
            functions.find(showAndHideKeyboard);
            bottomSheetDialog.dismiss();
        });

        bottomSheetView.findViewById(R.id.overlay).setOnClickListener(view -> {
            bottomSheetDialog.dismiss();
        });

        bottomSheetView.findViewById(R.id.tab_info).setOnClickListener(view -> {
            functions.tabInfo();
            bottomSheetDialog.dismiss();
        });

        bottomSheetView.findViewById(R.id.exit).setOnClickListener(view -> {
            activity.finishAffinity();
            System.exit(0);
        });

        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }

    private void setModeButton(View bottomSheetView, BottomSheetDialog bottomSheetDialog) {
        TabManager.Tab tab = tabManager.getCurrentTab();
        tab.isDesktopUA = !tab.isDesktopUA;
        ImageView icon = bottomSheetView.findViewById(R.id.mode_img);
        if (tab.isDesktopUA) {
            icon.setImageResource((R.drawable.menu_android));
        } else {
            icon.setImageResource(R.drawable.menu_dekstop);
        }

        bottomSheetView.findViewById(R.id.desktop).setOnClickListener(view -> {
            functions.switchUA(tab);
            bottomSheetDialog.dismiss();
        });
    }


    private void setCookieButton(View bottomSheetView, BottomSheetDialog bottomSheetDialog) {
        ImageView icon = bottomSheetView.findViewById(R.id.cookie_img);
        CookieManager cookieManager = CookieManager.getInstance();
        boolean isCookieActive = cookieManager.acceptThirdPartyCookies(tabManager.getCurrentWebView());

        if (isCookieActive){
            icon.setImageResource(R.drawable.menu_cookie);
        } else{
            icon.setImageResource(R.drawable.menu_cookie_nonactive);
        }

        bottomSheetView.findViewById(R.id.cookie).setOnClickListener(view -> {
            functions.cookie(cookieManager, isCookieActive);

            bottomSheetDialog.dismiss();
        });
    }
}
