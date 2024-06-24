package com.ibndev.icebrowser;

import android.app.DownloadManager;
import android.os.Bundle;
import android.os.Environment;
import android.webkit.CookieManager;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.SearchView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private static final Pattern URL_PATTERN = Pattern.compile(
            "^(http|https|ftp)://.*$",
            Pattern.CASE_INSENSITIVE
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        WebView webView = findViewById(R.id.main_web_view);
        SearchView searchView = findViewById(R.id.searchView);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query != null) {
                    String url;
                    if (URL_PATTERN.matcher(query).matches() || query.contains(".")) {
                        // It's a URL
                        url = query.startsWith("http://") || query.startsWith("https://") ? query : "http://" + query;
                    } else {
                        // It's a search term
                        url = "https://www.google.com/search?q=" + query;
                    }
                    webView.loadUrl(url);
                    searchView.clearFocus();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Handle query text change if needed
                return false;
            }
        });

        setWebview(webView);
    }

    private void setWebview(WebView webView) {
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setDomStorageEnabled(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });

        // Set a WebChromeClient to handle additional web features
        webView.setWebChromeClient(new WebChromeClient() {
            // Customize behavior if needed
        });

        webView.loadUrl("https://www.google.com");
    }

//    private void startDownload(String url, String filename) {
//        if (!hasOrRequestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,
//                null,
//                PERMISSION_REQUEST_DOWNLOAD)) {
//            return;
//        }
//        if (filename == null) {
//            filename = URLUtil.guessFileName(url, null, null);
//        }
//        DownloadManager.Request request;
//        try {
//            request = new DownloadManager.Request(Uri.parse(url));
//        } catch (IllegalArgumentException e) {
//            new AlertDialog.Builder(MainActivity.this)
//                    .setTitle("Can't Download URL")
//                    .setMessage(url)
//                    .setPositiveButton("OK", (dialog1, which1) -> {})
//                    .show();
//            return;
//        }
//        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
//        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);
//        String cookie = CookieManager.getInstance().getCookie(url);
//        if (cookie != null) {
//            request.addRequestHeader("Cookie", cookie);
//        }
//        DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
//        assert dm != null;
//        dm.enqueue(request);
//    }

}
