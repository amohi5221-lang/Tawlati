package com.tawlati.app.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import com.tawlati.app.models.TableModel;

public class AlarmHelper {

    public static final String ACTION_BOOKING_ALERT   = "com.tawlati.BOOKING_ALERT";
    public static final String ACTION_BOOKING_EXPIRED = "com.tawlati.BOOKING_EXPIRED";
    public static final String EXTRA_TABLE_ID       = "table_id";
    public static final String EXTRA_TABLE_NUMBER   = "table_number";
    public static final String EXTRA_CUSTOMER_NAME  = "customer_name";
    public static final String EXTRA_MINUTES_LEFT   = "minutes_left";

    // ✅ الإصلاح: requestCode فريد لكل طاولة ونوع تنبيه
    private static final int ALERT_REQUEST_BASE   = 10000;
    private static final int EXPIRED_REQUEST_BASE = 20000;

    public static void scheduleBookingAlarms(Context context, TableModel table,
                                              int alertBeforeMinutes) {
        AlarmManager alarmManager =
            (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        long now = System.currentTimeMillis();

        // جدولة تنبيه ما قبل الانتهاء
        long preAlertTime = table.bookingEndTime - (alertBeforeMinutes * 60000L);
        if (preAlertTime > now) {
            Intent preAlertIntent = new Intent(context, AlarmReceiver.class);
            preAlertIntent.setAction(ACTION_BOOKING_ALERT);
            preAlertIntent.putExtra(EXTRA_TABLE_ID,      table.id);
            preAlertIntent.putExtra(EXTRA_TABLE_NUMBER,  table.tableNumber);
            preAlertIntent.putExtra(EXTRA_CUSTOMER_NAME, table.customerName);
            preAlertIntent.putExtra(EXTRA_MINUTES_LEFT,  alertBeforeMinutes);

            PendingIntent preAlertPending = PendingIntent.getBroadcast(
                context,
                ALERT_REQUEST_BASE + table.id,   // ✅ requestCode فريد
                preAlertIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            setExactAlarm(alarmManager, preAlertTime, preAlertPending);
        }

        // جدولة تنبيه الانتهاء
        if (table.bookingEndTime > now) {
            Intent expiredIntent = new Intent(context, AlarmReceiver.class);
            expiredIntent.setAction(ACTION_BOOKING_EXPIRED);
            expiredIntent.putExtra(EXTRA_TABLE_ID,      table.id);
            expiredIntent.putExtra(EXTRA_TABLE_NUMBER,  table.tableNumber);
            expiredIntent.putExtra(EXTRA_CUSTOMER_NAME, table.customerName);

            PendingIntent expiredPending = PendingIntent.getBroadcast(
                context,
                EXPIRED_REQUEST_BASE + table.id,  // ✅ requestCode فريد ومختلف
                expiredIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            setExactAlarm(alarmManager, table.bookingEndTime, expiredPending);
        }
    }

    public static void cancelBookingAlarms(Context context, int tableId) {
        AlarmManager alarmManager =
            (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        // إلغاء تنبيه ما قبل الانتهاء
        Intent preAlertIntent = new Intent(context, AlarmReceiver.class);
        preAlertIntent.setAction(ACTION_BOOKING_ALERT);
        PendingIntent preAlertPending = PendingIntent.getBroadcast(
            context,
            ALERT_REQUEST_BASE + tableId,
            preAlertIntent,
            PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
        );
        if (preAlertPending != null) {
            alarmManager.cancel(preAlertPending);
            preAlertPending.cancel();
        }

        // إلغاء تنبيه الانتهاء
        Intent expiredIntent = new Intent(context, AlarmReceiver.class);
        expiredIntent.setAction(ACTION_BOOKING_EXPIRED);
        PendingIntent expiredPending = PendingIntent.getBroadcast(
            context,
            EXPIRED_REQUEST_BASE + tableId,
            expiredIntent,
            PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
        );
        if (expiredPending != null) {
            alarmManager.cancel(expiredPending);
            expiredPending.cancel();
        }
    }

    private static void setExactAlarm(AlarmManager am, long triggerTime, PendingIntent pi) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pi);
        } else {
            am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pi);
        }
    }
}
