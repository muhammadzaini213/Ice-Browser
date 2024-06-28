package com.ibndev.icebrowser.browserparts.topbar.tab;

import android.app.Activity;
import android.view.View;

import com.ibndev.icebrowser.R;
import com.ibndev.icebrowser.browserparts.bottombar.bottomsheetmore.BottomSheetMore;

public class TabsLayout {
    Activity activity;

    public TabsLayout(Activity activity) {
        this.activity = activity;
    }

    public void tabsLayout(TabManager tabManager) {
        View tabs_layout = activity.findViewById(R.id.main_tabs_layout);
        BottomSheetMore bottomSheetMore = new BottomSheetMore(activity, tabManager);

        tabs_layout.findViewById(R.id.main_tabs_more).setOnClickListener(view -> {
            bottomSheetMore.show();
        });

        tabs_layout.findViewById(R.id.main_tabs_new_tab).setOnClickListener(view -> {
            tabManager.newTab("google.com");
            tabManager.switchToTab(tabManager.tabs.size() - 1);
            tabs_layout.setVisibility(View.GONE);
        });

        tabs_layout.findViewById(R.id.main_tabs_close_menu).setOnClickListener(view -> tabs_layout.setVisibility(View.GONE));
    }

}
