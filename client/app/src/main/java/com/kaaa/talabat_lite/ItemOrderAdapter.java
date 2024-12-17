package com.kaaa.talabat_lite;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;


public class ItemOrderAdapter extends RecyclerView.Adapter<ItemOrderAdapter.ViewHolder> {
    public static class itemData {
        public String itemCount;
        public String itemName;
        public String itemPrice;
        public itemData(int itemCount, String itemName , float itemPrice)
        {
            this.itemCount = String.valueOf(itemCount);
            this.itemName = itemName;
            this.itemPrice = String.format("%.1f",itemPrice);
        }
    }

    private List<itemData> orderItemList;
    private Context context;

    public ItemOrderAdapter(Context context, List<itemData> orderitemList) {
        this.context = context;
        this.orderItemList = orderitemList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order_row, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        itemData item = orderItemList.get(position);
        holder.itemCount.setText(item.itemCount);
        holder.itemName.setText(item.itemName);
        holder.itemPrice.setText(item.itemPrice + " EGP");
        /*
        holder.itemView.setOnClickListener(v -> {
            Intent orderDetailsActivity = new Intent(context,OrderDetailsActivity.class);
            orderDetailsActivity.putExtra("orderId",order.orderId);
            context.startActivity(orderDetailsActivity);
        });
         */
    }

    @Override
    public int getItemCount() {
        return orderItemList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView itemCount, itemName, itemPrice;
        public ViewHolder(View itemView) {
            super(itemView);
            itemCount = itemView.findViewById(R.id.itemCount);
            itemName = itemView.findViewById(R.id.itemName);
            itemPrice = itemView.findViewById(R.id.itemPrice);
        }
    }
}
