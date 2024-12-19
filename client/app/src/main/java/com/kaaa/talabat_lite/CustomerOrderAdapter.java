package com.kaaa.talabat_lite;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;


public class CustomerOrderAdapter extends RecyclerView.Adapter<CustomerOrderAdapter.ViewHolder> {
    public static class orderData {
        public String orderId;
        public String merchantName;
        public String totalAmount;
        public orderData (int orderId, String merchantName , float totalAmount)
        {
            this.orderId = String.valueOf(orderId);
            this.merchantName = merchantName;
            this.totalAmount = String.format("%.1f",totalAmount);
        }
    }

    private List<orderData> orderList;
    private Context context;

    public CustomerOrderAdapter(Context context, List<orderData> orderList) {
        this.context = context;
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.customer_order_row, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        orderData order = orderList.get(position);
        holder.orderId.setText( "#" + order.orderId);
        holder.merchantName.setText(order.merchantName);
        holder.totalAmount.setText(order.totalAmount + " EGP");

        holder.itemView.setOnClickListener(v -> {
            Intent orderDetailsActivity = new Intent(context,OrderDetailsActivity.class);
            orderDetailsActivity.putExtra("orderId",order.orderId);
            context.startActivity(orderDetailsActivity);
        });
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView orderId, merchantName, totalAmount;
        public ViewHolder(View itemView) {
            super(itemView);
            orderId = itemView.findViewById(R.id.orderId);
            merchantName = itemView.findViewById(R.id.merchantName);
            totalAmount = itemView.findViewById(R.id.totalAmount);
        }
    }
}
