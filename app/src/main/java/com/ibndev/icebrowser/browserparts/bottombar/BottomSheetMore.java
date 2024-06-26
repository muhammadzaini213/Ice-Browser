package com.ibndev.icebrowser.browserparts.bottombar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.ibndev.icebrowser.R;
import com.ibndev.icebrowser.browserparts.topbar.tab.TabManager;

public class BottomSheetMore {
    Activity activity;
    TabManager tabManager;
    BottomSheetDialog bottomSheetDialog;
    View bottomSheetView;

    @SuppressLint("InflateParams")
    public BottomSheetMore(Activity activity, TabManager tabManager) {
        this.activity = activity;
        this.tabManager = tabManager;

        bottomSheetDialog = new BottomSheetDialog(activity);
        bottomSheetView = LayoutInflater.from(activity).inflate(
                R.layout.activity_main_tabs_more,
                null
        );

    }

    public void show() {
        bottomSheetView.findViewById(R.id.main_tabs_more_bottomsheet_toggle_javascript).setOnClickListener(view -> {});
        bottomSheetView.findViewById(R.id.main_tabs_more_bottomsheet_toggle_cache).setOnClickListener(view -> {});
        bottomSheetView.findViewById(R.id.main_tabs_more_bottomsheet_apply).setOnClickListener(view -> {});

        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }
}
