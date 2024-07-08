package com.ibndev.icebrowser.browserparts.top.tab.setup;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.ibndev.icebrowser.R;
import com.ibndev.icebrowser.browserparts.top.tab.TabManager;

import java.util.List;

public class TabsAdapter extends RecyclerView.Adapter<TabsAdapter.TabViewHolder> {
    private final List<String> tabTitles;
    private final TabManager tabManager;
    private final View.OnClickListener onItemClickListener;
    private final View.OnClickListener onCloseClickListener;
    private final Activity activity;

    public TabsAdapter(Activity activity, TabManager tabManager, List<String> tabTitles, View.OnClickListener onItemClickListener, View.OnClickListener onCloseClickListener) {
        this.tabTitles = tabTitles;
        this.tabManager = tabManager;
        this.onItemClickListener = onItemClickListener;
        this.onCloseClickListener = onCloseClickListener;
        this.activity = activity;
    }

    @NonNull
    @Override
    public TabViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_main_tabs_item_adapter, parent, false);
        return new TabViewHolder(v);
    }

    @Override
    public void onBindViewHolder(TabViewHolder holder, int position) {
        holder.textView.setText(tabTitles.get(position));
        holder.itemView.setTag(position);
        holder.itemView.setOnClickListener(onItemClickListener);
        holder.closeTab.setTag(position);
        holder.closeTab.setOnClickListener(onCloseClickListener);
        holder.favicon.setImageBitmap(tabManager.tabs.get(position).webview.getFavicon());

        if(position == tabManager.currentTabIndex){
            holder.layout.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(activity, R.color.background_active)));
        } else {
            holder.layout.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(activity, R.color.background_normal)));
        }
    }

    @Override
    public int getItemCount() {
        return tabTitles.size();
    }

    public static class TabViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;
        public ImageView closeTab;
        public ImageView favicon;
        public ConstraintLayout layout;

        public TabViewHolder(View v) {
            super(v);
            textView = v.findViewById(R.id.main_tabs_item_adapter_tab_title);
            closeTab = v.findViewById(R.id.main_tabs_item_adapter_tab_close);
            favicon = v.findViewById(R.id.main_tabs_item_adapter_favicon);
            layout = v.findViewById(R.id.main_tabs_item_layout);
        }
    }
}
