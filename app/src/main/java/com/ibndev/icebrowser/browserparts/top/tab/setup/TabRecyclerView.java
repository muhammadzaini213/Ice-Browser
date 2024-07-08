package com.ibndev.icebrowser.browserparts.top.tab.setup;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ibndev.icebrowser.R;
import com.ibndev.icebrowser.browserparts.top.tab.TabManager;

import java.util.List;

public class TabRecyclerView {

    public TabsAdapter adapter;

    @SuppressLint("NotifyDataSetChanged")
    public TabRecyclerView(Activity activity, TabManager tabManager, List<String> tabTitles, List<Bitmap> tabFavicon) {
        RecyclerView recyclerView = activity.findViewById(R.id.tabs_list);

        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        adapter = new TabsAdapter(activity, tabManager, tabTitles, v -> {
            int position = (int) v.getTag();
            tabManager.switchToTab(position);
            activity.findViewById(R.id.main_tabs_layout).setVisibility(View.GONE);
        }, v -> {
            int position = (int) v.getTag();
            tabManager.switchToTab(position);

            if (tabTitles.size() == 1) {
                activity.findViewById(R.id.main_tabs_layout).setVisibility(View.GONE);
            }
            if (position >= 0 && position < tabTitles.size()) {
                tabTitles.remove(position);
                adapter.notifyItemRemoved(position);
                adapter.notifyDataSetChanged();
            }
            tabManager.closeCurrentTab();
        });
        recyclerView.setAdapter(adapter);
    }
}
