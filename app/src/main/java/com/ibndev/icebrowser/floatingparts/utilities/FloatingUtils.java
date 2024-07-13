package com.ibndev.icebrowser.floatingparts.utilities;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.Observer;

import com.ibndev.icebrowser.R;
import com.ibndev.icebrowser.floatingparts.FloatingWindow;

public class FloatingUtils implements SensorEventListener {

    ViewGroup floatView;
    WindowManager windowManager;
    DisplayMetrics metrics;
    boolean isFloatingActive;
    boolean isFloatingShown;
    int LAYOUT_TYPE;
    Context context;

    WindowManager.LayoutParams floatWindowLayoutParam;
    TouchableWrapper touchableWrapper;
    FloatingLayout layout;

    int intwidth;
    int intheight;

    int volumePrev = 0;

    Observer<Boolean> observer;

    private Sensor accelerometer;
    private long lastUpdate;
    private float last_x, last_y, last_z;

    public FloatingUtils(FloatingWindow floatingWindow, int LAYOUT_TYPE) {
        context = floatingWindow.getApplicationContext();
        floatView = floatingWindow.floatView;
        windowManager = floatingWindow.windowManager;
        metrics = floatingWindow.metrics;
        this.LAYOUT_TYPE = LAYOUT_TYPE;

        layout = new FloatingLayout();

        observer = visible -> {
            if (windowManager != null && touchableWrapper != null) {
                if (!visible && isFloatingActive) {
                    windowManager.removeView(touchableWrapper);
                    isBypassMode = false;
                    isFloatingActive = false;
                }
            }
        };

        SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
        lastUpdate = System.currentTimeMillis();
        if (accelerometer != null) {
            assert sensorManager != null;
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }

        OverlayManager.getOverlayVisibility().observeForever(observer);

        setBroadcastReceiver();
    }

    @SuppressLint("ClickableViewAccessibility")
    public void startFloating() {
        isFloatingActive = true;
        isFloatingShown = true;

        int minWidth = (int) (200 * metrics.density);
        int minHeight = (int) (250 * metrics.density);
        intwidth = (int) (330 * metrics.density);
        intheight = (int) (600 * metrics.density);


        layout.width = intwidth;
        layout.height = intheight;

        floatWindowLayoutParam = new WindowManager.LayoutParams(
                intwidth,
                intheight,
                LAYOUT_TYPE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                        WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT
        );

        floatWindowLayoutParam.gravity = Gravity.START | Gravity.TOP;
        floatWindowLayoutParam.x = 0;
        floatWindowLayoutParam.y = 0;

        touchableWrapper = new TouchableWrapper(context);
        touchableWrapper.addView(floatView);


        touchableWrapper.setOutsideTouchListener((view, event) -> {
            // Make the floating window not focusable
            floatWindowLayoutParam.flags =
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
            windowManager.updateViewLayout(touchableWrapper, floatWindowLayoutParam);


            //TODO: Add anti obscure feature with volume and shake sensor
            if (LayoutSetData.isAntiObscureVolume || LayoutSetData.isAntiObscureShake) {
                windowManager.removeView(touchableWrapper);
                OverlayManager.getOverlayVisibility().removeObserver(observer);
                isFloatingActive = false;
            }
            return false;
        });

        // Set the inside touch listener
        touchableWrapper.setInsideTouchListener((view, event) -> {
//            Toast.makeText(context, "Inside", Toast.LENGTH_LONG).show();
            // Make the floating window focusable
            if (LayoutSetData.isNonFocusable) {
                floatWindowLayoutParam.flags =
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
            } else {
                floatWindowLayoutParam.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                        WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
            }


            windowManager.updateViewLayout(touchableWrapper, floatWindowLayoutParam);
            return false;
        });

        touchableWrapper.setOnTouchListener(new View.OnTouchListener() {
            final WindowManager.LayoutParams floatWindowLayoutUpdateParam = floatWindowLayoutParam;
            double x;
            double y;
            double px;
            double py;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        x = floatWindowLayoutUpdateParam.x;
                        y = floatWindowLayoutUpdateParam.y;
                        px = event.getRawX();
                        py = event.getRawY();
                        break;

                    case MotionEvent.ACTION_MOVE:
                        int xPos = (int) ((x + event.getRawX()) - px);
                        int yPos = (int) ((y + event.getRawY()) - py);

                        floatWindowLayoutUpdateParam.x = xPos;
                        floatWindowLayoutUpdateParam.y = yPos;

                        layout.xPos = floatWindowLayoutUpdateParam.x;
                        layout.yPos = floatWindowLayoutUpdateParam.y;

                        windowManager.updateViewLayout(touchableWrapper, floatWindowLayoutUpdateParam);
                        break;

                    case MotionEvent.ACTION_UP:
                        // You may want to add code here for when the touch is released
                        break;
                }
                return true;
            }
        });

        ImageView bottomRightResizer = floatView.findViewById(R.id.window_resizer);
        bottomRightResizer.setOnTouchListener(new View.OnTouchListener() {
            final WindowManager.LayoutParams floatWindowLayoutUpdateParam = floatWindowLayoutParam;
            double initialWidth;
            double initialHeight;
            double initialTouchX;
            double initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialWidth = floatWindowLayoutUpdateParam.width;
                        initialHeight = floatWindowLayoutUpdateParam.height;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        float newWidth = (float) (initialWidth + (event.getRawX() - initialTouchX));
                        float newHeight = (float) (initialHeight + (event.getRawY() - initialTouchY));

                        // Ensure the new width and height are not smaller than the minimum values
                        if (newWidth < minWidth) {
                            newWidth = minWidth;
                        }
                        if (newHeight < minHeight) {
                            newHeight = minHeight;
                        }

                        floatWindowLayoutUpdateParam.width = (int) newWidth;
                        floatWindowLayoutUpdateParam.height = (int) newHeight;

                        layout.width = floatWindowLayoutUpdateParam.width;
                        layout.height = floatWindowLayoutUpdateParam.height;

                        windowManager.updateViewLayout(touchableWrapper, floatWindowLayoutUpdateParam);
                        return true;

                    case MotionEvent.ACTION_UP:
                        // You may want to add code here for when the touch is released
                        return true;
                }
                return false;
            }
        });

        ImageView closeBtn = floatView.findViewById(R.id.window_main_top_navbar_close_button);
        closeBtn.setOnClickListener(view -> Toast.makeText(context, context.getString(R.string.window_close_onclick), Toast.LENGTH_LONG).show());

        closeBtn.setOnLongClickListener(view -> {
            windowManager.removeView(touchableWrapper);
            isFloatingActive = false;
            isBypassMode = false;
            OverlayManager.getOverlayVisibility().removeObserver(observer);
            return true;
        });

        windowManager.addView(touchableWrapper, floatWindowLayoutParam);

    }

    @SuppressLint("ClickableViewAccessibility")
    public void antiObscureFloating() {
        isFloatingActive = true;
        int minWidth = (int) (200 * metrics.density);
        int minHeight = (int) (250 * metrics.density);
        intwidth = layout.width;
        intheight = layout.height;


        floatWindowLayoutParam = new WindowManager.LayoutParams(
                intwidth,
                intheight,
                LAYOUT_TYPE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                        WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT
        );

        floatWindowLayoutParam.gravity = Gravity.START | Gravity.TOP;
        floatWindowLayoutParam.x = layout.xPos;
        floatWindowLayoutParam.y = layout.yPos;

        touchableWrapper.setOutsideTouchListener((view, event) -> {
            // Make the floating window not focusable
            floatWindowLayoutParam.flags =
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
            windowManager.updateViewLayout(touchableWrapper, floatWindowLayoutParam);


            //TODO: Add anti obscure feature with volume and shake sensor
            if (LayoutSetData.isAntiObscureVolume || LayoutSetData.isAntiObscureShake) {
                windowManager.removeView(touchableWrapper);
                OverlayManager.getOverlayVisibility().removeObserver(observer);
                isFloatingActive = false;
            }
            return false;
        });

        // Set the inside touch listener
        touchableWrapper.setInsideTouchListener((view, event) -> {
//            Toast.makeText(context, "Inside", Toast.LENGTH_LONG).show();
            // Make the floating window focusable
            if (LayoutSetData.isNonFocusable) {
                floatWindowLayoutParam.flags =
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
            } else {
                floatWindowLayoutParam.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                        WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
            }


            windowManager.updateViewLayout(touchableWrapper, floatWindowLayoutParam);


            return false;
        });

        touchableWrapper.setOnTouchListener(new View.OnTouchListener() {
            final WindowManager.LayoutParams floatWindowLayoutUpdateParam = floatWindowLayoutParam;
            double x;
            double y;
            double px;
            double py;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        x = floatWindowLayoutUpdateParam.x;
                        y = floatWindowLayoutUpdateParam.y;
                        px = event.getRawX();
                        py = event.getRawY();
                        break;

                    case MotionEvent.ACTION_MOVE:
                        int xPos = (int) ((x + event.getRawX()) - px);
                        int yPos = (int) ((y + event.getRawY()) - py);

                        floatWindowLayoutUpdateParam.x = xPos;
                        floatWindowLayoutUpdateParam.y = yPos;

                        layout.xPos = floatWindowLayoutUpdateParam.x;
                        layout.yPos = floatWindowLayoutUpdateParam.y;

                        windowManager.updateViewLayout(touchableWrapper, floatWindowLayoutUpdateParam);
                        break;

                    case MotionEvent.ACTION_UP:
                        // You may want to add code here for when the touch is released
                        break;
                }
                return true;
            }
        });

        ImageView bottomRightResizer = floatView.findViewById(R.id.window_resizer);
        bottomRightResizer.setOnTouchListener(new View.OnTouchListener() {
            final WindowManager.LayoutParams floatWindowLayoutUpdateParam = floatWindowLayoutParam;
            double initialWidth;
            double initialHeight;
            double initialTouchX;
            double initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialWidth = floatWindowLayoutUpdateParam.width;
                        initialHeight = floatWindowLayoutUpdateParam.height;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        float newWidth = (float) (initialWidth + (event.getRawX() - initialTouchX));
                        float newHeight = (float) (initialHeight + (event.getRawY() - initialTouchY));

                        // Ensure the new width and height are not smaller than the minimum values
                        if (newWidth < minWidth) {
                            newWidth = minWidth;
                        }
                        if (newHeight < minHeight) {
                            newHeight = minHeight;
                        }

                        floatWindowLayoutUpdateParam.width = (int) newWidth;
                        floatWindowLayoutUpdateParam.height = (int) newHeight;

                        layout.width = floatWindowLayoutUpdateParam.width;
                        layout.height = floatWindowLayoutUpdateParam.height;

                        windowManager.updateViewLayout(touchableWrapper, floatWindowLayoutUpdateParam);
                        return true;

                    case MotionEvent.ACTION_UP:
                        // You may want to add code here for when the touch is released
                        return true;
                }
                return false;
            }
        });

        ImageView closeBtn = floatView.findViewById(R.id.window_main_top_navbar_close_button);
        closeBtn.setOnClickListener(view -> Toast.makeText(context, context.getString(R.string.window_close_onclick), Toast.LENGTH_LONG).show());

        closeBtn.setOnLongClickListener(view -> {
            windowManager.removeView(touchableWrapper);
            isFloatingActive = false;
            isBypassMode = false;
            OverlayManager.getOverlayVisibility().removeObserver(observer);
            return true;
        });

        windowManager.addView(touchableWrapper, floatWindowLayoutParam);


    }


    @SuppressLint("ClickableViewAccessibility")
    public void hideFloating() {
        isFloatingShown = false;
        intwidth = (int) (50 * metrics.density);
        intheight = (int) (50 * metrics.density);

        floatWindowLayoutParam = new WindowManager.LayoutParams(
                intwidth,
                intheight,
                LAYOUT_TYPE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                        WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT
        );


        floatWindowLayoutParam.gravity = Gravity.START | Gravity.TOP;
        floatWindowLayoutParam.x = layout.xPos;
        floatWindowLayoutParam.y = layout.yPos;

        windowManager.updateViewLayout(touchableWrapper, floatWindowLayoutParam);

        final GestureDetector gestureDetector = new GestureDetector(floatView.getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapConfirmed(@NonNull MotionEvent e) {
                // Handle the click event
                // Do something when the floating view is clicked
                // Example: show a toast
                if (!LayoutSetData.isLongClick) {
                    showFloating();
                }
                return true;
            }

            @Override
            public void onLongPress(@NonNull MotionEvent e) {
                if (LayoutSetData.isLongClick) {
                    showFloating();
                }

            }

            @Override
            public boolean onDown(@NonNull MotionEvent e) {
                return true;
            }
        });


        if (LayoutSetData.isStaticBubble) {
            floatView.setOnTouchListener(null);


        } else {
            floatView.setOnClickListener(null);
            floatView.setOnTouchListener(new View.OnTouchListener() {
                final WindowManager.LayoutParams floatWindowLayoutUpdateParam = floatWindowLayoutParam;
                double x;
                double y;
                double px;
                double py;

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    gestureDetector.onTouchEvent(event);

                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            x = floatWindowLayoutUpdateParam.x;
                            y = floatWindowLayoutUpdateParam.y;
                            px = event.getRawX();
                            py = event.getRawY();
                            break;

                        case MotionEvent.ACTION_MOVE:
                            int xPos = (int) ((x + event.getRawX()) - px);
                            int yPos = (int) ((y + event.getRawY()) - py);

                            floatWindowLayoutUpdateParam.x = xPos;
                            floatWindowLayoutUpdateParam.y = yPos;

                            layout.xPos = floatWindowLayoutUpdateParam.x;
                            layout.yPos = floatWindowLayoutUpdateParam.y;

                            windowManager.updateViewLayout(touchableWrapper, floatWindowLayoutUpdateParam);
                            break;
                    }
                    return true;
                }
            });
        }

        if (LayoutSetData.isLongClick) {
            floatView.setOnClickListener(null);
            floatView.setOnLongClickListener(view -> {
                showFloating();
                return true;
            });
        } else {
            floatView.setOnClickListener(view -> showFloating());
        }

        floatView.findViewById(R.id.window_main_browser_layout).setVisibility(View.GONE);

        ImageView bubble = floatView.findViewById(R.id.window_ice_browser_bubble);
        bubble.setVisibility(View.VISIBLE);
        if (LayoutSetData.isHiddenMode) {
            bubble.setImageAlpha(0);
        } else {
            bubble.setImageAlpha(1000);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    public void showFloating() {
        isFloatingShown = false;
        isBypassMode = false;
        intwidth = layout.width;
        intheight = layout.height;
        int minWidth = (int) (200 * metrics.density);
        int minHeight = (int) (250 * metrics.density);

        floatWindowLayoutParam = new WindowManager.LayoutParams(
                intwidth
                , intheight,
                LAYOUT_TYPE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                        WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT);

        floatWindowLayoutParam.gravity = Gravity.START | Gravity.TOP;
        floatWindowLayoutParam.x = layout.xPos;
        floatWindowLayoutParam.y = layout.yPos;

        windowManager.updateViewLayout(touchableWrapper, floatWindowLayoutParam);

        floatView.findViewById(R.id.window_main_browser_layout).setVisibility(View.VISIBLE);
        floatView.findViewById(R.id.window_ice_browser_bubble).setVisibility(View.GONE);

        floatView.setOnTouchListener(new View.OnTouchListener() {
            final WindowManager.LayoutParams floatWindowLayoutUpdateParam = floatWindowLayoutParam;
            double x;
            double y;
            double px;
            double py;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        x = floatWindowLayoutUpdateParam.x;
                        y = floatWindowLayoutUpdateParam.y;
                        px = event.getRawX();
                        py = event.getRawY();
                        break;

                    case MotionEvent.ACTION_MOVE:
                        int xPos = (int) ((x + event.getRawX()) - px);
                        int yPos = (int) ((y + event.getRawY()) - py);

                        floatWindowLayoutUpdateParam.x = xPos;
                        floatWindowLayoutUpdateParam.y = yPos;

                        layout.xPos = floatWindowLayoutUpdateParam.x;
                        layout.yPos = floatWindowLayoutUpdateParam.y;

                        windowManager.updateViewLayout(touchableWrapper, floatWindowLayoutUpdateParam);
                        break;

                    case MotionEvent.ACTION_UP:
                        // You may want to add code here for when the touch is released
                        break;
                }
                return true;
            }
        });

        ImageView bottomRightResizer = floatView.findViewById(R.id.window_resizer);
        bottomRightResizer.setOnTouchListener(new View.OnTouchListener() {
            final WindowManager.LayoutParams floatWindowLayoutUpdateParam = floatWindowLayoutParam;
            double initialWidth;
            double initialHeight;
            double initialTouchX;
            double initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialWidth = floatWindowLayoutUpdateParam.width;
                        initialHeight = floatWindowLayoutUpdateParam.height;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        float newWidth = (float) (initialWidth + (event.getRawX() - initialTouchX));
                        float newHeight = (float) (initialHeight + (event.getRawY() - initialTouchY));

                        // Ensure the new width and height are not smaller than the minimum values
                        if (newWidth < minWidth) {
                            newWidth = minWidth;
                        }
                        if (newHeight < minHeight) {
                            newHeight = minHeight;
                        }

                        floatWindowLayoutUpdateParam.width = (int) newWidth;
                        floatWindowLayoutUpdateParam.height = (int) newHeight;

                        layout.width = floatWindowLayoutUpdateParam.width;
                        layout.height = floatWindowLayoutUpdateParam.height;

                        windowManager.updateViewLayout(touchableWrapper, floatWindowLayoutUpdateParam);
                        return true;

                    case MotionEvent.ACTION_UP:
                        // You may want to add code here for when the touch is released
                        return true;
                }
                return false;
            }
        });
    }


    boolean isBypassMode;

    protected void updateLayout() {
        Handler handler = new Handler();
        handler.postDelayed(() -> {

            if (isBypassMode) {
                windowManager.removeView(touchableWrapper);
                windowManager.addView(touchableWrapper, floatWindowLayoutParam);

                windowManager.updateViewLayout(touchableWrapper, floatWindowLayoutParam);
                updateLayout();
            }

        }, 5000);
    }

    public void bypassFloating() {
        if (isBypassMode) {
            isBypassMode = false;
            showFloating();
        } else {
            isBypassMode = true;
        }

        hideFloating();
        updateLayout();
    }


    private void setBroadcastReceiver() {
        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                /*
                This function is only activated if volume is changed
                 */

                if ("android.media.VOLUME_CHANGED_ACTION".equals(intent.getAction())) {

                    int volume = intent.getIntExtra("android.media.EXTRA_VOLUME_STREAM_VALUE", 0);

                    if (!isFloatingActive && LayoutSetData.isAntiObscureVolume) {
                        OverlayManager.getOverlayVisibility().observeForever(observer);
                        antiObscureFloating();
                        if(!isFloatingShown){
                            hideFloating();
                        }
                        isFloatingActive = true;
                    }
                    volumePrev = volume;


                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction("android.media.VOLUME_CHANGED_ACTION");
        context.registerReceiver(broadcastReceiver, filter);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        int SHAKE_THRESHOLD = 800;
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            long curTime = System.currentTimeMillis();
            if ((curTime - lastUpdate) > 100) {
                long diffTime = (curTime - lastUpdate);
                lastUpdate = curTime;

                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];

                float deltaX = x - last_x;
                float deltaY = y - last_y;
                float deltaZ = z - last_z;

                last_x = x;
                last_y = y;
                last_z = z;

                float speed = Math.abs(deltaX + deltaY + deltaZ) / diffTime * 10000;

                if (speed > SHAKE_THRESHOLD) {
                    onShake();
                }
            }
        }
    }

    private void onShake() {
        if (LayoutSetData.isAntiObscureShake && !isFloatingActive) {
            OverlayManager.getOverlayVisibility().observeForever(observer);
            antiObscureFloating();
            isFloatingActive = true;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
