package com.ibndev.icebrowser.browserparts.top;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.view.View;

import com.ibndev.icebrowser.R;
import com.ibndev.icebrowser.browserparts.top.tab.TabManager;
import com.ibndev.icebrowser.browserparts.top.tab.setup.TabRecyclerView;

import java.util.List;

public class TopBar {
    @SuppressLint("NotifyDataSetChanged")
    public TopBar(Activity activity, TabManager tabManager, TabRecyclerView recyclerView, List<String> tabTitles, List<Bitmap> tabFavicon, TopPopupMenu menuHelper) {
        activity.findViewById(R.id.main_top_navbar_menu_button).setOnClickListener(view ->
                menuHelper.showPopupMenu(view, tabManager.getCurrentWebView().getUrl(), tabManager.getCurrentWebView().getTitle()));

        activity.findViewById(R.id.main_top_navbar_open_tabs).setOnClickListener(view -> {
            tabTitles.clear();
            for (int i = 0; i < tabManager.tabs.size(); i++) {
                tabTitles.add(tabManager.tabs.get(i).webview.getTitle());
            }

            tabFavicon.clear();
            for (int i = 0; i < tabManager.tabs.size(); i++){
                tabFavicon.add(tabManager.tabs.get(i).webview.getFavicon());
            }
            recyclerView.adapter.notifyDataSetChanged();
            activity.findViewById(R.id.main_tabs_layout).setVisibility(View.VISIBLE);
        });
    }
}
