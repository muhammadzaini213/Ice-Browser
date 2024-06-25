package com.ibndev.icebrowser.browserparts.topbar;

import android.app.Activity;
import android.view.View;

import com.ibndev.icebrowser.R;
import com.ibndev.icebrowser.browserparts.topbar.tab.TabManager;
import com.ibndev.icebrowser.browserparts.topbar.tab.TabRecyclerView;

import java.util.List;

public class TopBar {
    public TopBar(Activity activity, TabManager tabManager, TabRecyclerView recyclerView, List<String> tabTitles, PopupMenuHelper menuHelper) {
        activity.findViewById(R.id.more_button).setOnClickListener(view -> {
            menuHelper.showPopupMenu(view, tabManager.getCurrentWebView().getUrl(), tabManager.getCurrentWebView().getTitle());
        });

        activity.findViewById(R.id.tabs_button).setOnClickListener(view -> {
            tabTitles.clear();
            for (int i = 0; i < tabManager.tabs.size(); i++) {
                tabTitles.add(tabManager.tabs.get(i).webview.getTitle());
            }
            recyclerView.adapter.notifyDataSetChanged();
            activity.findViewById(R.id.tabs_layout).setVisibility(View.VISIBLE);
        });
    }
}
