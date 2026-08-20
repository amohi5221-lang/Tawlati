package com.tawlati.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.tawlati.app.R;
import com.tawlati.app.adapters.WaitingListAdapter;
import com.tawlati.app.models.WaitingCustomer;
import com.tawlati.app.viewmodels.MainViewModel;

public class WaitingListActivity extends AppCompatActivity
    implements WaitingListAdapter.WaitingClickListener {

    private MainViewModel viewModel;
    private WaitingListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting_list);

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        RecyclerView rv = findViewById(R.id.rvFullWaitingList);
        adapter = new WaitingListAdapter(this, this);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        // ✅ LiveData تتحدث تلقائياً
        viewModel.waitingCustomers.observe(this, customers ->
            adapter.setCustomers(customers));

        Button btnAddWaiting = findViewById(R.id.btnAddToWaiting);
        if (btnAddWaiting != null) {
            btnAddWaiting.setOnClickListener(v ->
                startActivity(new Intent(this, BookingActivity.class)));
        }

        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
    }

    @Override
    public void onSeatCustomer(WaitingCustomer customer) {
        Intent intent = new Intent(this, BookingActivity.class);
        intent.putExtra("prefill_name",   customer.customerName);
        intent.putExtra("prefill_phone",  customer.customerPhone);
        intent.putExtra("prefill_guests", customer.guestsCount);
        intent.putExtra("waiting_id",     customer.id);
        startActivity(intent);
    }

    @Override
    public void onCancelCustomer(WaitingCustomer customer) {
        new AlertDialog.Builder(this)
            .setTitle("إلغاء الانتظار")
            .setMessage("هل تريد إلغاء انتظار " + customer.customerName + "؟")
            .setPositiveButton("إلغاء الانتظار", (d, w) ->
                viewModel.updateWaitingStatus(customer.id, "cancelled"))
            .setNegativeButton("تراجع", null)
            .show();
    }
}
