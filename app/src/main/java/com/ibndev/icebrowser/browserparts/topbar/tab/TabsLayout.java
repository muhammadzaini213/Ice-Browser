package com.ibndev.icebrowser.browserparts.topbar.tab;

import android.app.Activity;
import android.view.View;

import com.ibndev.icebrowser.R;

public class TabsLayout {
    Activity activity;

    public TabsLayout(Activity activity) {
        this.activity = activity;
    }

    public void tabsLayout(TabManager tabManager) {
        View tabs_layout = activity.findViewById(R.id.tabs_layout);

        tabs_layout.findViewById(R.id.tabs_menu).setOnClickListener(view -> {

        });

        tabs_layout.findViewById(R.id.tabs_new_tab).setOnClickListener(view -> {
            tabManager.newTab("google.com");
            tabManager.switchToTab(tabManager.tabs.size() - 1);
            tabs_layout.setVisibility(View.GONE);
        });

        tabs_layout.findViewById(R.id.close_tabs_menu).setOnClickListener(view -> tabs_layout.setVisibility(View.GONE));
    }

}
