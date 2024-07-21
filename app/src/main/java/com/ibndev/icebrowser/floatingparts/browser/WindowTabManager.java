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

    KeyboardView keyboardView;

    public WindowTabManager(FloatingWindow floatingWindow) {
        this.context = floatingWindow.getApplicationContext();
        this.floatView = floatingWindow.floatView;
        et = floatView.findViewById(R.id.window_main_top_navbar_autocomplete);

        keyboardView = new KeyboardView(floatingWindow, WindowTabManager.this);
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

    @SuppressLint({"SetJavaScriptEnabled", "DefaultLocale", "ClickableViewAccessibility"})
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
                String jsMonitorEvents = "var isEditableElementFocused = false;" +
                        "document.addEventListener('click', function() {" +
                        "    var activeElement = document.activeElement;" +
                        "    isEditableElementFocused = activeElement && (activeElement.tagName === 'INPUT' || activeElement.tagName === 'TEXTAREA');" +
                        "    console.log('Click detected. Is editable element focused:', isEditableElementFocused);" +
                        "});" +
                        "document.addEventListener('focusin', function() {" +
                        "    var activeElement = document.activeElement;" +
                        "    isEditableElementFocused = activeElement && (activeElement.tagName === 'INPUT' || activeElement.tagName === 'TEXTAREA');" +
                        "    console.log('Focus detected. Is editable element focused:', isEditableElementFocused);" +
                        "});";

                getCurrentWebView().evaluateJavascript(jsMonitorEvents, null);

                getCurrentWebView().setOnTouchListener((v, event) -> {
                    String jsCheckFocus = "isEditableElementFocused;";
                    getCurrentWebView().evaluateJavascript(jsCheckFocus, value -> {
                        if (Boolean.parseBoolean(value)) {
                            keyboardView.showKeyboard();
                        } else {
                            keyboardView.hideKeyboard();
                        }
                    });

                    return false;
                });


                String erudaScript = "(function() {" +
                        "var script = document.createElement('script');" +
                        "script.src = 'https://cdn.jsdelivr.net/npm/eruda';" +
                        "document.body.appendChild(script);" +
                        "script.onload = function() {" +
                        "eruda.init();" +
                        "};" +
                        "})();";
//                getCurrentWebView().evaluateJavascript(erudaScript, null);

                String javascript = "javascript:function insertText(text) {\n" +
                        "            var activeElement = document.activeElement;\n" +
                        "            if (activeElement && (activeElement.tagName === 'INPUT' || activeElement.tagName === 'TEXTAREA')) {\n" +
                        "                var start = activeElement.selectionStart;\n" +
                        "                var end = activeElement.selectionEnd;\n" +
                        "                activeElement.value = activeElement.value.substring(0, start) + text + activeElement.value.substring(end);\n" +
                        "                activeElement.setSelectionRange(start + text.length, start + text.length);\n" +
                        "var chatInputBox = document.querySelector('.chat-input-box');\n" +
                        "\n" +
                        "if (chatInputBox) {\n" +
                        "    chatInputBox.setAttribute('modelvalue', text);\n" +
                        "} else {\n" +
                        "    console.error('Element not found');\n" +
                        "}" +
                        "            } else if (activeElement && activeElement.contentEditable === 'true') {\n" +
                        "                var selection = window.getSelection();\n" +
                        "                var range = selection.getRangeAt(0);\n" +
                        "                range.deleteContents();\n" +
                        "                range.insertNode(document.createTextNode(text));\n" +
                        "            }\n" +
                        "        }\n" +
                        "\n" +
                        "        function deleteOneCharacter() {\n" +
                        "            var activeElement = document.activeElement;\n" +
                        "            if (activeElement && (activeElement.tagName === 'INPUT' || activeElement.tagName === 'TEXTAREA')) {\n" +
                        "                var start = activeElement.selectionStart;\n" +
                        "                var end = activeElement.selectionEnd;\n" +
                        "                if (start === end && start > 0) {\n" +
                        "                    activeElement.value = activeElement.value.substring(0, start - 1) + activeElement.value.substring(end);\n" +
                        "                    activeElement.setSelectionRange(start - 1, start - 1);\n" +
                        "                } else if (start !== end) {\n" +
                        "                    activeElement.value = activeElement.value.substring(0, start) + activeElement.value.substring(end);\n" +
                        "                    activeElement.setSelectionRange(start, start);\n" +
                        "var chatInputBox = document.querySelector('.chat-input-box');\n" +
                        "\n" +
                        "if (chatInputBox) {\n" +
                        "    chatInputBox.setAttribute('modelvalue', activeElement.value);\n" +
                        "} else {\n" +
                        "    console.error('Element not found');\n" +
                        "}" +
                        "                }\n" +
                        "            } else if (activeElement && activeElement.contentEditable === 'true') {\n" +
                        "                var selection = window.getSelection();\n" +
                        "                if (selection.rangeCount > 0) {\n" +
                        "                    var range = selection.getRangeAt(0);\n" +
                        "                    if (range.startOffset === range.endOffset && range.startOffset > 0) {\n" +
                        "                        range.setStart(range.startContainer, range.startOffset - 1);\n" +
                        "                        range.deleteContents();\n" +
                        "                    } else {\n" +
                        "                        range.deleteContents();\n" +
                        "                    }\n" +
                        "                }\n" +
                        "            }\n" +
                        "        }\n" +
                        "\n" +
                        "        function deleteAllCharacters() {\n" +
                        "            var activeElement = document.activeElement;\n" +
                        "            if (activeElement && (activeElement.tagName === 'INPUT' || activeElement.tagName === 'TEXTAREA')) {\n" +
                        "                activeElement.value = '';\n" +
                        "var chatInputBox = document.querySelector('.chat-input-box');\n" +
                        "\n" +
                        "// Change the 'modelvalue' attribute\n" +
                        "if (chatInputBox) {\n" +
                        "    chatInputBox.setAttribute('modelvalue', '');\n" +
                        "} else {\n" +
                        "    console.error('Element not found');\n" +
                        "}" +
                        "            } else if (activeElement && activeElement.contentEditable === 'true') {\n" +
                        "                activeElement.innerHTML = '';\n" +
                        "            }\n" +
                        "        } " +
                        "function sendEnterKey() {\n" +
                        "    var activeElement = document.activeElement;\n" +
                        "    if (activeElement) {\n" +
                        "        var eventKeyDown = new KeyboardEvent('keydown', { keyCode: 13, which: 13, key: 'Enter', code: 'Enter' });\n" +
                        "        var eventKeyUp = new KeyboardEvent('keyup', { keyCode: 13, which: 13, key: 'Enter', code: 'Enter' });\n" +
                        "\n" +
                        "        activeElement.dispatchEvent(eventKeyDown);\n" +
                        "        activeElement.dispatchEvent(eventKeyUp);\n" +
                        "\n" +
                        "        // Check if the active element is a form element and submit it\n" +
                        "        if (activeElement.tagName === 'INPUT' || activeElement.tagName === 'TEXTAREA') {\n" +
                        "            var form = activeElement.closest('form');\n" +
                        "            if (form) {\n" +
                        "                form.submit();\n" +
                        "            }\n" +
                        "        }\n" +
                        "    }\n" +
                        "}\n";

                getCurrentWebView().evaluateJavascript(javascript, null);


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
