package com.ibndev.icebrowser.floatingparts.browser;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Patterns;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.HttpAuthHandler;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AutoCompleteTextView;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ibndev.icebrowser.R;
import com.ibndev.icebrowser.floatingparts.FloatingWindow;

import java.util.ArrayList;

public class WindowTabManager {
    public final ArrayList<WindowTabManager.Tab> tabs = new ArrayList<>();
    private final ArrayList<WindowTabManager.TitleAndBundle> closedTabs = new ArrayList<>();
    public int currentTabIndex;
    AutoCompleteTextView et;
    Context context;
    ViewGroup floatView;

    public WindowTabManager(FloatingWindow floatingWindow) {
        this.context = floatingWindow.getApplicationContext();
        this.floatView = floatingWindow.floatView;
        et = floatView.findViewById(R.id.window_main_top_navbar_autocomplete);
    }

    public WindowTabManager.Tab getCurrentTab() {
        return tabs.get(currentTabIndex);
    }

    public WebView getCurrentWebView() {
        return getCurrentTab().webview;
    }

    public void switchToTab(int tab) {
        if (tab >= 0 && tab < tabs.size()) {
            getCurrentWebView().setVisibility(View.GONE);
            currentTabIndex = tab;
            getCurrentWebView().setVisibility(View.VISIBLE);
            getCurrentWebView().requestFocus();
            et.setText(getCurrentWebView().getUrl());
        } else {
            throw new IndexOutOfBoundsException("Invalid tab index");
        }
    }

    private void newTabCommon(WebView webview) {
        final FrameLayout webview_framelayout = floatView.findViewById(R.id.window_main_framelayout_webview);
        boolean isDesktopUA = !tabs.isEmpty() && getCurrentTab().isDesktopUA;
        webview.getSettings().setUserAgentString(isDesktopUA ? context.getString(R.string.desktopUA) : null);
        webview.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        webview.setVisibility(View.GONE);
        WindowTabManager.Tab tab = new WindowTabManager.Tab(webview);
        tab.isDesktopUA = isDesktopUA;
        tabs.add(tab);
        webview_framelayout.addView(webview);
    }

    public void loadUrl(String url, WebView webview) {
        url = url.trim();
        if (url.isEmpty()) {
            url = "about:blank";
        }
        if (url.startsWith("about:") || url.startsWith("javascript:") || url.startsWith("file:") || url.startsWith("data:") ||
                (url.indexOf(' ') == -1 && Patterns.WEB_URL.matcher(url).matches())) {
            int indexOfHash = url.indexOf('#');
            String guess = URLUtil.guessUrl(url);
            if (indexOfHash != -1 && guess.indexOf('#') == -1) {
                // Hash exists in original URL but no hash in guessed URL
                url = guess + url.substring(indexOfHash);
            } else {
                url = guess;
            }
        } else {
            url = URLUtil.composeSearchUrl(url, "https://www.google.com/search?q=%s", "%s");
        }
        webview.loadUrl(url);

    }

    public void newTab(String url) {
        WebView webview = createWebView(null, false);
        newTabCommon(webview);
        loadUrl(url, webview);
    }

    @SuppressLint({"SetJavaScriptEnabled", "DefaultLocale"})
    public WebView createWebView(Bundle bundle, boolean isNightMode) {
        final ProgressBar progressBar = floatView.findViewById(R.id.window_main_progressbar);
        final AutoCompleteTextView et = floatView.findViewById(R.id.window_main_top_navbar_autocomplete);

        WebView webview = new WebView(context);
        if (bundle != null) {
            webview.restoreState(bundle);
        }
        WebSettings settings = webview.getSettings();
        webview.setBackgroundColor(isNightMode ? Color.BLACK : Color.WHITE);
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
        settings.setAllowUniversalAccessFromFileURLs(true);
        settings.setJavaScriptEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setDomStorageEnabled(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setSupportMultipleWindows(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        webview.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                if (newProgress == 100) {
                    progressBar.setVisibility(View.GONE);
                } else {
                    progressBar.setProgress(newProgress);
                }
            }

            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
            }

            @Override
            public void onHideCustomView() {
            }

        });
        webview.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                progressBar.setProgress(0);
                progressBar.setVisibility(View.VISIBLE);
                if (view == getCurrentWebView()) {
                    et.setText(url);
                    et.setSelection(0);
                    view.requestFocus();
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {

            }

            @Override
            public void onReceivedHttpAuthRequest(WebView view, final HttpAuthHandler handler, String host, String realm) {

            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {

                return super.shouldInterceptRequest(view, request);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("intent://")) {
                    int start = url.indexOf(";S.browser_fallback_url=");
                    if (start != -1) {
                        start += ";S.browser_fallback_url=".length();
                        int end = url.indexOf(';', start);
                        if (end != -1 && end != start) {
                            url = url.substring(start, end);
                            url = Uri.decode(url);
                            view.loadUrl(url);
                            return true;
                        }
                    }
                }
                return false;
            }

            @Override
            public void onLoadResource(WebView view, String url) {

            }

        });
        webview.setOnLongClickListener(v -> {
            String url = null, imageUrl = null;
            WebView.HitTestResult r = ((WebView) v).getHitTestResult();
            switch (r.getType()) {
                case WebView.HitTestResult.SRC_ANCHOR_TYPE:
                    url = r.getExtra();
                    break;
                case WebView.HitTestResult.IMAGE_TYPE:
                    imageUrl = r.getExtra();
                    break;
                case WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE:
                case WebView.HitTestResult.EMAIL_TYPE:
                case WebView.HitTestResult.UNKNOWN_TYPE:
                    Handler handler = new Handler();
                    Message message = handler.obtainMessage();
                    ((WebView) v).requestFocusNodeHref(message);
                    url = message.getData().getString("url");
                    if ("".equals(url)) {
                        url = null;
                    }
                    imageUrl = message.getData().getString("src");
                    if ("".equals(imageUrl)) {
                        imageUrl = null;
                    }
                    if (url == null && imageUrl == null) {
                        return false;
                    }
                    break;
                default:
                    return false;
            }
//            showLongPressMenu(url, imageUrl);
            return true;
        });
        webview.setFindListener((activeMatchOrdinal, numberOfMatches, isDoneCounting) -> {
            TextView searchCount = floatView.findViewById(R.id.window_main_top_navbar_search_count);
            searchCount.setText(numberOfMatches == 0 ? context.getString(R.string.not_found) :
                    String.format("%d / %d", activeMatchOrdinal + 1, numberOfMatches));
        });

        return webview;
    }

    private void showLongPressMenu(String linkUrl, String imageUrl) {

    }

    public void closeCurrentTab() {
        if (getCurrentWebView().getUrl() != null && !getCurrentWebView().getUrl().equals("google.com")) {
            WindowTabManager.TitleAndBundle titleAndBundle = new WindowTabManager.TitleAndBundle();
            titleAndBundle.title = getCurrentWebView().getTitle();
            titleAndBundle.bundle = new Bundle();
            getCurrentWebView().saveState(titleAndBundle.bundle);
            closedTabs.add(0, titleAndBundle);
            if (closedTabs.size() > 500) {
                closedTabs.remove(closedTabs.size() - 1);
            }
        }
        ((FrameLayout) floatView.findViewById(R.id.window_main_framelayout_webview)).removeView(getCurrentWebView());
        getCurrentWebView().destroy();
        tabs.remove(currentTabIndex);
        if (currentTabIndex >= tabs.size()) {
            currentTabIndex = tabs.size() - 1;
        }
        if (currentTabIndex == -1) {
            // We just closed the last tab
            newTab("https://google.com");
            currentTabIndex = 0;
        }
        getCurrentWebView().setVisibility(View.VISIBLE);
        et.setText(getCurrentWebView().getUrl());
        getCurrentWebView().requestFocus();
    }

    public static class Tab {
        public WebView webview;
        public boolean isDesktopUA;

        public Tab(WebView w) {
            this.webview = w;
        }
    }

    static class TitleAndBundle {
        String title;
        Bundle bundle;
    }
}