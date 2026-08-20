package com.tawlati.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.tawlati.app.R;
import com.tawlati.app.adapters.TableAdapter;
import com.tawlati.app.adapters.WaitingListAdapter;
import com.tawlati.app.models.TableModel;
import com.tawlati.app.models.WaitingCustomer;
import com.tawlati.app.utils.AlarmHelper;
import com.tawlati.app.utils.NotificationHelper;
import com.tawlati.app.viewmodels.MainViewModel;

public class MainActivity extends AppCompatActivity
    implements TableAdapter.TableClickListener, WaitingListAdapter.WaitingClickListener {

    private MainViewModel viewModel;
    private TableAdapter tableAdapter;
    private WaitingListAdapter waitingAdapter;

    private TextView tvAvailableCount, tvOccupiedCount, tvWaitingCount;
    private TextView tvIndoorOutdoorDetail;
    private LinearLayout btnHome, btnCustomers, btnQuickBook, btnSettings;

    // ✅ متغيرات لتخزين القيم لتجنب nested observers
    private int lastIndoorCount = 0;
    private int lastOutdoorCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NotificationHelper.createNotificationChannels(this);
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        initViews();
        setupRecyclerViews();
        observeData();
    }

    private void initViews() {
        tvAvailableCount    = findViewById(R.id.tvAvailableCount);
        tvOccupiedCount     = findViewById(R.id.tvOccupiedCount);
        tvWaitingCount      = findViewById(R.id.tvWaitingCount);
        tvIndoorOutdoorDetail = findViewById(R.id.tvIndoorOutdoorDetail);

        btnHome       = findViewById(R.id.btnNavHome);
        btnCustomers  = findViewById(R.id.btnNavCustomers);
        btnQuickBook  = findViewById(R.id.btnNavQuickBook);
        btnSettings   = findViewById(R.id.btnNavSettings);

        btnSettings.setOnClickListener(v ->
            startActivity(new Intent(this, SettingsActivity.class)));
        btnCustomers.setOnClickListener(v ->
            startActivity(new Intent(this, WaitingListActivity.class)));
        btnQuickBook.setOnClickListener(v ->
            startActivity(new Intent(this, BookingActivity.class)));
        btnHome.setOnClickListener(v -> { /* الشاشة الحالية */ });
    }

    private void setupRecyclerViews() {
        RecyclerView rvTables = findViewById(R.id.rvTables);
        tableAdapter = new TableAdapter(this, this);
        rvTables.setLayoutManager(new GridLayoutManager(this, 3));
        rvTables.setAdapter(tableAdapter);

        RecyclerView rvWaiting = findViewById(R.id.rvWaitingList);
        waitingAdapter = new WaitingListAdapter(this, this);
        rvWaiting.setLayoutManager(
            new androidx.recyclerview.widget.LinearLayoutManager(this));
        rvWaiting.setAdapter(waitingAdapter);
    }

    private void observeData() {
        viewModel.allTables.observe(this, tables ->
            tableAdapter.setTables(tables));

        viewModel.availableCount.observe(this, count ->
            tvAvailableCount.setText(count != null ? String.valueOf(count) : "0"));

        viewModel.occupiedCount.observe(this, count ->
            tvOccupiedCount.setText(count != null ? String.valueOf(count) : "0"));

        viewModel.waitingCount.observe(this, count ->
            tvWaitingCount.setText(count != null ? String.valueOf(count) : "0"));

        viewModel.waitingCustomers.observe(this, customers ->
            waitingAdapter.setCustomers(customers));

        // ✅ الإصلاح: observer منفصل لكل قيمة بدون تداخل
        viewModel.availableIndoorCount.observe(this, indoor -> {
            lastIndoorCount = (indoor != null) ? indoor : 0;
            updateIndoorOutdoorText();
        });

        viewModel.availableOutdoorCount.observe(this, outdoor -> {
            lastOutdoorCount = (outdoor != null) ? outdoor : 0;
            updateIndoorOutdoorText();
        });
    }

    // ✅ دالة مشتركة لتحديث النص
    private void updateIndoorOutdoorText() {
        tvIndoorOutdoorDetail.setText(
            String.format("(%d داخلية، %d خارجية)", lastIndoorCount, lastOutdoorCount));
    }

    @Override
    public void onTableClick(TableModel table) {
        Intent intent = new Intent(this, BookingActivity.class);
        intent.putExtra("table_id", table.id);
        intent.putExtra("table_number", table.tableNumber);
        intent.putExtra("table_area", table.area);
        startActivity(intent);
    }

    @Override
    public void onFreeTable(TableModel table) {
        new AlertDialog.Builder(this)
            .setTitle("تفريغ الطاولة")
            .setMessage(String.format("هل تريد تفريغ طاولة %s؟\nسيتم إنهاء الحجز الحالي.",
                table.tableNumber))
            .setPositiveButton("تفريغ", (dialog, which) -> {
                viewModel.freeTable(table.id);
                AlarmHelper.cancelBookingAlarms(this, table.id);
                NotificationHelper.cancelNotification(this, table.id);
                checkWaitingList(table);
            })
            .setNegativeButton("إلغاء", null)
            .show();
    }

    @Override
    public void onConfirmService(TableModel table) { }

    private void checkWaitingList(TableModel freedTable) {
        new Thread(() -> {
            WaitingCustomer next = viewModel.getFirstInQueue();
            if (next != null) {
                runOnUiThread(() ->
                    new AlertDialog.Builder(this)
                        .setTitle("🔔 قائمة الانتظار")
                        .setMessage(String.format(
                            "يوجد عميل في قائمة الانتظار:\n👤 %s\n👥 %d أشخاص\n\nهل تريد نقله لطاولة %s؟",
                            next.customerName, next.guestsCount, freedTable.tableNumber))
                        .setPositiveButton("نعم، نقل العميل", (d, w) -> {
                            Intent intent = new Intent(this, BookingActivity.class);
                            intent.putExtra("table_id", freedTable.id);
                            intent.putExtra("table_number", freedTable.tableNumber);
                            intent.putExtra("table_area", freedTable.area);
                            intent.putExtra("prefill_name", next.customerName);
                            intent.putExtra("prefill_phone", next.customerPhone);
                            intent.putExtra("prefill_guests", next.guestsCount);
                            intent.putExtra("waiting_id", next.id);
                            startActivity(intent);
                        })
                        .setNegativeButton("لاحقاً", null)
                        .show()
                );
            }
        }).start();
    }

    @Override
    public void onSeatCustomer(WaitingCustomer customer) {
        Intent intent = new Intent(this, BookingActivity.class);
        intent.putExtra("prefill_name", customer.customerName);
        intent.putExtra("prefill_phone", customer.customerPhone);
        intent.putExtra("prefill_guests", customer.guestsCount);
        intent.putExtra("waiting_id", customer.id);
        startActivity(intent);
    }

    @Override
    public void onCancelCustomer(WaitingCustomer customer) {
        new AlertDialog.Builder(this)
            .setTitle("إلغاء الانتظار")
            .setMessage("هل تريد إلغاء انتظار العميل " + customer.customerName + "؟")
            .setPositiveButton("إلغاء الانتظار", (d, w) ->
                viewModel.updateWaitingStatus(customer.id, "cancelled"))
            .setNegativeButton("تراجع", null)
            .show();
    }
}
