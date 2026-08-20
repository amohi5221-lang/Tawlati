package com.tawlati.app.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.tawlati.app.database.TawlatiDatabase;
import com.tawlati.app.models.AppSettings;
import com.tawlati.app.models.TableModel;
import java.util.List;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null) return;

        // ✅ إعادة جدولة جميع الحجوزات بعد إعادة تشغيل الهاتف
        if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            rescheduleAllBookings(context);
            return;
        }

        int tableId = intent.getIntExtra(AlarmHelper.EXTRA_TABLE_ID, -1);
        String tableNumber = intent.getStringExtra(AlarmHelper.EXTRA_TABLE_NUMBER);
        String customerName = intent.getStringExtra(AlarmHelper.EXTRA_CUSTOMER_NAME);

        if (tableId == -1) return;

        if (AlarmHelper.ACTION_BOOKING_ALERT.equals(action)) {
            int minutesLeft = intent.getIntExtra(AlarmHelper.EXTRA_MINUTES_LEFT, 10);
            NotificationHelper.showPreEndAlert(context, tableNumber, customerName,
                minutesLeft, tableId);

        } else if (AlarmHelper.ACTION_BOOKING_EXPIRED.equals(action)) {
            NotificationHelper.showExpiredAlert(context, tableNumber, customerName, tableId);
            // تحديث حالة الطاولة إلى منتهية
            TawlatiDatabase.databaseExecutor.execute(() -> {
                TawlatiDatabase db = TawlatiDatabase.getInstance(context);
                TableModel table = db.tableDao().getTableById(tableId);
                if (table != null && table.isOccupied()) {
                    table.status = "expired";
                    db.tableDao().update(table);
                }
            });
        }
    }

    // ✅ الإصلاح: تنفيذ إعادة الجدولة بالكامل
    private void rescheduleAllBookings(Context context) {
        TawlatiDatabase.databaseExecutor.execute(() -> {
            TawlatiDatabase db = TawlatiDatabase.getInstance(context);
            AppSettings settings = db.settingsDao().getSettingsSync();
            int alertBefore = (settings != null) ? settings.alertBeforeMinutes : 10;

            // جلب جميع الطاولات المشغولة
            List<TableModel> occupiedTables = db.tableDao().getOccupiedTablesSync();
            if (occupiedTables == null) return;

            long now = System.currentTimeMillis();
            for (TableModel table : occupiedTables) {
                if (table.bookingEndTime > now) {
                    // الحجز لم ينته بعد - أعد جدولة التنبيهات
                    AlarmHelper.scheduleBookingAlarms(context, table, alertBefore);
                } else {
                    // الحجز انتهى أثناء إيقاف تشغيل الهاتف
                    table.status = "expired";
                    db.tableDao().update(table);
                    NotificationHelper.showExpiredAlert(
                        context, table.tableNumber, table.customerName, table.id);
                }
            }
        });
    }
}
