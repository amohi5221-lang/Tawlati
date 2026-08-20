package com.tawlati.app.database;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import com.tawlati.app.models.TableModel;
import java.util.List;

@Dao
public interface TableDao {

    @Query("SELECT * FROM tables ORDER BY tableNumber ASC")
    LiveData<List<TableModel>> getAllTables();

    @Query("SELECT * FROM tables WHERE status = 'available' ORDER BY tableNumber ASC")
    LiveData<List<TableModel>> getAvailableTables();

    @Query("SELECT * FROM tables WHERE status = 'available' AND area = :area ORDER BY tableNumber ASC")
    List<TableModel> getAvailableTablesByArea(String area);

    @Query("SELECT * FROM tables WHERE status = 'occupied' ORDER BY tableNumber ASC")
    LiveData<List<TableModel>> getOccupiedTables();

    // ✅ إضافة: نسخة sync لـ AlarmReceiver
    @Query("SELECT * FROM tables WHERE status = 'occupied' ORDER BY tableNumber ASC")
    List<TableModel> getOccupiedTablesSync();

    @Query("SELECT COUNT(*) FROM tables WHERE status = 'available'")
    LiveData<Integer> getAvailableCount();

    @Query("SELECT COUNT(*) FROM tables WHERE status = 'available' AND area = 'indoor'")
    LiveData<Integer> getAvailableIndoorCount();

    @Query("SELECT COUNT(*) FROM tables WHERE status = 'available' AND area = 'outdoor'")
    LiveData<Integer> getAvailableOutdoorCount();

    @Query("SELECT COUNT(*) FROM tables WHERE status = 'occupied'")
    LiveData<Integer> getOccupiedCount();

    @Query("SELECT * FROM tables WHERE id = :tableId")
    TableModel getTableById(int tableId);

    @Query("SELECT * FROM tables WHERE tableNumber = :tableNumber LIMIT 1")
    TableModel getTableByNumber(String tableNumber);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(TableModel table);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<TableModel> tables);

    @Update
    void update(TableModel table);

    @Delete
    void delete(TableModel table);

    @Query("DELETE FROM tables")
    void deleteAll();

    @Query("UPDATE tables SET status='available', customerName=NULL, customerPhone=NULL, " +
           "guestsCount=0, bookingStartTime=0, bookingDurationMin=0, bookingEndTime=0 " +
           "WHERE id = :tableId")
    void freeTable(int tableId);
}
