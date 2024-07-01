package com.ibndev.icebrowser.browserparts.top.tab.more;

import android.app.Activity;
import android.webkit.WebSettings;

import com.ibndev.icebrowser.browserparts.top.tab.TabManager;

public class MoreSheetFun {
    Activity activity;
    TabManager tabManager;

    public MoreSheetFun(Activity activity, TabManager tabManager) {
        this.activity = activity;
        this.tabManager = tabManager;
    }

    public void toggleJavascript(boolean isActive) {
        tabManager.getCurrentWebView().getSettings().setJavaScriptEnabled(!isActive);
    }

    public void toggleCSS(boolean isActive) {
        if (isActive) {
            String disableCSS = "    document.querySelectorAll('link[rel=\"stylesheet\"]').forEach(link => {\n" +
                    "        link.disabled = true;\n" +
                    "    });\n" +
                    "\n" +
                    "    document.querySelectorAll('style').forEach(style => {\n" +
                    "        style.disabled = true;\n" +
                    "    });\n" +
                    "\n" +
                    "    console.log('CSS disabled.');\n";
            tabManager.getCurrentWebView().evaluateJavascript(disableCSS, null);
        } else {
            String enableCSS = "    document.querySelectorAll('link[rel=\"stylesheet\"]').forEach(link => {\n" +
                    "        link.disabled = false;\n" +
                    "    });\n" +
                    "\n" +
                    "    document.querySelectorAll('style').forEach(style => {\n" +
                    "        style.disabled = false;\n" +
                    "    });\n" +
                    "\n" +
                    "    console.log('CSS enabled.');\n";
            tabManager.getCurrentWebView().evaluateJavascript(enableCSS, null);
        }
    }


    public void toggleCache(boolean isActive) {
        if (isActive) {
            tabManager.getCurrentWebView().getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        } else {
            tabManager.getCurrentWebView().getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        }
    }

    public void toggleDomStorage(boolean isActive) {
        tabManager.getCurrentWebView().getSettings().setDomStorageEnabled(!isActive);
    }

    public void toggleOverview(boolean isActive) {
        tabManager.getCurrentWebView().getSettings().setUseWideViewPort(!isActive);
    }

    public void toggleZoom(boolean isActive) {
        tabManager.getCurrentWebView().getSettings().setSupportZoom(!isActive);
    }

    public void toggleImage(boolean isActive) {
        tabManager.getCurrentWebView().getSettings().setLoadsImagesAutomatically(!isActive);
    }

    public void toggleLoader(boolean isActive) {
        tabManager.getCurrentWebView().getSettings().setBlockNetworkLoads(isActive);
    }

    public void togglePopup(boolean isActive) {
        tabManager.getCurrentWebView().getSettings().setSupportMultipleWindows(!isActive);
        tabManager.getCurrentWebView().getSettings().setJavaScriptCanOpenWindowsAutomatically(!isActive);
    }

    public void toggleSmallText(boolean isActive) {
        if (!isActive) {
            tabManager.getCurrentWebView().getSettings().setTextZoom(70);
        }
    }

    public void toggleNormalText(boolean isActive) {
        if (!isActive) {
            tabManager.getCurrentWebView().getSettings().setTextZoom(100);
        }
    }

    public void toggleBigText(boolean isActive) {
        if (!isActive) {
            tabManager.getCurrentWebView().getSettings().setTextZoom(130);
        }
    }
}
