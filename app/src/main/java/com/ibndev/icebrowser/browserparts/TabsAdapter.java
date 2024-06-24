package com.ibndev.icebrowser.browserparts;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import com.ibndev.icebrowser.R;

import java.util.List;

public class TabsAdapter extends RecyclerView.Adapter<TabsAdapter.TabViewHolder> {
    private List<String> tabTitles;
    private View.OnClickListener onItemClickListener;
    private View.OnClickListener onCloseClickListener;

    public static class TabViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;
        public ImageView closeTab;
        public ImageView favicon;
        public TabViewHolder(View v) {
            super(v);
            textView = v.findViewById(R.id.tab_title);
            closeTab = v.findViewById(R.id.close_this_tab);
            favicon = v.findViewById(R.id.favicon);
        }
    }

    public TabsAdapter(List<String> tabTitles, View.OnClickListener onItemClickListener, View.OnClickListener onCloseClickListener) {
        this.tabTitles = tabTitles;
        this.onItemClickListener = onItemClickListener;
        this.onCloseClickListener = onCloseClickListener;
    }

    @Override
    public TabViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tab, parent, false);
        return new TabViewHolder(v);
    }

    @Override
    public void onBindViewHolder(TabViewHolder holder, int position) {
        holder.textView.setText(tabTitles.get(position));
        holder.itemView.setTag(position);
        holder.itemView.setOnClickListener(onItemClickListener);
        holder.closeTab.setTag(position);
        holder.closeTab.setOnClickListener(onCloseClickListener);

    }

    @Override
    public int getItemCount() {
        return tabTitles.size();
    }
}
