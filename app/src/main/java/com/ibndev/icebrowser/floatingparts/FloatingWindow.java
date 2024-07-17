package com.ibndev.icebrowser.floatingparts;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.Nullable;

import com.ibndev.icebrowser.R;
import com.ibndev.icebrowser.floatingparts.browser.BrowserWindow;
import com.ibndev.icebrowser.floatingparts.browser.WindowTabManager;
import com.ibndev.icebrowser.floatingparts.navbar.WindowNavbar;
import com.ibndev.icebrowser.floatingparts.utilities.FloatingUtils;
import com.ibndev.icebrowser.floatingparts.utilities.NotificationHelper;
import com.ibndev.icebrowser.floatingparts.utilities.OverlayManager;

public class FloatingWindow extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public ViewGroup floatView;
    public WindowManager windowManager;
    public DisplayMetrics metrics;

    @Override
    public void onCreate() {
        super.onCreate();

        NotificationHelper notificationHelper = new NotificationHelper(this, this);
        notificationHelper.startForegroundService();

        createWindow();
    }

    @SuppressLint("InflateParams")
    public void createWindow() {
        metrics = getApplicationContext().getResources().getDisplayMetrics();

        LayoutInflater inflater = (LayoutInflater) getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        floatView = (ViewGroup) inflater.inflate(R.layout.window_main, null);
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int LAYOUT_TYPE = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            FloatingUtils utils = new FloatingUtils(FloatingWindow.this, LAYOUT_TYPE);
            utils.startFloating();

            WindowTabManager tabManager = new WindowTabManager(FloatingWindow.this);

            WindowNavbar navbar = new WindowNavbar(FloatingWindow.this, utils);
            navbar.setNavbar(tabManager);

            new BrowserWindow(this, tabManager);


        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopSelf();
    }
}
