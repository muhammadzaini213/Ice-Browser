package com.ibndev.icebrowser;

import static com.ibndev.icebrowser.browserparts.TabManager.FORM_FILE_CHOOSER;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.net.http.SslCertificate;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.ibndev.icebrowser.browserparts.DownloadHelper;
import com.ibndev.icebrowser.browserparts.PlacesDbHelper;
import com.ibndev.icebrowser.browserparts.PopupMenuHelper;
import com.ibndev.icebrowser.browserparts.SearchAutocompleteAdapter;
import com.ibndev.icebrowser.browserparts.ShowAndHideKeyboard;
import com.ibndev.icebrowser.browserparts.TabManager;
import com.ibndev.icebrowser.browserparts.TabsAdapter;
import com.ibndev.icebrowser.browserparts.WebCertificate;

import java.util.ArrayList;
import java.util.List;

public class WebActivity extends Activity {

    private static final String TAG = WebActivity.class.getSimpleName();
    final int PERMISSION_REQUEST_DOWNLOAD = 3;
    ShowAndHideKeyboard showAndHideKeyboard;
    DownloadHelper downloadHelper;
    private FrameLayout webviews;
    private AutoCompleteTextView et;
    private boolean isNightMode;
    private boolean isFullscreen;
    private SharedPreferences prefs;
    private EditText searchEdit;
    private SQLiteDatabase placesDb;
    private ValueCallback<Uri[]> fileUploadCallback;
    private boolean fileUploadCallbackShouldReset;
    private final List<String> tabTitles = new ArrayList<>();
    private TabsAdapter adapter;
    private final ArrayList<TitleAndBundle> closedTabs = new ArrayList<>();
    private TabManager tabManager;


    private void switchToTab(int tab) {
        tabManager.switchToTab(tab);
        et.setText(tabManager.getCurrentWebView().getUrl());
    }

    private void updateFullScreen() {
        final int flags = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        boolean fullscreenNow = (getWindow().getDecorView().getSystemUiVisibility() & flags) == flags;
        if (fullscreenNow != isFullscreen) {
            getWindow().getDecorView().setSystemUiVisibility(isFullscreen ? flags : 0);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            private final Thread.UncaughtExceptionHandler defaultUEH = Thread.getDefaultUncaughtExceptionHandler();

            @Override
            public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
                defaultUEH.uncaughtException(t, e);
            }
        });

        try {
            placesDb = new PlacesDbHelper(this).getWritableDatabase();
        } catch (SQLiteException e) {
            Log.e(TAG, "Can't open database", e);
        }

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        setContentView(R.layout.activity_main1);

        et = findViewById(R.id.et);

        showAndHideKeyboard = new ShowAndHideKeyboard(this, et);
        downloadHelper = new DownloadHelper(this);
        PopupMenuHelper menuHelper = new PopupMenuHelper(this, placesDb);

        tabRecyclerView();
        tabsLayout();
        bottomBar();

        findViewById(R.id.more_button).setOnClickListener(view -> {
            menuHelper.showPopupMenu(view, tabManager.getCurrentWebView().getUrl(), tabManager.getCurrentWebView().getTitle());
        });

        findViewById(R.id.tabs_button).setOnClickListener(view -> {
            tabTitles.clear();
            for (int i = 0; i < tabManager.tabs.size(); i++) {
                tabTitles.add(tabManager.tabs.get(i).webview.getTitle());
            }
            adapter.notifyDataSetChanged();
            findViewById(R.id.tabs_layout).setVisibility(View.VISIBLE);
        });

        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(visibility -> updateFullScreen());

        isFullscreen = false;
        isNightMode = prefs.getBoolean("night_mode", false);

        webviews = findViewById(R.id.webviews);

        et = findViewById(R.id.et);

        tabManager = new TabManager(this, webviews, et, findViewById(R.id.progressbar), findViewById(R.id.tabs_number), showAndHideKeyboard, downloadHelper);
        // setup edit text
        et.setSelected(false);
        String initialUrl = getUrlFromIntent(getIntent());
        et.setText(initialUrl.isEmpty() ? "google.com" : initialUrl);
        et.setAdapter(new SearchAutocompleteAdapter(this, text -> {
            et.setText(text);
            et.setSelection(text.length());
        }));
        et.setOnItemClickListener((parent, view, position, id) -> {
            tabManager.getCurrentWebView().requestFocus();
            tabManager.loadUrl(et.getText().toString(), tabManager.getCurrentWebView());
        });


        et.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                tabManager.loadUrl(et.getText().toString(), tabManager.getCurrentWebView());
                tabManager.getCurrentWebView().requestFocus();
                return true;
            } else {
                return false;
            }
        });

        searchEdit = findViewById(R.id.searchEdit);
        searchEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tabManager.getCurrentWebView().findAllAsync(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        findViewById(R.id.searchFindNext).setOnClickListener(v -> {
            showAndHideKeyboard.hideKeyboard();
            tabManager.getCurrentWebView().findNext(true);
        });

        findViewById(R.id.searchFindPrev).setOnClickListener(v -> {
            showAndHideKeyboard.hideKeyboard();
            tabManager.getCurrentWebView().findNext(false);
        });

        findViewById(R.id.searchClose).setOnClickListener(v -> {
            tabManager.getCurrentWebView().clearMatches();
            searchEdit.setText("");
            tabManager.getCurrentWebView().requestFocus();
            findViewById(R.id.searchPane).setVisibility(View.GONE);
            showAndHideKeyboard.hideKeyboard();
        });


        tabManager.newTab(et.getText().toString());
        tabManager.getCurrentWebView().setVisibility(View.VISIBLE);
        tabManager.getCurrentWebView().requestFocus();


    }

    //TODO: Move this class to TabManager from here
//    private WebView createWebView(Bundle bundle) {
//        final ProgressBar progressBar = findViewById(R.id.progressbar);
//
//        WebView webview = new WebView(this);
//        if (bundle != null) {
//            webview.restoreState(bundle);
//        }
//        webview.setBackgroundColor(isNightMode ? Color.BLACK : Color.WHITE);
//        WebSettings settings = webview.getSettings();
//        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
//        settings.setAllowUniversalAccessFromFileURLs(true);
//        settings.setJavaScriptEnabled(true);
//        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
//        settings.setDomStorageEnabled(true);
//        settings.setBuiltInZoomControls(true);
//        settings.setDisplayZoomControls(false);
//        settings.setLoadWithOverviewMode(true);
//        webview.setWebChromeClient(new WebChromeClient() {
//            @Override
//            public void onProgressChanged(WebView view, int newProgress) {
//                super.onProgressChanged(view, newProgress);
//                if (newProgress == 100) {
//                    progressBar.setVisibility(View.GONE);
//                } else {
//                    progressBar.setProgress(newProgress);
//                }
//            }
//
//            @Override
//            public void onShowCustomView(View view, CustomViewCallback callback) {
//                fullScreenView[0] = view;
//                fullScreenCallback[0] = callback;
//                WebActivity.this.findViewById(R.id.main_layout).setVisibility(View.INVISIBLE);
//                ViewGroup fullscreenLayout = WebActivity.this.findViewById(R.id.fullScreenVideo);
//                fullscreenLayout.addView(view);
//                fullscreenLayout.setVisibility(View.VISIBLE);
//            }
//
//            @Override
//            public void onHideCustomView() {
//                if (fullScreenView[0] == null) return;
//
//                ViewGroup fullscreenLayout = WebActivity.this.findViewById(R.id.fullScreenVideo);
//                fullscreenLayout.removeView(fullScreenView[0]);
//                fullscreenLayout.setVisibility(View.GONE);
//                fullScreenView[0] = null;
//                fullScreenCallback[0] = null;
//                WebActivity.this.findViewById(R.id.main_layout).setVisibility(View.VISIBLE);
//            }
//
//            @Override
//            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
//                if (fileUploadCallback != null) {
//                    fileUploadCallback.onReceiveValue(null);
//                }
//
//                fileUploadCallback = filePathCallback;
//                Intent intent = fileChooserParams.createIntent();
//                try {
//                    fileUploadCallbackShouldReset = true;
//                    startActivityForResult(intent, FORM_FILE_CHOOSER);
//                    return true;
//                } catch (ActivityNotFoundException e) {
//                    // Continue below
//                }
//
//                intent.setType("*/*");
//                try {
//                    fileUploadCallbackShouldReset = false;
//                    startActivityForResult(intent, FORM_FILE_CHOOSER);
//                    return true;
//                } catch (ActivityNotFoundException e) {
//                    // Continue below
//                }
//
//                // Everything failed, let user know
//                Toast.makeText(WebActivity.this, "Can't open file chooser", Toast.LENGTH_SHORT).show();
//                fileUploadCallback = null;
//                return false;
//            }
//        });
//        webview.setWebViewClient(new WebViewClient() {
//            final String[] sslErrors = {"Not yet valid", "Expired", "Hostname mismatch", "Untrusted CA", "Invalid date", "Unknown error"};
//
//            @Override
//            public void onPageStarted(WebView view, String url, Bitmap favicon) {
//                progressBar.setProgress(0);
//                progressBar.setVisibility(View.VISIBLE);
//                if (view == tabManager.getCurrentWebView()) {
//                    et.setText(url);
//                    et.setSelection(0);
//                    view.requestFocus();
//                }
//            }
//
//            @Override
//            public void onPageFinished(WebView view, String url) {
//                if (view == tabManager.getCurrentWebView()) {
//                    // Don't use the argument url here since navigation to that URL might have been
//                    // cancelled due to SSL error
//                    if (et.getSelectionStart() == 0 && et.getSelectionEnd() == 0 && et.getText().toString().equals(view.getUrl())) {
//                        // If user haven't started typing anything, focus on webview
//                        view.requestFocus();
//                    }
//                }
//            }
//
//            @Override
//            public void onReceivedHttpAuthRequest(WebView view, final HttpAuthHandler handler, String host, String realm) {
//                new AlertDialog.Builder(WebActivity.this)
//                        .setTitle(host)
//                        .setView(R.layout.login_password)
//                        .setCancelable(false)
//                        .setPositiveButton("OK", (dialog, which) -> {
//                            String username = ((EditText) ((Dialog) dialog).findViewById(R.id.username)).getText().toString();
//                            String password = ((EditText) ((Dialog) dialog).findViewById(R.id.password)).getText().toString();
//                            handler.proceed(username, password);
//                        })
//                        .setNegativeButton("Cancel", (dialog, which) -> handler.cancel()).show();
//            }
//
//            @Override
//            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
//
//                return super.shouldInterceptRequest(view, request);
//            }
//
//            @Override
//            public boolean shouldOverrideUrlLoading(WebView view, String url) {
//                // For intent:// URLs, redirect to browser_fallback_url if given
//                if (url.startsWith("intent://")) {
//                    int start = url.indexOf(";S.browser_fallback_url=");
//                    if (start != -1) {
//                        start += ";S.browser_fallback_url=".length();
//                        int end = url.indexOf(';', start);
//                        if (end != -1 && end != start) {
//                            url = url.substring(start, end);
//                            url = Uri.decode(url);
//                            view.loadUrl(url);
//                            return true;
//                        }
//                    }
//                }
//                return false;
//            }
//
//            @Override
//            public void onLoadResource(WebView view, String url) {
//
//            }
//
//            @Override
//            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
//                int primaryError = error.getPrimaryError();
//                String errorStr = primaryError >= 0 && primaryError < sslErrors.length ? sslErrors[primaryError] : "Unknown error " + primaryError;
//                new AlertDialog.Builder(WebActivity.this)
//                        .setTitle("Insecure connection")
//                        .setMessage(String.format("Error: %s\nURL: %s\n\nCertificate:\n%s",
//                                errorStr, error.getUrl(), WebCertificate.certificateToStr(error.getCertificate())))
//                        .setPositiveButton("Proceed", (dialog, which) -> handler.proceed())
//                        .setNegativeButton("Cancel", (dialog, which) -> handler.cancel())
//                        .show();
//            }
//        });
//        webview.setOnLongClickListener(v -> {
//            String url = null, imageUrl = null;
//            WebView.HitTestResult r = ((WebView) v).getHitTestResult();
//            switch (r.getType()) {
//                case WebView.HitTestResult.SRC_ANCHOR_TYPE:
//                    url = r.getExtra();
//                    break;
//                case WebView.HitTestResult.IMAGE_TYPE:
//                    imageUrl = r.getExtra();
//                    break;
//                case WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE:
//                case WebView.HitTestResult.EMAIL_TYPE:
//                case WebView.HitTestResult.UNKNOWN_TYPE:
//                    Handler handler = new Handler();
//                    Message message = handler.obtainMessage();
//                    ((WebView) v).requestFocusNodeHref(message);
//                    url = message.getData().getString("url");
//                    if ("".equals(url)) {
//                        url = null;
//                    }
//                    imageUrl = message.getData().getString("src");
//                    if ("".equals(imageUrl)) {
//                        imageUrl = null;
//                    }
//                    if (url == null && imageUrl == null) {
//                        return false;
//                    }
//                    break;
//                default:
//                    return false;
//            }
//            showLongPressMenu(url, imageUrl);
//            return true;
//        });
//        webview.setDownloadListener((url, userAgent, contentDisposition, mimetype, contentLength) -> {
//            String filename = URLUtil.guessFileName(url, contentDisposition, mimetype);
//            new AlertDialog.Builder(WebActivity.this)
//                    .setTitle("Download")
//                    .setMessage(String.format("Filename: %s\nSize: %.2f MB\nURL: %s",
//                            filename,
//                            contentLength / 1024.0 / 1024.0,
//                            url))
//                    .setPositiveButton("Download", (dialog, which) -> downloadHelper.startDownload(url, filename))
//                    .setNeutralButton("Open", (dialog, which) -> {
//                        Intent i = new Intent(Intent.ACTION_VIEW);
//                        i.setData(Uri.parse(url));
//                        try {
//                            startActivity(i);
//                        } catch (ActivityNotFoundException e) {
//                            new AlertDialog.Builder(WebActivity.this)
//                                    .setTitle("Open")
//                                    .setMessage("Can't open files of this type. Try downloading instead.")
//                                    .setPositiveButton("OK", (dialog1, which1) -> {
//                                    })
//                                    .show();
//                        }
//                    })
//                    .setNegativeButton("Cancel", (dialog, which) -> {
//                    })
//                    .show();
//        });
//        webview.setFindListener((activeMatchOrdinal, numberOfMatches, isDoneCounting) ->
//                searchCount.setText(numberOfMatches == 0 ? "Not found" :
//                        String.format("%d / %d", activeMatchOrdinal + 1, numberOfMatches)));
//        return webview;
//    }
//
//    private void newTabCommon(WebView webview) {
//        boolean isDesktopUA = !tabManager.tabs.isEmpty() && tabManager.getCurrentTab().isDesktopUA;
//        webview.getSettings().setUserAgentString(isDesktopUA ? getString(R.string.desktopUA) : null);
//        webview.getSettings().setUseWideViewPort(isDesktopUA);
//        webview.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
//        webview.setVisibility(View.GONE);
//        TabManager.Tab tab = new TabManager.Tab(webview);
//        tab.isDesktopUA = isDesktopUA;
//        tabManager.tabs.add(tab);
//        webviews.addView(webview);
//        setTabCountText(tabManager.tabs.size());
//    }
//    private void loadUrl(String url, WebView webview) {
//        url = url.trim();
//        if (url.isEmpty()) {
//            url = "about:blank";
//        }
//        if (url.startsWith("about:") || url.startsWith("javascript:") || url.startsWith("file:") || url.startsWith("data:") ||
//                (url.indexOf(' ') == -1 && Patterns.WEB_URL.matcher(url).matches())) {
//            int indexOfHash = url.indexOf('#');
//            String guess = URLUtil.guessUrl(url);
//            if (indexOfHash != -1 && guess.indexOf('#') == -1) {
//                // Hash exists in original URL but no hash in guessed URL
//                url = guess + url.substring(indexOfHash);
//            } else {
//                url = guess;
//            }
//        } else {
//            url = URLUtil.composeSearchUrl(url, "https://www.google.com/search?q=%s", "%s");
//        }
//
//        webview.loadUrl(url);
//
//        showAndHideKeyboard.hideKeyboard();
//    }
//
//    private void newTab(String url) {
//        WebView webview = createWebView(null);
//        newTabCommon(webview);
//        loadUrl(url, webview);
//    }
//
//    public void setTabCountText(int count) {
//        TextView tabs_number = findViewById(R.id.tabs_number);
//        tabs_number.setText(String.valueOf(count));
//    }
//
//    private void showLongPressMenu(String linkUrl, String imageUrl) {
//        String url;
//        String title;
//        String[] options = new String[]{"Open in new tab", "Copy URL", "Show full URL", "Download"};
//
//        if (imageUrl == null) {
//            if (linkUrl == null) {
//                throw new IllegalArgumentException("Bad null arguments in showLongPressMenu");
//            } else {
//                // Text link
//                url = linkUrl;
//                title = linkUrl;
//            }
//        } else {
//            if (linkUrl == null) {
//                // Image without link
//                url = imageUrl;
//                title = "Image: " + imageUrl;
//            } else {
//                // Image with link
//                url = linkUrl;
//                title = linkUrl;
//                String[] newOptions = new String[options.length + 1];
//                System.arraycopy(options, 0, newOptions, 0, options.length);
//                newOptions[newOptions.length - 1] = "Image Options";
//                options = newOptions;
//            }
//        }
//        new AlertDialog.Builder(WebActivity.this).setTitle(title).setItems(options, (dialog, which) -> {
//            switch (which) {
//                case 0:
//                    newTab(url);
//                    break;
//                case 1:
//                    ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
//                    assert clipboard != null;
//                    ClipData clipData = ClipData.newPlainText("URL", url);
//                    clipboard.setPrimaryClip(clipData);
//                    break;
//                case 2:
//                    new AlertDialog.Builder(WebActivity.this)
//                            .setTitle("Full URL")
//                            .setMessage(url)
//                            .setPositiveButton("OK", (dialog1, which1) -> {
//                            })
//                            .show();
//                    break;
//                case 3:
//                    downloadHelper.startDownload(url, null);
//                    break;
//                case 4:
//                    showLongPressMenu(null, imageUrl);
//                    break;
//            }
//        }).show();
//    }

    //to here

    private void tabsLayout() {
        View tabs_layout = findViewById(R.id.tabs_layout);

        tabs_layout.findViewById(R.id.tabs_menu).setOnClickListener(view -> {

        });

        tabs_layout.findViewById(R.id.tabs_new_tab).setOnClickListener(view -> {
            tabManager.newTab("google.com");
            switchToTab(tabManager.tabs.size() - 1);
            tabs_layout.setVisibility(View.GONE);
        });

        tabs_layout.findViewById(R.id.close_tabs_menu).setOnClickListener(view -> tabs_layout.setVisibility(View.GONE));
    }

    private void tabRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.tabs_list);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TabsAdapter(tabTitles, v -> {
            int position = (int) v.getTag();
            switchToTab(position);
            findViewById(R.id.tabs_layout).setVisibility(View.GONE);
        }, v -> {
            int position = (int) v.getTag();
            switchToTab(position);

            if (tabTitles.size() == 1) {
                findViewById(R.id.tabs_layout).setVisibility(View.GONE);
            }
            if (position >= 0 && position < tabTitles.size()) {
                tabTitles.remove(position);
                adapter.notifyItemRemoved(position);
                adapter.notifyDataSetChanged();
            }
            closeCurrentTab();
        });
        recyclerView.setAdapter(adapter);
    }

    private void bottomBar() {
        View bottom_bar = findViewById(R.id.bottom_bar);

        bottom_bar.findViewById(R.id.back_btn).setOnClickListener(view -> {
            if (tabManager.getCurrentWebView().canGoBack()) {
                tabManager.getCurrentWebView().goBack();
            }
        });

        bottom_bar.findViewById(R.id.forward_btn).setOnClickListener(view -> {
            if (tabManager.getCurrentWebView().canGoForward()) {
                tabManager.getCurrentWebView().goForward();
            }
        });

        bottom_bar.findViewById(R.id.home_btn).setOnClickListener(view -> tabManager.getCurrentWebView().loadUrl("https://google.com"));

        bottom_bar.findViewById(R.id.refresh_btn).setOnClickListener(view -> tabManager.getCurrentWebView().reload());

        bottom_bar.findViewById(R.id.menu_btn).setOnClickListener(view -> showBottomSheetMenu());
    }

    private void showBottomSheetMenu() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(WebActivity.this);
        View bottomSheetView = LayoutInflater.from(WebActivity.this).inflate(
                R.layout.bottom_sheet_menu,
                null
        );

        bottomSheetView.findViewById(R.id.desktop).setOnClickListener(view -> {
            TabManager.Tab tab = tabManager.getCurrentTab();
            tab.isDesktopUA = !tab.isDesktopUA;
            tabManager.getCurrentWebView().getSettings().setUserAgentString(tab.isDesktopUA ? getString(R.string.desktopUA) : null);
            tabManager.getCurrentWebView().getSettings().setUseWideViewPort(tab.isDesktopUA);
            tabManager.getCurrentWebView().reload();

            bottomSheetDialog.dismiss();
        });

        bottomSheetView.findViewById(R.id.bookmark).setOnClickListener(view -> {
            showBookmarks();
            bottomSheetDialog.dismiss();
        });

        bottomSheetView.findViewById(R.id.adblock).setOnClickListener(view -> {

            bottomSheetDialog.dismiss();
        });

        bottomSheetView.findViewById(R.id.share).setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
            intent.putExtra(Intent.EXTRA_TEXT,tabManager.getCurrentWebView().getUrl());
            intent.setType("text/plain");
            startActivity(Intent.createChooser(intent, "Share URL"));

            bottomSheetDialog.dismiss();
        });

        bottomSheetView.findViewById(R.id.find).setOnClickListener(view -> {
            searchEdit.setText("");
            findViewById(R.id.searchPane).setVisibility(View.VISIBLE);
            searchEdit.requestFocus();
            showAndHideKeyboard.showKeyboard();

            bottomSheetDialog.dismiss();
        });

        bottomSheetView.findViewById(R.id.cookie).setOnClickListener(view -> {
            if (CookieManager.getInstance().acceptThirdPartyCookies(tabManager.getCurrentWebView())) {
                CookieManager cookieManager = CookieManager.getInstance();
                boolean newValue = !cookieManager.acceptThirdPartyCookies(tabManager.getCurrentWebView());
                cookieManager.setAcceptThirdPartyCookies(tabManager.getCurrentWebView(), newValue);
            }
            bottomSheetDialog.dismiss();
        });

        bottomSheetView.findViewById(R.id.tab_info).setOnClickListener(view -> {
            String s = "URL: " + tabManager.getCurrentWebView().getUrl() + "\n";
            s += "Title: " + tabManager.getCurrentWebView().getTitle() + "\n\n";
            SslCertificate certificate =tabManager.getCurrentWebView().getCertificate();
            s += certificate == null ? "Not secure" : "Certificate:\n" + WebCertificate.certificateToStr(certificate);

            new AlertDialog.Builder(this)
                    .setTitle("Page info")
                    .setMessage(s)
                    .setPositiveButton("OK", (dialog, which) -> {
                    })
                    .show();

            bottomSheetDialog.dismiss();
        });

        bottomSheetView.findViewById(R.id.exit).setOnClickListener(view -> {
            finishAffinity();
            System.exit(0);
        });

        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }

    @Override
    protected void onDestroy() {
        if (placesDb != null) {
            placesDb.close();
        }
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FORM_FILE_CHOOSER) {
            if (fileUploadCallback != null) {
                if (fileUploadCallbackShouldReset) {
                    fileUploadCallback.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, data));
                    fileUploadCallback = null;
                } else {
                    fileUploadCallbackShouldReset = true;
                }
            }
        }
    }

    private void toggleFullscreen() {
        isFullscreen = !isFullscreen;
        updateFullScreen();
    }

    private void toggleNightMode() {
        isNightMode = !isNightMode;
        prefs.edit().putBoolean("night_mode", isNightMode).apply();
    }

    private void showBookmarks() {
        if (placesDb == null) return;
        Cursor cursor = placesDb.rawQuery("SELECT title, url, id as _id FROM bookmarks", null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Bookmarks")
                .setOnDismissListener(dlg -> cursor.close())
                .setCursor(cursor, (dlg, which) -> {
                    cursor.moveToPosition(which);
                    @SuppressLint("Range") String url = cursor.getString(cursor.getColumnIndex("url"));
                    et.setText(url);
                    tabManager.loadUrl(url,tabManager.getCurrentWebView());
                }, "title")
                .create();
        dialog.getListView().setOnItemLongClickListener((parent, view, position, id) -> {
            cursor.moveToPosition(position);
            @SuppressLint("Range") int rowid = cursor.getInt(cursor.getColumnIndex("_id"));
            @SuppressLint("Range") String title = cursor.getString(cursor.getColumnIndex("title"));
            @SuppressLint("Range") String url = cursor.getString(cursor.getColumnIndex("url"));
            dialog.dismiss();
            new AlertDialog.Builder(WebActivity.this)
                    .setTitle(title)
                    .setItems(new String[]{"Rename", "Change URL", "Delete"}, (dlg, which) -> {
                        switch (which) {
                            case 0: {
                                EditText editView = new EditText(this);
                                editView.setText(title);
                                new AlertDialog.Builder(this)
                                        .setTitle("Rename bookmark")
                                        .setView(editView)
                                        .setPositiveButton("Rename", (renameDlg, which1) -> {
                                            placesDb.execSQL("UPDATE bookmarks SET title=? WHERE id=?", new Object[]{editView.getText(), rowid});
                                        })
                                        .setNegativeButton("Cancel", (renameDlg, which1) -> {
                                        })
                                        .show();
                                break;
                            }
                            case 1: {
                                EditText editView = new EditText(this);
                                editView.setText(url);
                                new AlertDialog.Builder(this)
                                        .setTitle("Change bookmark URL")
                                        .setView(editView)
                                        .setPositiveButton("Change URL", (renameDlg, which1) -> {
                                            placesDb.execSQL("UPDATE bookmarks SET url=? WHERE id=?", new Object[]{editView.getText(), rowid});
                                        })
                                        .setNegativeButton("Cancel", (renameDlg, which1) -> {
                                        })
                                        .show();
                                break;
                            }
                            case 2:
                                placesDb.execSQL("DELETE FROM bookmarks WHERE id = ?", new Object[]{rowid});
                                break;
                        }
                    })
                    .show();
            return true;
        });
        dialog.show();
    }

    private void closeCurrentTab() {
        if (tabManager.getCurrentWebView().getUrl() != null && !tabManager.getCurrentWebView().getUrl().equals("google.com")) {
            TitleAndBundle titleAndBundle = new TitleAndBundle();
            titleAndBundle.title =tabManager.getCurrentWebView().getTitle();
            titleAndBundle.bundle = new Bundle();
           tabManager.getCurrentWebView().saveState(titleAndBundle.bundle);
            closedTabs.add(0, titleAndBundle);
            if (closedTabs.size() > 500) {
                closedTabs.remove(closedTabs.size() - 1);
            }
        }
        ((FrameLayout) findViewById(R.id.webviews)).removeView(tabManager.getCurrentWebView());
       tabManager.getCurrentWebView().destroy();
        tabManager.tabs.remove(tabManager.currentTabIndex);
        if (tabManager.currentTabIndex >= tabManager.tabs.size()) {
            tabManager.currentTabIndex = tabManager.tabs.size() - 1;
        }
        if (tabManager.currentTabIndex == -1) {
            // We just closed the last tab
            tabManager.newTab("google.com");
            tabManager.currentTabIndex = 0;
        }
       tabManager.getCurrentWebView().setVisibility(View.VISIBLE);
        et.setText(tabManager.getCurrentWebView().getUrl());
        tabManager.setTabCountText(tabManager.tabs.size());
       tabManager.getCurrentWebView().requestFocus();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_DOWNLOAD) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
                new AlertDialog.Builder(this)
                        .setTitle("Permission Denied")
                        .setMessage("The app needs storage permission to download files.")
                        .setPositiveButton("OK", (dialog, which) -> {
                        })
                        .show();
            }
        }
    }

    private String getUrlFromIntent(Intent intent) {
        if (Intent.ACTION_VIEW.equals(intent.getAction()) && intent.getData() != null) {
            return intent.getDataString();
        } else if (Intent.ACTION_SEND.equals(intent.getAction()) && "text/plain".equals(intent.getType())) {
            return intent.getStringExtra(Intent.EXTRA_TEXT);
        } else if (Intent.ACTION_WEB_SEARCH.equals(intent.getAction()) && intent.getStringExtra("query") != null) {
            return intent.getStringExtra("query");
        } else {
            return "";
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        String url = getUrlFromIntent(intent);
        if (!url.isEmpty()) {
            tabManager.newTab("google.com");
            switchToTab(tabManager.tabs.size() - 1);
        }
    }

    @Override
    public void onBackPressed() {
        if (findViewById(R.id.fullScreenVideo).getVisibility() == View.VISIBLE && tabManager.fullScreenCallback[0] != null) {
            tabManager.fullScreenCallback[0].onCustomViewHidden();
        } else if (tabManager.getCurrentWebView().canGoBack()) {
           tabManager.getCurrentWebView().goBack();
        } else if (tabManager.tabs.size() > 1) {
            closeCurrentTab();
        } else {
            super.onBackPressed();
        }
    }

    interface MyBooleanSupplier {
        boolean getAsBoolean();
    }



    static class TitleAndBundle {
        String title;
        Bundle bundle;
    }

    //TODO: Need it sometime
//    private void deleteAllBookmarks() {
//        if (placesDb == null) {
//            new AlertDialog.Builder(this)
//                    .setTitle("Bookmarks error")
//                    .setMessage("Can't open bookmarks database")
//                    .setPositiveButton("OK", (dialog, which) -> {
//                    })
//                    .show();
//            return;
//        }
//        new AlertDialog.Builder(this)
//                .setTitle("Delete all bookmarks?")
//                .setMessage("This action cannot be undone")
//                .setNegativeButton("Cancel", (dialog, which) -> {
//                })
//                .setPositiveButton("Delete All", (dialog, which) -> placesDb.execSQL("DELETE FROM bookmarks"))
//                .show();
//    }
//
//    private void clearHistoryCache() {
//        WebView v = getCurrentWebView();
//        v.clearCache(true);
//        v.clearFormData();
//        v.clearHistory();
//        CookieManager.getInstance().removeAllCookies(null);
//        WebStorage.getInstance().deleteAllData();
//    }
    //TODO: Maybe you need this
//    final MenuAction[] menuActions = new MenuAction[]{
//            new MenuAction("Night mode", R.drawable.night, this::toggleNightMode, () -> isNightMode),
//            new MenuAction("Full screen", R.drawable.fullscreen, this::toggleFullscreen, () -> isFullscreen),
//
//            new MenuAction("Delete all bookmarks", 0, this::deleteAllBookmarks),
//
//            new MenuAction("Clear history and cache", 0, this::clearHistoryCache),
//    };
}
