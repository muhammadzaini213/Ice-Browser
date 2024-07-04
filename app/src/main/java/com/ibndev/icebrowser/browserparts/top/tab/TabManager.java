package com.ibndev.icebrowser.browserparts.top.tab;

import static android.content.Context.CLIPBOARD_SERVICE;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Patterns;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.HttpAuthHandler;
import android.webkit.SslErrorHandler;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
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
import android.widget.Toast;

import com.ibndev.icebrowser.MainBrowserActivity;
import com.ibndev.icebrowser.R;
import com.ibndev.icebrowser.setup.permission.PermissionCodes;
import com.ibndev.icebrowser.utilities.DownloadHelper;
import com.ibndev.icebrowser.utilities.ShowAndHideKeyboard;
import com.ibndev.icebrowser.utilities.WebCertificate;

import java.util.ArrayList;

public class TabManager {
    public final WebChromeClient.CustomViewCallback[] fullScreenCallback = new WebChromeClient.CustomViewCallback[1];
    public final ArrayList<Tab> tabs = new ArrayList<>();
    final MainBrowserActivity activity;
    final ShowAndHideKeyboard showAndHideKeyboard;
    final DownloadHelper downloadHelper;
    final View[] fullScreenView = new View[1];
    private final ArrayList<TitleAndBundle> closedTabs = new ArrayList<>();
    public int currentTabIndex;
    ValueCallback<Uri[]> fileUploadCallback;
    AutoCompleteTextView et;


    public TabManager(MainBrowserActivity activity) {
        this.activity = activity;
        downloadHelper = new DownloadHelper(activity);
        et = activity.findViewById(R.id.main_top_navbar_autocomplete);
        showAndHideKeyboard = new ShowAndHideKeyboard(activity);

    }

    public Tab getCurrentTab() {
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
        final FrameLayout webview_framelayout = activity.findViewById(R.id.main_framelayout_webview);
        boolean isDesktopUA = !tabs.isEmpty() && getCurrentTab().isDesktopUA;
        webview.getSettings().setUserAgentString(isDesktopUA ? activity.getString(R.string.desktopUA) : null);
        webview.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        webview.setVisibility(View.GONE);
        TabManager.Tab tab = new TabManager.Tab(webview);
        tab.isDesktopUA = isDesktopUA;
        tabs.add(tab);
        webview_framelayout.addView(webview);
        setTabCountText(tabs.size());
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

        showAndHideKeyboard.hideKeyboard();
    }

    public void newTab(String url) {
        WebView webview = createWebView(null, false);
        newTabCommon(webview);
        loadUrl(url, webview);
    }

    public void setTabCountText(int count) {
        TextView tabs_number = activity.findViewById(R.id.main_top_navbar_tabs_number);
        tabs_number.setText(String.valueOf(count));
    }

    @SuppressLint({"SetJavaScriptEnabled", "DefaultLocale"})
    public WebView createWebView(Bundle bundle, boolean isNightMode) {
        final ProgressBar progressBar = activity.findViewById(R.id.main_progressbar);
        final AutoCompleteTextView et = activity.findViewById(R.id.main_top_navbar_autocomplete);

        WebView webview = new WebView(activity);
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
                fullScreenView[0] = view;
                fullScreenCallback[0] = callback;
                activity.findViewById(R.id.main_browser_layout).setVisibility(View.INVISIBLE);
                ViewGroup fullscreenLayout = activity.findViewById(R.id.main_fullScreenVideo);
                fullscreenLayout.addView(view);
                fullscreenLayout.setVisibility(View.VISIBLE);
            }

            @Override
            public void onHideCustomView() {
                if (fullScreenView[0] == null) return;

                ViewGroup fullscreenLayout = activity.findViewById(R.id.main_fullScreenVideo);
                fullscreenLayout.removeView(fullScreenView[0]);
                fullscreenLayout.setVisibility(View.GONE);
                fullScreenView[0] = null;
                fullScreenCallback[0] = null;
                activity.findViewById(R.id.main_browser_layout).setVisibility(View.VISIBLE);
            }

            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                if (fileUploadCallback != null) {
                    fileUploadCallback.onReceiveValue(null);
                }

                fileUploadCallback = filePathCallback;
                Intent intent = fileChooserParams.createIntent();
                try {
                    activity.startActivityForResult(intent, PermissionCodes.FORM_FILE_CHOOSER);
                    return true;
                } catch (ActivityNotFoundException e) {
                    // Continue below
                }

                intent.setType("*/*");
                try {
                    activity.startActivityForResult(intent, PermissionCodes.FORM_FILE_CHOOSER);
                    return true;
                } catch (ActivityNotFoundException e) {
                    // Continue below
                }

                // Everything failed, let user know
                Toast.makeText(activity, activity.getString(R.string.file_chooser_open_fail), Toast.LENGTH_SHORT).show();
                fileUploadCallback = null;
                return false;
            }
        });
        webview.setWebViewClient(new WebViewClient() {
            final String[] sslErrors = {"Not yet valid", "Expired", "Hostname mismatch", "Untrusted CA", "Invalid date", "Unknown error"};

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
                if (view == getCurrentWebView()) {
                    // Don't use the argument url here since navigation to that URL might have been
                    // cancelled due to SSL error
                    if (et.getSelectionStart() == 0 && et.getSelectionEnd() == 0 && et.getText().toString().equals(view.getUrl())) {
                        // If user haven't started typing anything, focus on webview
                        view.requestFocus();
                    }
                }
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

            @SuppressLint("WebViewClientOnReceivedSslError")
            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                int primaryError = error.getPrimaryError();
                String errorStr = primaryError >= 0 && primaryError < sslErrors.length ? sslErrors[primaryError] : "Unknown error " + primaryError;
                new AlertDialog.Builder(activity)
                        .setTitle(activity.getString(R.string.insecure_connection))
                        .setMessage(String.format("Error: %s\nURL: %s\n\nCertificate:\n%s",
                                errorStr, error.getUrl(), WebCertificate.certificateToStr(error.getCertificate())))
                        .setPositiveButton(activity.getString(R.string.proceed), (dialog, which) -> handler.proceed())
                        .setNegativeButton(activity.getString(R.string.cancel), (dialog, which) -> handler.cancel())
                        .show();
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
            showLongPressMenu(url, imageUrl);
            return true;
        });
        webview.setDownloadListener((url, userAgent, contentDisposition, mimetype, contentLength) -> {
            String filename = URLUtil.guessFileName(url, contentDisposition, mimetype);
            new AlertDialog.Builder(activity)
                    .setTitle(activity.getString(R.string.download))
                    .setMessage(String.format(
                            activity.getString(R.string.filename) +
                                    "%s\n" +
                                    activity.getString(R.string.size) +
                                    "%.2f MB\n" +
                                    activity.getString(R.string.url) + "%s",
                            filename,
                            contentLength / 1024.0 / 1024.0,
                            url))
                    .setPositiveButton(activity.getString(R.string.download), (dialog, which) -> downloadHelper.startDownload(url, filename))
                    .setNeutralButton(activity.getString(R.string.open), (dialog, which) -> {
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(url));
                        try {
                            activity.startActivity(i);
                        } catch (ActivityNotFoundException e) {
                            new AlertDialog.Builder(activity)
                                    .setTitle(activity.getString(R.string.open))
                                    .setMessage(activity.getString(R.string.open_file_try_download))
                                    .setPositiveButton(activity.getString(R.string.ok), (dialog1, which1) -> {
                                    })
                                    .show();
                        }
                    })
                    .setNegativeButton(activity.getString(R.string.cancel), (dialog, which) -> {
                    })
                    .show();
        });
        webview.setFindListener((activeMatchOrdinal, numberOfMatches, isDoneCounting) -> {
            TextView searchCount = activity.findViewById(R.id.main_top_navbar_search_count);
            searchCount.setText(numberOfMatches == 0 ? activity.getString(R.string.not_found) :
                    String.format("%d / %d", activeMatchOrdinal + 1, numberOfMatches));
        });

        return webview;
    }

    private void showLongPressMenu(String linkUrl, String imageUrl) {
        String url;
        String title;
        String[] options = new String[]{
                activity.getString(R.string.open_in_new_tab),
                activity.getString(R.string.copy_url),
                activity.getString(R.string.show_full_url),
                activity.getString(R.string.download)};

        if (imageUrl == null) {
            if (linkUrl == null) {
                throw new IllegalArgumentException("Bad null arguments in showLongPressMenu");
            } else {
                // Text link
                url = linkUrl;
                title = linkUrl;
            }
        } else {
            if (linkUrl == null) {
                // Image without link
                url = imageUrl;
                title = activity.getString(R.string.image) + imageUrl;
            } else {
                // Image with link
                url = linkUrl;
                title = linkUrl;
                String[] newOptions = new String[options.length + 1];
                System.arraycopy(options, 0, newOptions, 0, options.length);
                newOptions[newOptions.length - 1] = activity.getString(R.string.image_options);
                options = newOptions;
            }
        }
        new AlertDialog.Builder(activity).setTitle(title).setItems(options, (dialog, which) -> {
            switch (which) {
                case 0:
                    newTab(url);
                    break;
                case 1:
                    ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(CLIPBOARD_SERVICE);
                    assert clipboard != null;
                    ClipData clipData = ClipData.newPlainText(activity.getString(R.string.url), url);
                    clipboard.setPrimaryClip(clipData);
                    break;
                case 2:
                    new AlertDialog.Builder(activity)
                            .setTitle(activity.getString(R.string.full_url))
                            .setMessage(url)
                            .setPositiveButton(activity.getString(R.string.ok), (dialog1, which1) -> {
                            })
                            .show();
                    break;
                case 3:
                    downloadHelper.startDownload(url, null);
                    break;
                case 4:
                    showLongPressMenu(null, imageUrl);
                    break;
            }
        }).show();

    }

    public void closeCurrentTab() {
        if (getCurrentWebView().getUrl() != null && !getCurrentWebView().getUrl().equals("google.com")) {
            TitleAndBundle titleAndBundle = new TitleAndBundle();
            titleAndBundle.title = getCurrentWebView().getTitle();
            titleAndBundle.bundle = new Bundle();
            getCurrentWebView().saveState(titleAndBundle.bundle);
            closedTabs.add(0, titleAndBundle);
            if (closedTabs.size() > 500) {
                closedTabs.remove(closedTabs.size() - 1);
            }
        }
        ((FrameLayout) activity.findViewById(R.id.main_framelayout_webview)).removeView(getCurrentWebView());
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
        setTabCountText(tabs.size());
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
