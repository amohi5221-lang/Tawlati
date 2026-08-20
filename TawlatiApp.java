package com.tawlati.app;

import android.app.Application;
import com.tawlati.app.utils.NotificationHelper;

public class TawlatiApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        NotificationHelper.createNotificationChannels(this);
    }
}
