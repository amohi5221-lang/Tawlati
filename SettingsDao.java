package com.tawlati.app.database;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import com.tawlati.app.models.AppSettings;

@Dao
public interface SettingsDao {

    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    LiveData<AppSettings> getSettings();

    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    AppSettings getSettingsSync();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(AppSettings settings);

    @Update
    void update(AppSettings settings);
}
