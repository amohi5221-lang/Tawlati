package com.tawlati.app.adapters;

import android.content.Context;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.tawlati.app.R;
import com.tawlati.app.models.WaitingCustomer;
import java.util.*;

public class WaitingListAdapter extends RecyclerView.Adapter<WaitingListAdapter.WaitingViewHolder> {

    private List<WaitingCustomer> customers = new ArrayList<>();
    private final Context context;
    private WaitingClickListener listener;

    public interface WaitingClickListener {
        void onSeatCustomer(WaitingCustomer customer);
        void onCancelCustomer(WaitingCustomer customer);
    }

    public WaitingListAdapter(Context context, WaitingClickListener listener) {
        this.context  = context;
        this.listener = listener;
    }

    public void setCustomers(List<WaitingCustomer> customers) {
        this.customers = (customers != null) ? customers : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public WaitingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_waiting, parent, false);
        return new WaitingViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull WaitingViewHolder holder, int position) {
        WaitingCustomer customer = customers.get(position);

        holder.tvPosition.setText(String.valueOf(position + 1));
        holder.tvName.setText(
            customer.customerName != null ? customer.customerName : "");
        holder.tvPhone.setText(
            customer.customerPhone != null ? customer.customerPhone : "");
        holder.tvGuests.setText(customer.guestsCount + " أشخاص");
        holder.tvArea.setText(
            "outdoor".equals(customer.preferredArea) ? "🌿 خارجي" : "🏠 داخلي");

        // ✅ عرض كم دقيقة ينتظر
        long waitMins = customer.getWaitingMinutes();
        if (waitMins < 60) {
            holder.tvTime.setText("انتظر: " + waitMins + " دقيقة");
        } else {
            holder.tvTime.setText("وصل: " + customer.getArrivalTimeFormatted());
        }

        // لون مختلف للأول
        holder.tvPosition.setBackgroundResource(
            position == 0 ? R.drawable.circle_primary : R.drawable.circle_secondary);

        holder.btnSeat.setOnClickListener(v -> {
            if (listener != null) listener.onSeatCustomer(customer);
        });
        holder.btnCancel.setOnClickListener(v -> {
            if (listener != null) listener.onCancelCustomer(customer);
        });
    }

    @Override
    public int getItemCount() { return customers.size(); }

    static class WaitingViewHolder extends RecyclerView.ViewHolder {
        TextView tvPosition, tvName, tvPhone, tvGuests, tvArea, tvTime;
        Button btnSeat, btnCancel;

        WaitingViewHolder(View itemView) {
            super(itemView);
            tvPosition = itemView.findViewById(R.id.tvWaitPosition);
            tvName     = itemView.findViewById(R.id.tvWaitName);
            tvPhone    = itemView.findViewById(R.id.tvWaitPhone);
            tvGuests   = itemView.findViewById(R.id.tvWaitGuests);
            tvArea     = itemView.findViewById(R.id.tvWaitArea);
            tvTime     = itemView.findViewById(R.id.tvWaitTime);
            btnSeat    = itemView.findViewById(R.id.btnSeatCustomer);
            btnCancel  = itemView.findViewById(R.id.btnCancelWait);
        }
    }
}
