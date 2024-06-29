package com.ibndev.icebrowser.browserparts.bottom.sheet.bookmark;

import android.graphics.Bitmap;

public class BookmarkData {
    private String title;
    private String url;
    private Bitmap favicon;

    public BookmarkData(String title, String url, Bitmap favicon) {
        this.title = title;
        this.url = url;
        this.favicon = favicon;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    public Bitmap getFavicon() {
        return favicon;
    }
}
