package com.ibndev.icebrowser.browserparts.bottom.sheet.settings;

import android.webkit.CookieManager;
import android.webkit.WebStorage;
import android.webkit.WebView;

import com.ibndev.icebrowser.browserparts.top.tab.TabManager;

public class ClearBrowserData {

    TabManager tabManager;

    public ClearBrowserData(TabManager tabManager) {
        this.tabManager = tabManager;
    }

    public void clearHistoryCache() {
        WebView v = tabManager.getCurrentWebView();
        v.clearCache(true);
        v.clearFormData();
        v.clearHistory();
        CookieManager.getInstance().removeAllCookies(null);
        WebStorage.getInstance().deleteAllData();
    }
}
