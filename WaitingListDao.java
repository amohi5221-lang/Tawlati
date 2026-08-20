package com.tawlati.app.database;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import com.tawlati.app.models.WaitingCustomer;
import java.util.List;

@Dao
public interface WaitingListDao {

    @Query("SELECT * FROM waiting_list WHERE status = 'waiting' ORDER BY arrivalTime ASC")
    LiveData<List<WaitingCustomer>> getWaitingCustomers();

    @Query("SELECT * FROM waiting_list WHERE status = 'waiting' ORDER BY arrivalTime ASC")
    List<WaitingCustomer> getWaitingCustomersSync();

    // ✅ يجب استدعاؤها من background thread دائماً
    @Query("SELECT * FROM waiting_list WHERE status = 'waiting' ORDER BY arrivalTime ASC LIMIT 1")
    WaitingCustomer getFirstInQueue();

    @Query("SELECT COUNT(*) FROM waiting_list WHERE status = 'waiting'")
    LiveData<Integer> getWaitingCount();

    @Insert
    long insert(WaitingCustomer customer);

    @Update
    void update(WaitingCustomer customer);

    @Query("UPDATE waiting_list SET status = :status WHERE id = :customerId")
    void updateStatus(int customerId, String status);

    @Delete
    void delete(WaitingCustomer customer);

    // ✅ إضافة: حذف جميع المنتهية
    @Query("DELETE FROM waiting_list WHERE status != 'waiting'")
    void clearCompleted();
}
