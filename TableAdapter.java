package com.tawlati.app.adapters;

import android.content.Context;
import androidx.core.content.ContextCompat;
import android.os.CountDownTimer;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.tawlati.app.R;
import com.tawlati.app.models.TableModel;
import java.util.*;

public class TableAdapter extends RecyclerView.Adapter<TableAdapter.TableViewHolder> {

    private List<TableModel> tables = new ArrayList<>();
    private final Context context;
    private TableClickListener listener;
    private final Map<Integer, CountDownTimer> timers = new HashMap<>();

    public interface TableClickListener {
        void onTableClick(TableModel table);
        void onFreeTable(TableModel table);
        void onConfirmService(TableModel table);
    }

    public TableAdapter(Context context, TableClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setTables(List<TableModel> tables) {
        for (CountDownTimer timer : timers.values()) timer.cancel();
        timers.clear();
        this.tables = tables != null ? tables : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TableViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_table, parent, false);
        return new TableViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull TableViewHolder holder, int position) {
        TableModel table = tables.get(position);
        holder.bind(table);
    }

    @Override
    public int getItemCount() { return tables.size(); }

    @Override
    public void onViewRecycled(@NonNull TableViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder.currentTableId != -1 && timers.containsKey(holder.currentTableId)) {
            timers.get(holder.currentTableId).cancel();
            timers.remove(holder.currentTableId);
        }
    }

    class TableViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvTableNumber, tvStatus, tvCustomerName, tvPhone,
                 tvGuests, tvArea, tvTimer, tvCapacity;
        Button btnFree, btnService;
        LinearLayout layoutBookingInfo;
        int currentTableId = -1;

        TableViewHolder(View itemView) {
            super(itemView);
            cardView        = itemView.findViewById(R.id.cardTable);
            tvTableNumber   = itemView.findViewById(R.id.tvTableNumber);
            tvStatus        = itemView.findViewById(R.id.tvStatus);
            tvCustomerName  = itemView.findViewById(R.id.tvCustomerName);
            tvPhone         = itemView.findViewById(R.id.tvPhone);
            tvGuests        = itemView.findViewById(R.id.tvGuests);
            tvArea          = itemView.findViewById(R.id.tvArea);
            tvTimer         = itemView.findViewById(R.id.tvTimer);
            tvCapacity      = itemView.findViewById(R.id.tvCapacity);
            btnFree         = itemView.findViewById(R.id.btnFreeTable);
            btnService      = itemView.findViewById(R.id.btnConfirmService);
            layoutBookingInfo = itemView.findViewById(R.id.layoutBookingInfo);
        }

        void bind(TableModel table) {
            // إلغاء المؤقت القديم قبل ربط بيانات جديدة
            if (currentTableId != -1 && timers.containsKey(currentTableId)) {
                timers.get(currentTableId).cancel();
                timers.remove(currentTableId);
            }
            currentTableId = table.id;

            tvTableNumber.setText(table.tableNumber);
            tvArea.setText("outdoor".equals(table.area) ? "🌿 خارجي" : "🏠 داخلي");

            if (table.isAvailable()) {
                bindAvailableTable(table);
            } else if (table.isOccupied()) {
                bindOccupiedTable(table);
            } else {
                bindExpiredTable(table);
            }
        }

        void bindAvailableTable(TableModel table) {
            cardView.setCardBackgroundColor(
                ContextCompat.getColor(context, R.color.table_available));
            tvStatus.setText("✅ متاحة");
            tvStatus.setTextColor(
                ContextCompat.getColor(context, android.R.color.white));
            layoutBookingInfo.setVisibility(View.GONE);
            btnFree.setVisibility(View.GONE);
            btnService.setVisibility(View.GONE);
            tvTimer.setVisibility(View.GONE);
            cardView.setOnClickListener(v -> {
                if (listener != null) listener.onTableClick(table);
            });
        }

        void bindOccupiedTable(TableModel table) {
            cardView.setCardBackgroundColor(
                ContextCompat.getColor(context, R.color.table_occupied));
            tvStatus.setText("🔴 مشغولة");
            tvStatus.setTextColor(
                ContextCompat.getColor(context, android.R.color.white));
            layoutBookingInfo.setVisibility(View.VISIBLE);
            btnFree.setVisibility(View.VISIBLE);
            btnService.setVisibility(View.GONE);
            tvTimer.setVisibility(View.VISIBLE);

            tvCustomerName.setText("العميل: " + table.customerName);
            tvPhone.setText("📞 " + table.customerPhone);
            tvGuests.setText("👥 " + table.guestsCount + " أشخاص");

            // ✅ الإصلاح: حساب الوقت مرة واحدة وتخزينه في متغير final
            final long remainingMillis = table.getRemainingMillis();

            if (remainingMillis > 0) {
                // عرض الوقت الأولي فوراً قبل بدء المؤقت
                updateTimerText(tvTimer, remainingMillis);

                CountDownTimer timer = new CountDownTimer(remainingMillis, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        // ✅ نستخدم millisUntilFinished من onTick مباشرة (وليس remaining)
                        updateTimerText(tvTimer, millisUntilFinished);
                        if (millisUntilFinished < 600000) {
                            tvTimer.setTextColor(
                                ContextCompat.getColor(context, R.color.timer_warning));
                        } else {
                            tvTimer.setTextColor(
                                ContextCompat.getColor(context, android.R.color.white));
                        }
                    }

                    @Override
                    public void onFinish() {
                        tvTimer.setText("⏱ 00:00:00");
                        tvTimer.setTextColor(
                            ContextCompat.getColor(context, R.color.timer_warning));
                        cardView.setCardBackgroundColor(
                            ContextCompat.getColor(context, R.color.table_expired));
                        tvStatus.setText("⚠️ انتهى الوقت");
                    }
                }.start();

                timers.put(table.id, timer);
            } else {
                tvTimer.setText("⏱ 00:00:00");
            }

            btnFree.setOnClickListener(v -> {
                if (listener != null) listener.onFreeTable(table);
            });
            cardView.setOnClickListener(null);
        }

        void bindExpiredTable(TableModel table) {
            cardView.setCardBackgroundColor(
                ContextCompat.getColor(context, R.color.table_expired));
            tvStatus.setText("⚠️ انتهى الوقت");
            tvStatus.setTextColor(
                ContextCompat.getColor(context, android.R.color.white));
            layoutBookingInfo.setVisibility(View.VISIBLE);
            btnFree.setVisibility(View.VISIBLE);
            btnService.setVisibility(View.GONE);
            tvTimer.setVisibility(View.VISIBLE);
            tvTimer.setText("⏱ 00:00:00");
            tvTimer.setTextColor(
                ContextCompat.getColor(context, R.color.timer_warning));

            if (table.customerName != null) {
                tvCustomerName.setText("العميل: " + table.customerName);
                tvPhone.setText("📞 " + table.customerPhone);
                tvGuests.setText("👥 " + table.guestsCount + " أشخاص");
            }
            btnFree.setOnClickListener(v -> {
                if (listener != null) listener.onFreeTable(table);
            });
            cardView.setOnClickListener(null);
        }

        // ✅ دالة مساعدة لتحويل الوقت وعرضه
        private void updateTimerText(TextView tv, long millis) {
            long h = millis / 3600000;
            long m = (millis % 3600000) / 60000;
            long s = (millis % 60000) / 1000;
            tv.setText(String.format("⏱ %02d:%02d:%02d", h, m, s));
        }
    }
}
