package com.tawlati.app.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tables")
public class TableModel {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String tableNumber;      // "T01", "T02"...
    public String area;             // "indoor" | "outdoor"
    public int capacity;            // number of seats
    public String status;           // "available" | "occupied" | "expired"

    public String customerName;
    public String customerPhone;
    public int guestsCount;
    public long bookingStartTime;
    public int bookingDurationMin;
    public long bookingEndTime;

    public TableModel() {}

    public boolean isAvailable() { return "available".equals(status); }
    public boolean isOccupied()  { return "occupied".equals(status);  }
    public boolean isExpired()   { return "expired".equals(status);   }

    // ✅ يعمل بشكل صحيح لجميع الحالات
    public long getRemainingMillis() {
        if (bookingEndTime <= 0) return 0;
        long remaining = bookingEndTime - System.currentTimeMillis();
        return Math.max(0, remaining);
    }

    public String getRemainingFormatted() {
        long remaining = getRemainingMillis();
        if (remaining <= 0) return "00:00:00";
        long hours   = remaining / 3600000;
        long minutes = (remaining % 3600000) / 60000;
        long seconds = (remaining % 60000) / 1000;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}
