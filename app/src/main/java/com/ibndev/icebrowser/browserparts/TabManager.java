package com.ibndev.icebrowser.browserparts;

import android.view.View;
import android.webkit.WebView;

import java.util.ArrayList;

public class TabManager {
    public static class Tab {
        WebView webview;
        boolean isDesktopUA;

        public Tab(WebView w) {
            this.webview = w;
        }
    }

    private final ArrayList<Tab> tabs = new ArrayList<>();
    private int currentTabIndex;

    public Tab getCurrentTab() {
        return tabs.get(currentTabIndex);
    }

    public WebView getCurrentWebView() {
        return getCurrentTab().webview;
    }

    public void addTab(Tab tab) {
        tabs.add(tab);
    }

    public void setCurrentTabIndex(int index) {
        if (index >= 0 && index < tabs.size()) {
            currentTabIndex = index;
        } else {
            throw new IndexOutOfBoundsException("Invalid tab index");
        }
    }

    public void switchToTab(int tab) {
        if (tab >= 0 && tab < tabs.size()) {
            getCurrentWebView().setVisibility(View.GONE);
            currentTabIndex = tab;
            getCurrentWebView().setVisibility(View.VISIBLE);
            getCurrentWebView().requestFocus();
        } else {
            throw new IndexOutOfBoundsException("Invalid tab index");
        }
    }
}
