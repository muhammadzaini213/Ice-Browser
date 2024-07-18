package com.ibndev.icebrowser.floatingparts.utilities;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import com.ibndev.icebrowser.MainBrowserActivity;
import com.ibndev.icebrowser.R;
import com.ibndev.icebrowser.floatingparts.FloatingWindow;

public class NotificationHelper {

    private static final int NOTIFICATION_ID = 2113;

    Context context;
    FloatingWindow service;

    public NotificationHelper(Context context, FloatingWindow service) {
        this.context = context;
        this.service = service;
    }

    private void createNotificationChannel() {

        NotificationChannel serviceChannel = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            serviceChannel = new NotificationChannel(
                    context.getString(R.string.app_name),
                    context.getString(R.string.overlay_running),
                    NotificationManager.IMPORTANCE_HIGH // Ensure high importance for visibility
            );

            serviceChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC); // Ensure visibility on lockscreen
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);

        }
    }

    private Notification buildNotification() {
        Intent notificationIntent = new Intent(context, IntentReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE // Update flag usage
        );


        return new NotificationCompat.Builder(context, context.getString(R.string.app_name))
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(context.getString(R.string.overlay_running))
                .setSmallIcon(R.drawable.bottomsheet_menu_overlay_icon)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .addAction(R.drawable.tabs_close_icon, context.getString(R.string.window_popup_layout_set_item_close_all_window), pendingIntent)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .build();
    }

    public void startForegroundService() {
        createNotificationChannel();
        Notification notification = buildNotification();
        service.startForeground(NOTIFICATION_ID, notification);
    }
}
