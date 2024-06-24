package com.ibndev.icebrowser;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.net.http.SslCertificate;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.HttpAuthHandler;
import android.webkit.SslErrorHandler;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.FrameLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.ibndev.icebrowser.browserparts.PlacesDbHelper;
import com.ibndev.icebrowser.browserparts.ShowAndHideKeyboard;
import com.ibndev.icebrowser.browserparts.TabsAdapter;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class WebActivity extends Activity {

    public static class Tab {
        public Tab(WebView w) {
            this.webview = w;
        }

        WebView webview;
        boolean isDesktopUA;
    }

    private final View[] fullScreenView = new View[1];
    private final WebChromeClient.CustomViewCallback[] fullScreenCallback = new WebChromeClient.CustomViewCallback[1];

    private static final String TAG = WebActivity.class.getSimpleName();

    static final String searchUrl = "https://www.google.com/search?q=%s";
    static final String searchCompleteUrl = "https://www.google.com/complete/search?client=firefox&q=%s";
    static final String desktopUA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.163 Safari/537.36";

    static final int PERMISSION_REQUEST_DOWNLOAD = 3;

    static final int FORM_FILE_CHOOSER = 1;

    private ArrayList<Tab> tabs = new ArrayList<>();
    private int currentTabIndex;
    private FrameLayout webviews;
    private AutoCompleteTextView et;
    private boolean isNightMode;
    private boolean isFullscreen;
    private SharedPreferences prefs;
    private TextView searchCount;

    private EditText searchEdit;

    private SQLiteDatabase placesDb;

    private ValueCallback<Uri[]> fileUploadCallback;
    private boolean fileUploadCallbackShouldReset;

    private static class MenuAction {

        static HashMap<String, MenuAction> actions = new HashMap<>();

        private MenuAction(String title, int icon, Runnable action) {
            this(title, icon, action, null);
        }

        private MenuAction(String title, int icon, Runnable action, MyBooleanSupplier getState) {
            this.title = title;
            this.icon = icon;
            this.action = action;
            this.getState = getState;
            actions.put(title, this);
        }

        @Override
        public String toString() {
            return title;
        }

        private String title;
        private int icon;
        private Runnable action;
        private MyBooleanSupplier getState;
    }

    boolean isBookmarked = false;
    private List<String> tabTitles = new ArrayList<>();
    private RecyclerView recyclerView;
    private TabsAdapter adapter;

    private void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        MenuInflater inflater = popupMenu.getMenuInflater();
        inflater.inflate(R.menu.top_bar_menu, popupMenu.getMenu());


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            popupMenu.setForceShowIcon(true);
        } else {
            try {
                Field[] fields = popupMenu.getClass().getDeclaredFields();
                for (Field field : fields) {
                    if ("mPopup".equals(field.getName())) {
                        field.setAccessible(true);
                        Object menuPopupHelper = field.get(popupMenu);
                        Class<?> classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());
                        Method setForceIcons = classPopupHelper.getMethod("setForceShowIcon", boolean.class);
                        setForceIcons.invoke(menuPopupHelper, true);
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        isBookmarked = isUrlInBookmarks(getCurrentWebView().getUrl());


        if (isBookmarked) {
            popupMenu.getMenu().findItem(R.id.action_save_bookmark).setIcon(R.drawable.bookmark_saved);
            popupMenu.getMenu().findItem(R.id.action_save_bookmark).setTitle("Remove Bookmark");
        } else {
            popupMenu.getMenu().findItem(R.id.action_save_bookmark).setIcon(R.drawable.add_);
            popupMenu.getMenu().findItem(R.id.action_save_bookmark).setTitle("Save Bookmark");
        }

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                return onOptionsItemSelected(item);
            }
        });
        popupMenu.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_open_with){
            openUrlInApp();
            return true;
        } else if (item.getItemId() == R.id.action_save_bookmark){
            if(isBookmarked){
                deleteBookmark(getCurrentWebView().getUrl());
                Toast.makeText(WebActivity.this, "Bookmark removed", Toast.LENGTH_SHORT).show();
            } else {
                addBookmark();
                Toast.makeText(WebActivity.this, "Bookmark saved", Toast.LENGTH_SHORT).show();
            }
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void deleteBookmark(String urlToCheck) {
        if (placesDb == null) return;

        // Use parameterized query to avoid SQL injection
        placesDb.execSQL("DELETE FROM bookmarks WHERE url = ?", new Object[]{urlToCheck});
    }


    @SuppressWarnings("unchecked")
    final MenuAction[] menuActions = new MenuAction[]{
            new MenuAction("Night mode", R.drawable.night, this::toggleNightMode, () -> isNightMode),
            new MenuAction("Full screen", R.drawable.fullscreen, this::toggleFullscreen, () -> isFullscreen),

            new MenuAction("Delete all bookmarks", 0, this::deleteAllBookmarks),

            new MenuAction("Clear history and cache", 0, this::clearHistoryCache),
    };

    static class TitleAndUrl {
        String title;
        String url;
    }

    static class TitleAndBundle {
        String title;
        Bundle bundle;
    }

    private ArrayList<TitleAndBundle> closedTabs = new ArrayList<>();

    private Tab getCurrentTab() {
        return tabs.get(currentTabIndex);
    }

    ShowAndHideKeyboard showAndHideKeyboard;


    private WebView getCurrentWebView() {
        return getCurrentTab().webview;
    }


    private void switchToTab(int tab) {
        getCurrentWebView().setVisibility(View.GONE);
        currentTabIndex = tab;
        getCurrentWebView().setVisibility(View.VISIBLE);
        et.setText(getCurrentWebView().getUrl());
        getCurrentWebView().requestFocus();
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
            private Thread.UncaughtExceptionHandler defaultUEH = Thread.getDefaultUncaughtExceptionHandler();

            @Override
            public void uncaughtException(Thread t, Throwable e) {
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


        tabRecyclerView();
        tabsLayout();
        bottomBar();

        findViewById(R.id.more_button).setOnClickListener(this::showPopupMenu);

        findViewById(R.id.tabs_button).setOnClickListener(view -> {
            tabTitles.clear();
            for (int i = 0; i < tabs.size(); i++) {
                tabTitles.add(tabs.get(i).webview.getTitle());
            }
            adapter.notifyDataSetChanged();
            findViewById(R.id.tabs_layout).setVisibility(View.VISIBLE);
        });

        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(visibility -> updateFullScreen());

        isFullscreen = false;
        isNightMode = prefs.getBoolean("night_mode", false);

        webviews = findViewById(R.id.webviews);
        currentTabIndex = 0;

        et = findViewById(R.id.et);

        // setup edit text
        et.setSelected(false);
        String initialUrl = getUrlFromIntent(getIntent());
        et.setText(initialUrl.isEmpty() ? "google.com" : initialUrl);
        et.setAdapter(new SearchAutocompleteAdapter(this, text -> {
            et.setText(text);
            et.setSelection(text.length());
        }));
        et.setOnItemClickListener((parent, view, position, id) -> {
            getCurrentWebView().requestFocus();
            loadUrl(et.getText().toString(), getCurrentWebView());
        });


        et.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                loadUrl(et.getText().toString(), getCurrentWebView());
                getCurrentWebView().requestFocus();
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
                getCurrentWebView().findAllAsync(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        searchCount = findViewById(R.id.searchCount);
        findViewById(R.id.searchFindNext).setOnClickListener(v -> {
            showAndHideKeyboard.hideKeyboard();
            getCurrentWebView().findNext(true);
        });

        findViewById(R.id.searchFindPrev).setOnClickListener(v -> {
            showAndHideKeyboard.hideKeyboard();
            getCurrentWebView().findNext(false);
        });

        findViewById(R.id.searchClose).setOnClickListener(v -> {
            getCurrentWebView().clearMatches();
            searchEdit.setText("");
            getCurrentWebView().requestFocus();
            findViewById(R.id.searchPane).setVisibility(View.GONE);
            showAndHideKeyboard.hideKeyboard();
        });


        newTab(et.getText().toString());
        getCurrentWebView().setVisibility(View.VISIBLE);
        getCurrentWebView().requestFocus();
        onNightModeChange();


    }

    private WebView createWebView(Bundle bundle) {
        final ProgressBar progressBar = findViewById(R.id.progressbar);

        WebView webview = new WebView(this);
        if (bundle != null) {
            webview.restoreState(bundle);
        }
        webview.setBackgroundColor(isNightMode ? Color.BLACK : Color.WHITE);
        WebSettings settings = webview.getSettings();
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
        settings.setAllowUniversalAccessFromFileURLs(true);
        settings.setJavaScriptEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setDomStorageEnabled(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setLoadWithOverviewMode(true);
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
                WebActivity.this.findViewById(R.id.main_layout).setVisibility(View.INVISIBLE);
                ViewGroup fullscreenLayout = WebActivity.this.findViewById(R.id.fullScreenVideo);
                fullscreenLayout.addView(view);
                fullscreenLayout.setVisibility(View.VISIBLE);
            }

            @Override
            public void onHideCustomView() {
                if (fullScreenView[0] == null) return;

                ViewGroup fullscreenLayout = WebActivity.this.findViewById(R.id.fullScreenVideo);
                fullscreenLayout.removeView(fullScreenView[0]);
                fullscreenLayout.setVisibility(View.GONE);
                fullScreenView[0] = null;
                fullScreenCallback[0] = null;
                WebActivity.this.findViewById(R.id.main_layout).setVisibility(View.VISIBLE);
            }

            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                if (fileUploadCallback != null) {
                    fileUploadCallback.onReceiveValue(null);
                }

                fileUploadCallback = filePathCallback;
                Intent intent = fileChooserParams.createIntent();
                try {
                    fileUploadCallbackShouldReset = true;
                    startActivityForResult(intent, FORM_FILE_CHOOSER);
                    return true;
                } catch (ActivityNotFoundException e) {
                    // Continue below
                }

                // FileChooserParams.createIntent() copies the <input type=file> "accept" attribute to the intent's getType(),
                // which can be e.g. ".png,.jpg" in addition to mime-type-style "image/*", however startActivityForResult()
                // only accepts mime-type-style. Try with just */* instead.
                intent.setType("*/*");
                try {
                    fileUploadCallbackShouldReset = false;
                    startActivityForResult(intent, FORM_FILE_CHOOSER);
                    return true;
                } catch (ActivityNotFoundException e) {
                    // Continue below
                }

                // Everything failed, let user know
                Toast.makeText(WebActivity.this, "Can't open file chooser", Toast.LENGTH_SHORT).show();
                fileUploadCallback = null;
                return false;
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
                new AlertDialog.Builder(WebActivity.this)
                        .setTitle(host)
                        .setView(R.layout.login_password)
                        .setCancelable(false)
                        .setPositiveButton("OK", (dialog, which) -> {
                            String username = ((EditText) ((Dialog) dialog).findViewById(R.id.username)).getText().toString();
                            String password = ((EditText) ((Dialog) dialog).findViewById(R.id.password)).getText().toString();
                            handler.proceed(username, password);
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> handler.cancel()).show();
            }

            final InputStream emptyInputStream = new ByteArrayInputStream(new byte[0]);

            String lastMainPage = "";

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {

                return super.shouldInterceptRequest(view, request);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // For intent:// URLs, redirect to browser_fallback_url if given
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

            final String[] sslErrors = {"Not yet valid", "Expired", "Hostname mismatch", "Untrusted CA", "Invalid date", "Unknown error"};

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                int primaryError = error.getPrimaryError();
                String errorStr = primaryError >= 0 && primaryError < sslErrors.length ? sslErrors[primaryError] : "Unknown error " + primaryError;
                new AlertDialog.Builder(WebActivity.this)
                        .setTitle("Insecure connection")
                        .setMessage(String.format("Error: %s\nURL: %s\n\nCertificate:\n%s",
                                errorStr, error.getUrl(), certificateToStr(error.getCertificate())))
                        .setPositiveButton("Proceed", (dialog, which) -> handler.proceed())
                        .setNegativeButton("Cancel", (dialog, which) -> handler.cancel())
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
            new AlertDialog.Builder(WebActivity.this)
                    .setTitle("Download")
                    .setMessage(String.format("Filename: %s\nSize: %.2f MB\nURL: %s",
                            filename,
                            contentLength / 1024.0 / 1024.0,
                            url))
                    .setPositiveButton("Download", (dialog, which) -> startDownload(url, filename))
                    .setNeutralButton("Open", (dialog, which) -> {
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(url));
                        try {
                            startActivity(i);
                        } catch (ActivityNotFoundException e) {
                            new AlertDialog.Builder(WebActivity.this)
                                    .setTitle("Open")
                                    .setMessage("Can't open files of this type. Try downloading instead.")
                                    .setPositiveButton("OK", (dialog1, which1) -> {
                                    })
                                    .show();
                        }
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> {
                    })
                    .show();
        });
        webview.setFindListener((activeMatchOrdinal, numberOfMatches, isDoneCounting) ->
                searchCount.setText(numberOfMatches == 0 ? "Not found" :
                        String.format("%d / %d", activeMatchOrdinal + 1, numberOfMatches)));
        return webview;
    }


    private void newTabCommon(WebView webview) {
        boolean isDesktopUA = !tabs.isEmpty() && getCurrentTab().isDesktopUA;
        webview.getSettings().setUserAgentString(isDesktopUA ? desktopUA : null);
        webview.getSettings().setUseWideViewPort(isDesktopUA);
        webview.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        webview.setVisibility(View.GONE);
        Tab tab = new Tab(webview);
        tab.isDesktopUA = isDesktopUA;
        tabs.add(tab);
        webviews.addView(webview);
        setTabCountText(tabs.size());
    }

    private void newTab(String url) {
        WebView webview = createWebView(null);
        newTabCommon(webview);
        loadUrl(url, webview);
    }

    public void setTabCountText(int count) {
        TextView tabs_number = findViewById(R.id.tabs_number);
        tabs_number.setText(String.valueOf(count));
    }


    private void showLongPressMenu(String linkUrl, String imageUrl) {
        String url;
        String title;
        String[] options = new String[]{"Open in new tab", "Copy URL", "Show full URL", "Download"};

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
                title = "Image: " + imageUrl;
            } else {
                // Image with link
                url = linkUrl;
                title = linkUrl;
                String[] newOptions = new String[options.length + 1];
                System.arraycopy(options, 0, newOptions, 0, options.length);
                newOptions[newOptions.length - 1] = "Image Options";
                options = newOptions;
            }
        }
        new AlertDialog.Builder(WebActivity.this).setTitle(title).setItems(options, (dialog, which) -> {
            switch (which) {
                case 0:
                    newTab(url);
                    break;
                case 1:
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                    assert clipboard != null;
                    ClipData clipData = ClipData.newPlainText("URL", url);
                    clipboard.setPrimaryClip(clipData);
                    break;
                case 2:
                    new AlertDialog.Builder(WebActivity.this)
                            .setTitle("Full URL")
                            .setMessage(url)
                            .setPositiveButton("OK", (dialog1, which1) -> {
                            })
                            .show();
                    break;
                case 3:
                    startDownload(url, null);
                    break;
                case 4:
                    showLongPressMenu(null, imageUrl);
                    break;
            }
        }).show();
    }

    @SuppressLint("DefaultLocale")
    private static String certificateToStr(SslCertificate certificate) {
        if (certificate == null) {
            return null;
        }
        String s = "";
        SslCertificate.DName issuedTo = certificate.getIssuedTo();
        if (issuedTo != null) {
            s += "Issued to: " + issuedTo.getDName() + "\n";
        }
        SslCertificate.DName issuedBy = certificate.getIssuedBy();
        if (issuedBy != null) {
            s += "Issued by: " + issuedBy.getDName() + "\n";
        }
        Date issueDate = certificate.getValidNotBeforeDate();
        if (issueDate != null) {
            s += String.format("Issued on: %tF %tT %tz\n", issueDate, issueDate, issueDate);
        }
        Date expiryDate = certificate.getValidNotAfterDate();
        if (expiryDate != null) {
            s += String.format("Expires on: %tF %tT %tz\n", expiryDate, expiryDate, expiryDate);
        }
        return s;
    }

    private void startDownload(String url, String filename) {
        if (!hasOrRequestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                null,
                PERMISSION_REQUEST_DOWNLOAD)) {
            return;
        }
        if (filename == null) {
            filename = URLUtil.guessFileName(url, null, null);
        }
        DownloadManager.Request request;
        try {
            request = new DownloadManager.Request(Uri.parse(url));
        } catch (IllegalArgumentException e) {
            new AlertDialog.Builder(WebActivity.this)
                    .setTitle("Can't Download URL")
                    .setMessage(url)
                    .setPositiveButton("OK", (dialog1, which1) -> {
                    })
                    .show();
            return;
        }
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);
        String cookie = CookieManager.getInstance().getCookie(url);
        if (cookie != null) {
            request.addRequestHeader("Cookie", cookie);
        }
        DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        assert dm != null;
        dm.enqueue(request);
    }


    private void loadUrl(String url, WebView webview) {
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
            url = URLUtil.composeSearchUrl(url, searchUrl, "%s");
        }

        webview.loadUrl(url);

        showAndHideKeyboard.hideKeyboard();
    }

    private void tabsLayout() {
        View tabs_layout = findViewById(R.id.tabs_layout);

        tabs_layout.findViewById(R.id.tabs_menu).setOnClickListener(view -> {

        });

        tabs_layout.findViewById(R.id.tabs_new_tab).setOnClickListener(view -> {
            newTab("google.com");
            switchToTab(tabs.size() - 1);
            tabs_layout.setVisibility(View.GONE);
        });

        tabs_layout.findViewById(R.id.close_tabs_menu).setOnClickListener(view -> tabs_layout.setVisibility(View.GONE));
    }


    private void tabRecyclerView() {
        recyclerView = findViewById(R.id.tabs_list);

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
            if (getCurrentWebView().canGoBack()) {
                getCurrentWebView().goBack();
            }
        });

        bottom_bar.findViewById(R.id.forward_btn).setOnClickListener(view -> {
            if (getCurrentWebView().canGoForward()) {
                getCurrentWebView().goForward();
            }
        });

        bottom_bar.findViewById(R.id.home_btn).setOnClickListener(view -> getCurrentWebView().loadUrl("https://google.com"));

        bottom_bar.findViewById(R.id.refresh_btn).setOnClickListener(view -> getCurrentWebView().reload());

        bottom_bar.findViewById(R.id.menu_btn).setOnClickListener(view -> showBottomSheetMenu());
    }

    private void showBottomSheetMenu() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(WebActivity.this);
        View bottomSheetView = LayoutInflater.from(WebActivity.this).inflate(
                R.layout.bottom_sheet_menu,
                null
        );

        bottomSheetView.findViewById(R.id.desktop).setOnClickListener(view -> {
            Tab tab = getCurrentTab();
            tab.isDesktopUA = !tab.isDesktopUA;
            getCurrentWebView().getSettings().setUserAgentString(tab.isDesktopUA ? desktopUA : null);
            getCurrentWebView().getSettings().setUseWideViewPort(tab.isDesktopUA);
            getCurrentWebView().reload();

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
            intent.putExtra(Intent.EXTRA_TEXT, getCurrentWebView().getUrl());
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
            if (CookieManager.getInstance().acceptThirdPartyCookies(getCurrentWebView())) {
                CookieManager cookieManager = CookieManager.getInstance();
                boolean newValue = !cookieManager.acceptThirdPartyCookies(getCurrentWebView());
                cookieManager.setAcceptThirdPartyCookies(getCurrentWebView(), newValue);
            }
            bottomSheetDialog.dismiss();
        });

        bottomSheetView.findViewById(R.id.tab_info).setOnClickListener(view -> {
            String s = "URL: " + getCurrentWebView().getUrl() + "\n";
            s += "Title: " + getCurrentWebView().getTitle() + "\n\n";
            SslCertificate certificate = getCurrentWebView().getCertificate();
            s += certificate == null ? "Not secure" : "Certificate:\n" + certificateToStr(certificate);

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
        onNightModeChange();
    }


    private boolean isUrlInBookmarks(String urlToCheck) {
        if (placesDb == null) return false;
        Cursor cursor = placesDb.rawQuery("SELECT 1 FROM bookmarks WHERE url = ?", new String[]{urlToCheck});

        boolean exists = cursor.moveToFirst();
        cursor.close();

        return exists;
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
                    loadUrl(url, getCurrentWebView());
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

    private void addBookmark() {
        if (placesDb == null) return;
        ContentValues values = new ContentValues(2);
        values.put("title", getCurrentWebView().getTitle());
        values.put("url", getCurrentWebView().getUrl());
        placesDb.insert("bookmarks", null, values);
    }

    private void deleteAllBookmarks() {
        if (placesDb == null) {
            new AlertDialog.Builder(this)
                    .setTitle("Bookmarks error")
                    .setMessage("Can't open bookmarks database")
                    .setPositiveButton("OK", (dialog, which) -> {
                    })
                    .show();
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Delete all bookmarks?")
                .setMessage("This action cannot be undone")
                .setNegativeButton("Cancel", (dialog, which) -> {
                })
                .setPositiveButton("Delete All", (dialog, which) -> placesDb.execSQL("DELETE FROM bookmarks"))
                .show();
    }

    private void clearHistoryCache() {
        WebView v = getCurrentWebView();
        v.clearCache(true);
        v.clearFormData();
        v.clearHistory();
        CookieManager.getInstance().removeAllCookies(null);
        WebStorage.getInstance().deleteAllData();
    }

    private void closeCurrentTab() {
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
        ((FrameLayout) findViewById(R.id.webviews)).removeView(getCurrentWebView());
        getCurrentWebView().destroy();
        tabs.remove(currentTabIndex);
        if (currentTabIndex >= tabs.size()) {
            currentTabIndex = tabs.size() - 1;
        }
        if (currentTabIndex == -1) {
            // We just closed the last tab
            newTab("google.com");
            currentTabIndex = 0;
        }
        getCurrentWebView().setVisibility(View.VISIBLE);
        et.setText(getCurrentWebView().getUrl());
        setTabCountText(tabs.size());
        getCurrentWebView().requestFocus();
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
            newTab("google.com");
            switchToTab(tabs.size() - 1);
        }
    }

    private void onNightModeChange() {
        if (isNightMode) {
            int textColor = Color.rgb(0x61, 0x61, 0x5f);
            int backgroundColor = Color.rgb(0x22, 0x22, 0x22);
            et.setTextColor(textColor);
            et.setBackgroundColor(backgroundColor);
            searchEdit.setTextColor(textColor);
            searchEdit.setBackgroundColor(backgroundColor);
            findViewById(R.id.main_layout).setBackgroundColor(Color.BLACK);
            findViewById(R.id.toolbar).setBackgroundColor(Color.BLACK);
            ((ProgressBar) findViewById(R.id.progressbar)).setProgressTintList(ColorStateList.valueOf(Color.rgb(0, 0x66, 0)));
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setNavigationBarColor(Color.BLACK);
        } else {
            int textColor = Color.BLACK;
            int backgroundColor = Color.rgb(0xe0, 0xe0, 0xe0);
            et.setTextColor(textColor);
            et.setBackgroundColor(backgroundColor);
            searchEdit.setTextColor(textColor);
            searchEdit.setBackgroundColor(backgroundColor);
            findViewById(R.id.main_layout).setBackgroundColor(Color.WHITE);
            findViewById(R.id.toolbar).setBackgroundColor(Color.rgb(0xe0, 0xe0, 0xe0));
            ((ProgressBar) findViewById(R.id.progressbar)).setProgressTintList(ColorStateList.valueOf(Color.rgb(0, 0xcc, 0)));
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        }
        for (int i = 0; i < tabs.size(); i++) {
            tabs.get(i).webview.setBackgroundColor(isNightMode ? Color.BLACK : Color.WHITE);
        }
    }

    private void openUrlInApp() {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(getCurrentWebView().getUrl()));
        try {
            startActivity(i);
        } catch (ActivityNotFoundException e) {
            new AlertDialog.Builder(WebActivity.this)
                    .setTitle("Open in app")
                    .setMessage("No app can open this URL.")
                    .setPositiveButton("OK", (dialog1, which1) -> {
                    })
                    .show();
        }
    }


    @Override
    public void onBackPressed() {
        if (findViewById(R.id.fullScreenVideo).getVisibility() == View.VISIBLE && fullScreenCallback[0] != null) {
            fullScreenCallback[0].onCustomViewHidden();
        } else if (getCurrentWebView().canGoBack()) {
            getCurrentWebView().goBack();
        } else if (tabs.size() > 1) {
            closeCurrentTab();
        } else {
            super.onBackPressed();
        }
    }

    boolean hasOrRequestPermission(String permission, String explanation, int requestCode) {
        if (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) {
            // Permission already granted
            return true;
        }
        if (explanation != null && shouldShowRequestPermissionRationale(permission)) {
            new AlertDialog.Builder(this)
                    .setTitle("Permission Required")
                    .setMessage(explanation)
                    .setPositiveButton("OK", (dialog, which) -> requestPermissions(new String[]{permission}, requestCode))
                    .show();
            return false;
        }
        requestPermissions(new String[]{permission}, requestCode);
        return false;
    }

    interface MyBooleanSupplier {
        boolean getAsBoolean();
    }

    static class SearchAutocompleteAdapter extends BaseAdapter implements Filterable {

        interface OnSearchCommitListener {
            void onSearchCommit(String text);
        }

        private final Context mContext;
        private final OnSearchCommitListener commitListener;
        private List<String> completions = new ArrayList<>();

        SearchAutocompleteAdapter(Context context, OnSearchCommitListener commitListener) {
            mContext = context;
            this.commitListener = commitListener;
        }

        @Override
        public int getCount() {
            return completions.size();
        }

        @Override
        public Object getItem(int position) {
            return completions.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @SuppressLint("ClickableViewAccessibility")
        @Override
        @SuppressWarnings("ConstantConditions")
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(android.R.layout.simple_dropdown_item_1line, parent, false);
            }
            TextView v = convertView.findViewById(android.R.id.text1);
            v.setText(completions.get(position));
            Drawable d = mContext.getResources().getDrawable(R.drawable.commit_search, null);
            int size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32, mContext.getResources().getDisplayMetrics());
            d.setBounds(0, 0, size, size);
            v.setCompoundDrawables(null, null, d, null);
            //noinspection AndroidLintClickableViewAccessibility
            v.setOnTouchListener((v1, event) -> {
                if (event.getAction() != MotionEvent.ACTION_DOWN) {
                    return false;
                }
                TextView t = (TextView) v1;
                if (event.getX() > t.getWidth() - t.getCompoundPaddingRight()) {
                    commitListener.onSearchCommit(getItem(position).toString());
                    return true;
                }
                return false;
            });
            //noinspection AndroidLintClickableViewAccessibility
            parent.setOnTouchListener((dropdown, event) -> {
                if (event.getX() > dropdown.getWidth() - size * 2) {
                    return true;
                }
                return false;
            });
            return convertView;
        }

        @Override
        public Filter getFilter() {
            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    // Invoked on a worker thread
                    FilterResults filterResults = new FilterResults();
                    if (constraint != null) {
                        List<String> results = getCompletions(constraint.toString());
                        filterResults.values = results;
                        filterResults.count = results.size();
                    }
                    return filterResults;
                }

                @Override
                @SuppressWarnings("unchecked")
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    if (results != null && results.count > 0) {
                        completions = (List<String>) results.values;
                        notifyDataSetChanged();
                    } else {
                        notifyDataSetInvalidated();
                    }
                }
            };
        }

        // Runs on a worker thread
        private List<String> getCompletions(String text) {
            int total = 0;
            byte[] data = new byte[16384];
            try {
                URL url = new URL(URLUtil.composeSearchUrl(text, searchCompleteUrl, "%s"));
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    while (total <= data.length) {
                        int count = in.read(data, total, data.length - total);
                        if (count == -1) {
                            break;
                        }
                        total += count;
                    }
                    if (total == data.length) {
                        // overflow
                        return new ArrayList<>();
                    }
                } finally {
                    urlConnection.disconnect();
                }
            } catch (IOException e) {
                // Swallow exception and return empty list
                return new ArrayList<>();
            }

            // Result looks like:
            // [ "original query", ["completion1", "completion2", ...], ...]

            JSONArray jsonArray;
            try {
                jsonArray = new JSONArray(new String(data, StandardCharsets.UTF_8));
            } catch (JSONException e) {
                return new ArrayList<>();
            }
            jsonArray = jsonArray.optJSONArray(1);
            if (jsonArray == null) {
                return new ArrayList<>();
            }
            final int MAX_RESULTS = 10;
            List<String> result = new ArrayList<>(Math.min(jsonArray.length(), MAX_RESULTS));
            for (int i = 0; i < jsonArray.length() && result.size() < MAX_RESULTS; i++) {
                String s = jsonArray.optString(i);
                if (s != null && !s.isEmpty()) {
                    result.add(s);
                }
            }
            return result;
        }
    }
}
