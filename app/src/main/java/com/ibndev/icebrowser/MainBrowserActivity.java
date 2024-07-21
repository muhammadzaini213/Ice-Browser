package com.ibndev.icebrowser;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.VideoController;
import com.google.android.gms.ads.VideoOptions;
import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAdOptions;

import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;


import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ibndev.icebrowser.browserparts.bottom.navbar.BottomBar;
import com.ibndev.icebrowser.browserparts.top.TopBar;
import com.ibndev.icebrowser.browserparts.top.TopPopupMenu;
import com.ibndev.icebrowser.browserparts.top.autocomplete.SearchAutoComplete;
import com.ibndev.icebrowser.browserparts.top.tab.TabManager;
import com.ibndev.icebrowser.browserparts.top.tab.setup.TabRecyclerView;
import com.ibndev.icebrowser.browserparts.top.tab.setup.TabsLayout;
import com.ibndev.icebrowser.floatingparts.utilities.IntersititialAdLoader;
import com.ibndev.icebrowser.setup.FullScreen;
import com.ibndev.icebrowser.setup.permission.PermissionCodes;
import com.ibndev.icebrowser.utilities.Statics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainBrowserActivity extends Activity {

    private final List<String> tabTitles = new ArrayList<>();
    private final List<Bitmap> tabFavicon = new ArrayList<>();
    SearchAutoComplete autoComplete;
    TabRecyclerView recyclerView;
    private ValueCallback<Uri[]> fileUploadCallback;
    private boolean fileUploadCallbackShouldReset;
    private TabManager tabManager;

    @SuppressLint("HardwareIds")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        IntersititialAdLoader adLoader = new IntersititialAdLoader();
        adLoader.loadOpenAppAd(this, this);

        TopPopupMenu menuHelper = new TopPopupMenu(this);

        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(visibility -> new FullScreen(getWindow()));

        tabManager = new TabManager(this);
        recyclerView = new TabRecyclerView(this, tabManager, tabTitles, tabFavicon);
        autoComplete = new SearchAutoComplete(this, tabManager);

        new TabsLayout(this).tabsLayout(tabManager);
        new BottomBar(this, tabManager);
        new TopBar(this, tabManager, recyclerView, tabTitles, menuHelper);

        tabManager.newTab("google.com");

        tabManager.getCurrentWebView().setVisibility(View.VISIBLE);
        tabManager.getCurrentWebView().requestFocus();

        SharedPreferences sharedPreferences = getSharedPreferences("ICE_BROWSER", Activity.MODE_PRIVATE);
        Statics.isPremium = sharedPreferences.getBoolean("PREMIUM", false);

        if (!Statics.isPremium) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference userDocRef = db.collection("users").document("free");

            userDocRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        boolean active = Boolean.TRUE.equals(document.getBoolean("active"));
                        if (!active) {
                            Toast.makeText(this, "Masa pengetesan untuk Ice Browser sudah selesai, terimakasih sudah berpartisipasi!", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    }
                }
            });
        } else {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference userID = db.collection("users").document(sharedPreferences.getString("LICENSE_ID", "free"));
            userID.get().addOnCompleteListener(task -> {
                if(task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if(document.exists()) {
                        String android_id = document.getString("android_id");
                        boolean active = Boolean.TRUE.equals(document.getBoolean("isPremium"));

                        if(!active){
                            Toast.makeText(this, getString(R.string.license_deactivated), Toast.LENGTH_LONG).show();
                            Statics.isPremium = false;
                            sharedPreferences.edit().putBoolean("PREMIUM", false).apply();
                            sharedPreferences.edit().putString("LICENSE_ID", "").apply();
                        }
                        if(!android_id.equals(Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID))){
                            Toast.makeText(this, getString(R.string.license_used), Toast.LENGTH_LONG).show();
                            Statics.isPremium = false;
                            sharedPreferences.edit().putBoolean("PREMIUM", false).apply();
                            sharedPreferences.edit().putString("LICENSE_ID", "").apply();
                        }
                    }
                }
            });
        }

        if(Statics.isPremium){
            Toast.makeText(this, getString(R.string.license_id_activated), Toast.LENGTH_LONG).show();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PermissionCodes.FORM_FILE_CHOOSER) {
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


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PermissionCodes.PERMISSION_REQUEST_DOWNLOAD) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                return;
            }
            new AlertDialog.Builder(this)
                    .setTitle("Permission Denied")
                    .setMessage("The app needs storage permission to download files.")
                    .setPositiveButton("OK", (dialog, which) -> {
                    })
                    .show();

        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        String url = autoComplete.getUrlFromIntent(intent);
        if (!url.isEmpty()) {
            tabManager.newTab("google.com");
            tabManager.switchToTab(tabManager.tabs.size() - 1);
        }
    }

    @Override
    public void onBackPressed() {
        if (findViewById(R.id.main_fullScreenVideo).getVisibility() == View.VISIBLE && tabManager.fullScreenCallback[0] != null) {
            tabManager.fullScreenCallback[0].onCustomViewHidden();
        } else if (tabManager.getCurrentWebView().canGoBack()) {
            tabManager.getCurrentWebView().goBack();
        } else if (tabManager.tabs.size() > 1) {
            tabManager.closeCurrentTab();
        } else {
            super.onBackPressed();
        }
    }


}
