package com.ibndev.icebrowser.floatingparts.navbar.popup.browsermenu;

import android.content.Context;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.PopupMenu;

import com.ibndev.icebrowser.R;
import com.ibndev.icebrowser.floatingparts.FloatingWindow;
import com.ibndev.icebrowser.floatingparts.browser.WindowTabManager;
import com.ibndev.icebrowser.floatingparts.utilities.FloatingUtils;

public class BrowserMenu1 {

    Context context;
    ViewGroup floatView;
    FloatingUtils utils;
    WindowTabManager tabManager;
    WebSettings webSettings;

    boolean desktopActive;
    boolean javascriptActive;
    boolean cssActive;
    boolean cacheActive;
    boolean domActive;

    public BrowserMenu1(FloatingWindow floatingWindow, FloatingUtils utils) {
        context = floatingWindow.getApplicationContext();
        floatView = floatingWindow.floatView;
        this.utils = utils;
    }

    public void showPopupMenu(WindowTabManager tabManager) {
        this.tabManager = tabManager;

        WebView currentWebview = tabManager.getCurrentWebView();
        webSettings = currentWebview.getSettings();

        PopupMenu popupMenu = new PopupMenu(context, floatView.findViewById(R.id.window_main_top_navbar_layout_set_button));
        MenuInflater inflater = popupMenu.getMenuInflater();
        inflater.inflate(R.menu.window_browser_menu_set_1, popupMenu.getMenu());

        initializeMenuChecked(popupMenu);
        popupMenu.setOnMenuItemClickListener(this::onOptionsItemSelected);
        popupMenu.show();
    }


    private void initializeMenuChecked(PopupMenu popupMenu) {
        MenuItem desktopItem = popupMenu.getMenu().findItem(R.id.action_desktop);
        MenuItem javascriptItem = popupMenu.getMenu().findItem(R.id.action_javascript);
        MenuItem cssItem = popupMenu.getMenu().findItem(R.id.action_css);
        MenuItem cacheItem = popupMenu.getMenu().findItem(R.id.action_cache);
        MenuItem domItem = popupMenu.getMenu().findItem(R.id.action_dom);


        String javascript =
                "(function() { " +
                        "    var cssEnabled = false; " +
                        "    document.querySelectorAll('link[rel=\"stylesheet\"]').forEach(function(link) {" +
                        "        if (!link.disabled) { " +
                        "            cssEnabled = true; " +
                        "        } " +
                        "    }); " +
                        "    document.querySelectorAll('style').forEach(function(style) {" +
                        "        if (!style.disabled) { " +
                        "            cssEnabled = true; " +
                        "        } " +
                        "    }); " +
                        "    return cssEnabled; " +
                        "})();";

        // Evaluate JavaScript and handle result
        tabManager.getCurrentWebView().evaluateJavascript(javascript, value -> {
            // 'value' contains the result from JavaScript (true/false)
            cssActive = Boolean.parseBoolean(value);
            cssItem.setChecked(cssActive);
        });

        desktopActive = webSettings.getUserAgentString() == context.getString(R.string.desktopUA);
        javascriptActive = webSettings.getJavaScriptEnabled();
        cacheActive = webSettings.getCacheMode() == WebSettings.LOAD_DEFAULT;
        domActive = webSettings.getDomStorageEnabled();


        desktopItem.setChecked(desktopActive);
        javascriptItem.setChecked(javascriptActive);
        cacheItem.setChecked(cacheActive);
        domItem.setChecked(domActive);
    }

    private boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_desktop) {
            if (desktopActive) {
                tabManager.getCurrentWebView().getSettings().setUserAgentString("");
                tabManager.getCurrentWebView().reload();
            } else {
                tabManager.getCurrentWebView().getSettings().setUserAgentString(context.getString(R.string.desktopUA));
                tabManager.getCurrentWebView().reload();
            }

        } else if (item.getItemId() == R.id.action_javascript) {
            tabManager.getCurrentWebView().getSettings().setJavaScriptEnabled(!javascriptActive);
        } else if (item.getItemId() == R.id.action_css) {
            if (cssActive) {
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
        } else if (item.getItemId() == R.id.action_cache) {
            if (cacheActive) {
                tabManager.getCurrentWebView().getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
            } else {
                tabManager.getCurrentWebView().getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
            }
        } else if (item.getItemId() == R.id.action_dom) {
            tabManager.getCurrentWebView().getSettings().setDomStorageEnabled(!domActive);
        }
        return true;

    }

}
