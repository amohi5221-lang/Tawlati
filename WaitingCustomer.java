package com.tawlati.app.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.util.Calendar;

@Entity(tableName = "waiting_list")
public class WaitingCustomer {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String customerName;
    public String customerPhone;
    public int guestsCount;
    public String preferredArea;   // "indoor" | "outdoor"
    public long arrivalTime;
    public String status;          // "waiting" | "seated" | "cancelled"
    public String notes;

    public WaitingCustomer() {
        // ✅ قيم افتراضية آمنة
        this.status        = "waiting";
        this.preferredArea = "indoor";
        this.guestsCount   = 1;
        this.arrivalTime   = System.currentTimeMillis();
    }

    public String getArrivalTimeFormatted() {
        if (arrivalTime <= 0) return "00:00";
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(arrivalTime);
        return String.format("%02d:%02d",
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE));
    }

    // ✅ احسب كم دقيقة مرت منذ الوصول
    public long getWaitingMinutes() {
        if (arrivalTime <= 0) return 0;
        return (System.currentTimeMillis() - arrivalTime) / 60000;
    }
}
