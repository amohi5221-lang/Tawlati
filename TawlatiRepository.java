package com.tawlati.app.database;

import android.app.Application;
import androidx.lifecycle.LiveData;
import com.tawlati.app.models.AppSettings;
import com.tawlati.app.models.TableModel;
import com.tawlati.app.models.WaitingCustomer;
import java.util.List;

public class TawlatiRepository {

    private final TableDao tableDao;
    private final WaitingListDao waitingListDao;
    private final SettingsDao settingsDao;

    public TawlatiRepository(Application application) {
        TawlatiDatabase db = TawlatiDatabase.getInstance(application);
        tableDao      = db.tableDao();
        waitingListDao= db.waitingListDao();
        settingsDao   = db.settingsDao();
    }

    // ===== TABLE OPERATIONS =====

    public LiveData<List<TableModel>> getAllTables() {
        return tableDao.getAllTables();
    }

    public LiveData<Integer> getAvailableCount() {
        return tableDao.getAvailableCount();
    }

    public LiveData<Integer> getAvailableIndoorCount() {
        return tableDao.getAvailableIndoorCount();
    }

    public LiveData<Integer> getAvailableOutdoorCount() {
        return tableDao.getAvailableOutdoorCount();
    }

    public LiveData<Integer> getOccupiedCount() {
        return tableDao.getOccupiedCount();
    }

    // ✅ async version (for ViewModel normal calls)
    public void bookTable(TableModel table, String customerName, String customerPhone,
                          int guestsCount, String area, int durationMin) {
        TawlatiDatabase.databaseExecutor.execute(() -> {
            table.customerName      = customerName;
            table.customerPhone     = customerPhone;
            table.guestsCount       = guestsCount;
            table.area              = area;
            table.bookingStartTime  = System.currentTimeMillis();
            table.bookingDurationMin= durationMin;
            table.bookingEndTime    = table.bookingStartTime + (durationMin * 60000L);
            table.status            = "occupied";
            tableDao.update(table);
        });
    }

    // ✅ sync version (for BookingActivity direct thread use)
    public void updateTableSync(TableModel table) {
        tableDao.update(table);
    }

    public void freeTable(int tableId) {
        TawlatiDatabase.databaseExecutor.execute(() -> tableDao.freeTable(tableId));
    }

    public void updateTable(TableModel table) {
        TawlatiDatabase.databaseExecutor.execute(() -> tableDao.update(table));
    }

    public TableModel getTableByIdSync(int tableId) {
        return tableDao.getTableById(tableId);
    }

    public List<TableModel> getAvailableTablesByArea(String area) {
        return tableDao.getAvailableTablesByArea(area);
    }

    // ===== WAITING LIST OPERATIONS =====

    public LiveData<List<WaitingCustomer>> getWaitingCustomers() {
        return waitingListDao.getWaitingCustomers();
    }

    public LiveData<Integer> getWaitingCount() {
        return waitingListDao.getWaitingCount();
    }

    public void addToWaitingList(WaitingCustomer customer) {
        TawlatiDatabase.databaseExecutor.execute(() -> waitingListDao.insert(customer));
    }

    public void updateWaitingCustomerStatus(int customerId, String status) {
        TawlatiDatabase.databaseExecutor.execute(() ->
            waitingListDao.updateStatus(customerId, status));
    }

    public WaitingCustomer getFirstInQueue() {
        return waitingListDao.getFirstInQueue();
    }

    public List<WaitingCustomer> getWaitingCustomersSync() {
        return waitingListDao.getWaitingCustomersSync();
    }

    // ===== SETTINGS OPERATIONS =====

    public LiveData<AppSettings> getSettings() {
        return settingsDao.getSettings();
    }

    public AppSettings getSettingsSync() {
        return settingsDao.getSettingsSync();
    }

    public void updateSettings(AppSettings settings) {
        TawlatiDatabase.databaseExecutor.execute(() -> settingsDao.update(settings));
    }

    public void regenerateTables(int indoorCount, int outdoorCount) {
        TawlatiDatabase.generateDefaultTables(indoorCount, outdoorCount);
    }
}
