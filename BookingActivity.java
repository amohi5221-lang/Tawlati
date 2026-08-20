package com.tawlati.app.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.text.TextUtils;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.tawlati.app.R;
import com.tawlati.app.models.AppSettings;
import com.tawlati.app.models.TableModel;
import com.tawlati.app.models.WaitingCustomer;
import com.tawlati.app.utils.AlarmHelper;
import com.tawlati.app.viewmodels.MainViewModel;
import java.util.ArrayList;
import java.util.List;

public class BookingActivity extends AppCompatActivity {

    private MainViewModel viewModel;
    private EditText etName, etPhone, etGuests, etDuration;
    private RadioGroup rgArea;
    private RadioButton rbIndoor, rbOutdoor;
    private Spinner spinnerTable;
    private Button btnBook, btnAddToWaiting, btnCallPhone;
    private TextView tvTitle;

    private int preselectedTableId = -1;
    private int waitingCustomerId  = -1;
    private List<TableModel> availableTables = new ArrayList<>();
    private AppSettings settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        initViews();
        loadSettings();
        handleIntentData();
        setupAreaListener();
    }

    private void initViews() {
        tvTitle         = findViewById(R.id.tvBookingTitle);
        etName          = findViewById(R.id.etCustomerName);
        etPhone         = findViewById(R.id.etCustomerPhone);
        etGuests        = findViewById(R.id.etGuestsCount);
        etDuration      = findViewById(R.id.etDuration);
        rgArea          = findViewById(R.id.rgArea);
        rbIndoor        = findViewById(R.id.rbIndoor);
        rbOutdoor       = findViewById(R.id.rbOutdoor);
        spinnerTable    = findViewById(R.id.spinnerTable);
        btnBook         = findViewById(R.id.btnConfirmBooking);
        btnAddToWaiting = findViewById(R.id.btnAddToWaiting);
        btnCallPhone    = findViewById(R.id.btnCallPhone);

        btnBook.setOnClickListener(v -> confirmBooking());
        btnAddToWaiting.setOnClickListener(v -> addToWaitingList());

        btnCallPhone.setOnClickListener(v -> {
            String phone = etPhone.getText().toString().trim();
            if (!TextUtils.isEmpty(phone)) {
                Intent callIntent = new Intent(Intent.ACTION_CALL,
                    Uri.parse("tel:" + phone));
                try {
                    startActivity(callIntent);
                } catch (SecurityException e) {
                    Toast.makeText(this, "يرجى منح إذن الاتصال",
                        Toast.LENGTH_SHORT).show();
                }
            }
        });

        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
    }

    private void loadSettings() {
        new Thread(() -> {
            settings = viewModel.getSettingsSync();
            runOnUiThread(() -> {
                int def = (settings != null) ? settings.defaultBookingDurationMin : 60;
                etDuration.setText(String.valueOf(def));
            });
        }).start();
    }

    private void handleIntentData() {
        Intent intent    = getIntent();
        preselectedTableId = intent.getIntExtra("table_id", -1);
        waitingCustomerId  = intent.getIntExtra("waiting_id", -1);

        String prefillName  = intent.getStringExtra("prefill_name");
        String prefillPhone = intent.getStringExtra("prefill_phone");
        int prefillGuests   = intent.getIntExtra("prefill_guests", 0);

        if (prefillName  != null) etName.setText(prefillName);
        if (prefillPhone != null) etPhone.setText(prefillPhone);
        if (prefillGuests > 0)    etGuests.setText(String.valueOf(prefillGuests));

        String tableArea = intent.getStringExtra("table_area");
        if ("outdoor".equals(tableArea)) rbOutdoor.setChecked(true);
        else                              rbIndoor.setChecked(true);

        if (preselectedTableId != -1) {
            String tableNumber = intent.getStringExtra("table_number");
            if (tableNumber != null) tvTitle.setText("حجز طاولة " + tableNumber);
        }

        loadAvailableTables();
    }

    private void setupAreaListener() {
        rgArea.setOnCheckedChangeListener((group, checkedId) -> loadAvailableTables());
    }

    private void loadAvailableTables() {
        String area = rbOutdoor.isChecked() ? "outdoor" : "indoor";
        new Thread(() -> {
            availableTables = viewModel.getAvailableTablesByArea(area);

            // ✅ إضافة الطاولة المختارة مسبقاً إذا لم تكن في القائمة
            if (preselectedTableId != -1) {
                boolean found = false;
                for (TableModel t : availableTables) {
                    if (t.id == preselectedTableId) { found = true; break; }
                }
                if (!found) {
                    TableModel selected = viewModel.getTableByIdSync(preselectedTableId);
                    if (selected != null) availableTables.add(0, selected);
                }
            }

            runOnUiThread(() -> {
                List<String> tableNames = new ArrayList<>();
                if (availableTables.isEmpty()) {
                    tableNames.add("لا توجد طاولات متاحة");
                } else {
                    for (TableModel t : availableTables)
                        tableNames.add(t.tableNumber + " (" +
                            ("indoor".equals(t.area) ? "داخلي" : "خارجي") + ")");
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    this, android.R.layout.simple_spinner_item, tableNames);
                adapter.setDropDownViewResource(
                    android.R.layout.simple_spinner_dropdown_item);
                spinnerTable.setAdapter(adapter);

                if (preselectedTableId != -1) {
                    for (int i = 0; i < availableTables.size(); i++) {
                        if (availableTables.get(i).id == preselectedTableId) {
                            spinnerTable.setSelection(i);
                            break;
                        }
                    }
                }
            });
        }).start();
    }

    private void confirmBooking() {
        String name       = etName.getText().toString().trim();
        String phone      = etPhone.getText().toString().trim();
        String guestsStr  = etGuests.getText().toString().trim();
        String durationStr= etDuration.getText().toString().trim();

        if (TextUtils.isEmpty(name))  { etName.setError("يرجى إدخال اسم العميل"); return; }
        if (TextUtils.isEmpty(phone)) { etPhone.setError("يرجى إدخال رقم الجوال"); return; }
        if (TextUtils.isEmpty(guestsStr)) { etGuests.setError("يرجى إدخال عدد الأشخاص"); return; }

        if (availableTables.isEmpty()) {
            Toast.makeText(this, "لا توجد طاولات متاحة", Toast.LENGTH_SHORT).show();
            return;
        }

        int guests = safeParseInt(guestsStr, 1);
        int duration = TextUtils.isEmpty(durationStr) ? 60 : safeParseInt(durationStr, 60);
        String area  = rbOutdoor.isChecked() ? "outdoor" : "indoor";

        int selectedIndex = spinnerTable.getSelectedItemPosition();
        if (selectedIndex < 0 || selectedIndex >= availableTables.size()) {
            Toast.makeText(this, "يرجى اختيار طاولة", Toast.LENGTH_SHORT).show();
            return;
        }

        TableModel table        = availableTables.get(selectedIndex);
        final int alertBefore   = (settings != null) ? settings.alertBeforeMinutes : 10;
        final int finalDuration = duration;

        // ✅ الإصلاح: جدولة الـ Alarm داخل نفس thread قاعدة البيانات
        new Thread(() -> {
            // 1. حفظ الحجز في قاعدة البيانات
            long startTime = System.currentTimeMillis();
            long endTime   = startTime + (finalDuration * 60000L);

            table.customerName     = name;
            table.customerPhone    = phone;
            table.guestsCount      = guests;
            table.area             = area;
            table.bookingStartTime = startTime;
            table.bookingDurationMin = finalDuration;
            table.bookingEndTime   = endTime;
            table.status           = "occupied";

            viewModel.updateTableSync(table);

            // 2. جدولة الـ Alarm مباشرة بعد الحفظ (نفس الـ thread)
            AlarmHelper.scheduleBookingAlarms(this, table, alertBefore);

            // 3. إزالة من قائمة الانتظار إن وجد
            if (waitingCustomerId != -1) {
                viewModel.updateWaitingStatus(waitingCustomerId, "seated");
            }

            runOnUiThread(() -> {
                Toast.makeText(this,
                    "✅ تم الحجز بنجاح لطاولة " + table.tableNumber,
                    Toast.LENGTH_SHORT).show();
                finish();
            });
        }).start();
    }

    private void addToWaitingList() {
        String name      = etName.getText().toString().trim();
        String phone     = etPhone.getText().toString().trim();
        String guestsStr = etGuests.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            etName.setError("يرجى إدخال اسم العميل");
            return;
        }

        WaitingCustomer customer = new WaitingCustomer();
        customer.customerName  = name;
        customer.customerPhone = phone;
        customer.guestsCount = TextUtils.isEmpty(guestsStr) ? 1 : safeParseInt(guestsStr, 1);
        customer.preferredArea = rbOutdoor.isChecked() ? "outdoor" : "indoor";
        customer.arrivalTime   = System.currentTimeMillis();
        customer.status        = "waiting";

        viewModel.addToWaitingList(customer);
        Toast.makeText(this, "✅ تم إضافة " + name + " لقائمة الانتظار",
            Toast.LENGTH_SHORT).show();
        finish();
    }

    // ✅ دالة آمنة لتحويل النص لرقم بدون crash
    private int safeParseInt(String value, int defaultVal) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            Log.w("BookingActivity", "safeParseInt: invalid value=" + value);
            return defaultVal;
        }
    }
}
