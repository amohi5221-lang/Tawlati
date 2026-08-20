package com.tawlati.app.database;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.room.*;
import androidx.sqlite.db.SupportSQLiteDatabase;
import com.tawlati.app.models.AppSettings;
import com.tawlati.app.models.TableModel;
import com.tawlati.app.models.WaitingCustomer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(
    entities = {TableModel.class, WaitingCustomer.class, AppSettings.class},
    version = 1,
    exportSchema = false
)
public abstract class TawlatiDatabase extends RoomDatabase {

    public abstract TableDao tableDao();
    public abstract WaitingListDao waitingListDao();
    public abstract SettingsDao settingsDao();

    private static volatile TawlatiDatabase INSTANCE;
    public static final ExecutorService databaseExecutor =
        Executors.newFixedThreadPool(4);

    public static TawlatiDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (TawlatiDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                        context.getApplicationContext(),
                        TawlatiDatabase.class,
                        "tawlati_database"
                    )
                    .addCallback(new RoomDatabase.Callback() {
                        @Override
                        public void onCreate(@NonNull SupportSQLiteDatabase db) {
                            super.onCreate(db);
                            // ✅ الإصلاح: تمرير INSTANCE بشكل صريح بعد البناء
                            databaseExecutor.execute(() -> {
                                TawlatiDatabase database = INSTANCE;
                                if (database == null) return;

                                AppSettings defaultSettings = new AppSettings();
                                database.settingsDao().insert(defaultSettings);

                                // توليد الطاولات الافتراضية
                                generateTablesInternal(database, 20, 15);
                            });
                        }
                    })
                    .build();
                }
            }
        }
        return INSTANCE;
    }

    // ✅ الإصلاح: نسخة داخلية تأخذ database كمعامل لتجنب NullPointerException
    private static void generateTablesInternal(TawlatiDatabase database,
                                                int indoorCount, int outdoorCount) {
        database.tableDao().deleteAll();
        List<TableModel> tables = new ArrayList<>();
        int counter = 1;
        for (int i = 0; i < indoorCount; i++) {
            TableModel t = new TableModel();
            t.tableNumber = String.format("T%02d", counter++);
            t.area     = "indoor";
            t.capacity = 4;
            t.status   = "available";
            tables.add(t);
        }
        for (int i = 0; i < outdoorCount; i++) {
            TableModel t = new TableModel();
            t.tableNumber = String.format("T%02d", counter++);
            t.area     = "outdoor";
            t.capacity = 6;
            t.status   = "available";
            tables.add(t);
        }
        database.tableDao().insertAll(tables);
    }

    // ✅ نسخة عامة للاستدعاء من الخارج
    public static void generateDefaultTables(int indoorCount, int outdoorCount) {
        databaseExecutor.execute(() -> {
            TawlatiDatabase database = INSTANCE;
            if (database == null) return;
            generateTablesInternal(database, indoorCount, outdoorCount);
        });
    }
}
