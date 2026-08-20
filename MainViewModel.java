package com.tawlati.app.viewmodels;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.tawlati.app.database.TawlatiRepository;
import com.tawlati.app.models.AppSettings;
import com.tawlati.app.models.TableModel;
import com.tawlati.app.models.WaitingCustomer;
import java.util.List;

public class MainViewModel extends AndroidViewModel {

    private final TawlatiRepository repository;

    public final LiveData<List<TableModel>> allTables;
    public final LiveData<Integer> availableCount;
    public final LiveData<Integer> availableIndoorCount;
    public final LiveData<Integer> availableOutdoorCount;
    public final LiveData<Integer> occupiedCount;
    public final LiveData<List<WaitingCustomer>> waitingCustomers;
    public final LiveData<Integer> waitingCount;
    public final LiveData<AppSettings> settings;

    public MainViewModel(Application application) {
        super(application);
        repository = new TawlatiRepository(application);

        allTables           = repository.getAllTables();
        availableCount      = repository.getAvailableCount();
        availableIndoorCount= repository.getAvailableIndoorCount();
        availableOutdoorCount= repository.getAvailableOutdoorCount();
        occupiedCount       = repository.getOccupiedCount();
        waitingCustomers    = repository.getWaitingCustomers();
        waitingCount        = repository.getWaitingCount();
        settings            = repository.getSettings();
    }

    public void bookTable(TableModel table, String name, String phone,
                          int guests, String area, int duration) {
        repository.bookTable(table, name, phone, guests, area, duration);
    }

    // ✅ sync version للاستخدام من background thread مباشرة
    public void updateTableSync(TableModel table) {
        repository.updateTableSync(table);
    }

    public void freeTable(int tableId) {
        repository.freeTable(tableId);
    }

    public void updateTable(TableModel table) {
        repository.updateTable(table);
    }

    public TableModel getTableByIdSync(int tableId) {
        return repository.getTableByIdSync(tableId);
    }

    public List<TableModel> getAvailableTablesByArea(String area) {
        return repository.getAvailableTablesByArea(area);
    }

    public void addToWaitingList(WaitingCustomer customer) {
        repository.addToWaitingList(customer);
    }

    public void updateWaitingStatus(int customerId, String status) {
        repository.updateWaitingCustomerStatus(customerId, status);
    }

    public WaitingCustomer getFirstInQueue() {
        return repository.getFirstInQueue();
    }

    public AppSettings getSettingsSync() {
        return repository.getSettingsSync();
    }

    public void updateSettings(AppSettings settings) {
        repository.updateSettings(settings);
    }

    public void regenerateTables(int indoor, int outdoor) {
        repository.regenerateTables(indoor, outdoor);
    }
}
