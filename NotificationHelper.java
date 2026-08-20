package com.tawlati.app.utils;

import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.tawlati.app.R;
import com.tawlati.app.ui.MainActivity;

public class NotificationHelper {

    public static final String CHANNEL_ID_ALERTS  = "tawlati_alerts";
    public static final String CHANNEL_ID_EXPIRED = "tawlati_expired";

    public static void createNotificationChannels(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm =
                context.getSystemService(NotificationManager.class);

            NotificationChannel alertChannel = new NotificationChannel(
                CHANNEL_ID_ALERTS, "تنبيهات الحجز",
                NotificationManager.IMPORTANCE_HIGH);
            alertChannel.setDescription("تنبيهات قبل انتهاء وقت الحجز");
            alertChannel.enableVibration(true);
            nm.createNotificationChannel(alertChannel);

            NotificationChannel expiredChannel = new NotificationChannel(
                CHANNEL_ID_EXPIRED, "انتهاء الحجز",
                NotificationManager.IMPORTANCE_MAX);
            expiredChannel.setDescription("إشعارات عند انتهاء وقت الحجز");
            expiredChannel.enableVibration(true);
            nm.createNotificationChannel(expiredChannel);
        }
    }

    public static void showPreEndAlert(Context context, String tableNumber,
                                        String customerName, int minutesLeft, int notifId) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context, notifId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        String text = String.format("متبقي %d دقيقة - طاولة %s - %s",
            minutesLeft, tableNumber, customerName != null ? customerName : "");

        NotificationCompat.Builder builder =
            new NotificationCompat.Builder(context, CHANNEL_ID_ALERTS)
                .setSmallIcon(R.drawable.ic_table_notification)
                .setContentTitle("⚠️ تنبيه انتهاء الحجز قريب")
                .setContentText(text)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSound(soundUri)
                .setVibrate(new long[]{0, 500, 200, 500})
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        try {
            NotificationManagerCompat.from(context).notify(notifId, builder.build());
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    public static void showExpiredAlert(Context context, String tableNumber,
                                         String customerName, int notifId) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context, notifId + 1000, intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        String text = String.format("انتهى وقت طاولة %s - %s - يرجى التفريغ",
            tableNumber, customerName != null ? customerName : "");

        NotificationCompat.Builder builder =
            new NotificationCompat.Builder(context, CHANNEL_ID_EXPIRED)
                .setSmallIcon(R.drawable.ic_table_notification)
                .setContentTitle("🔴 انتهى وقت الحجز!")
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setVibrate(new long[]{0, 1000, 500, 1000, 500, 1000})
                .setAutoCancel(false)
                .setOngoing(true)
                .setContentIntent(pendingIntent);

        try {
            NotificationManagerCompat.from(context).notify(notifId + 1000, builder.build());
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    // ✅ اسم موحد يستخدمه MainActivity
    public static void cancelNotification(Context context, int notifId) {
        NotificationManagerCompat nm = NotificationManagerCompat.from(context);
        nm.cancel(notifId);
        nm.cancel(notifId + 1000);
    }
}
