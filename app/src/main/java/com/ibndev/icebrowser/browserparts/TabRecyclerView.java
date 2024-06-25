package com.ibndev.icebrowser.browserparts;

import android.app.Activity;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ibndev.icebrowser.R;

import java.util.List;

public class TabRecyclerView {

    public TabsAdapter adapter;

    public TabRecyclerView(Activity activity, TabManager tabManager, List<String> tabTitles) {
        RecyclerView recyclerView = activity.findViewById(R.id.tabs_list);

        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        adapter = new TabsAdapter(tabTitles, v -> {
            int position = (int) v.getTag();
            tabManager.switchToTab(position);
            activity.findViewById(R.id.tabs_layout).setVisibility(View.GONE);
        }, v -> {
            int position = (int) v.getTag();
            tabManager.switchToTab(position);

            if (tabTitles.size() == 1) {
                activity.findViewById(R.id.tabs_layout).setVisibility(View.GONE);
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
