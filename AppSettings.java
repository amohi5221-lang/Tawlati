package com.tawlati.app.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "app_settings")
public class AppSettings {

    @PrimaryKey
    public int id = 1;  // Single row settings

    public String cafeName;
    public int indoorTablesCount;
    public int outdoorTablesCount;
    public int defaultBookingDurationMin;
    public int alertBeforeMinutes;
    public boolean darkMode;
    public String adminPassword;

    // Default constructor with sensible defaults
    public AppSettings() {
        this.cafeName = "طاولتي";
        this.indoorTablesCount = 20;
        this.outdoorTablesCount = 15;
        this.defaultBookingDurationMin = 60;
        this.alertBeforeMinutes = 10;
        this.darkMode = false;
        this.adminPassword = "1234";
    }
}
