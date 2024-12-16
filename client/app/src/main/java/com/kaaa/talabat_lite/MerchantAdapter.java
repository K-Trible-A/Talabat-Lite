package com.kaaa.talabat_lite;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MerchantAdapter extends RecyclerView.Adapter<MerchantAdapter.ViewHolder> {

    protected static class MerchantData {
        int id;
        public String name;
        public float rating;

        public MerchantData(int id, String name, float rating) {
            this.name = name;
            this.rating = rating;
            this.id = id;
        }
    }

    private List<MerchantData> MerchantList;
    private Context context;

    public MerchantAdapter(Context context, List<MerchantData> MerchantList) {
        this.context = context;
        this.MerchantList = MerchantList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.merchant_row, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        MerchantData merchant = MerchantList.get(position);
        holder.merchName.setText(merchant.name);
        holder.merchRate.setText(String.format("%.1f", merchant.rating));

        holder.merchImage.setOnClickListener(v -> {
            Intent viewMerch = new Intent(context, CustomerViewofMerchant.class);
            viewMerch.putExtra("merch_id", merchant.id);

            // Check if context is an instance of Activity
            if (context instanceof Activity) {
                context.startActivity(viewMerch);
            } else {
                // Add the FLAG_ACTIVITY_NEW_TASK flag if context is not an Activity
                viewMerch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(viewMerch);
            }
        });
    }

    @Override
    public int getItemCount() {
        return MerchantList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView merchName, merchRate;
        ImageView merchImage;

        public ViewHolder(View merchView) {
            super(merchView);
            merchName = merchView.findViewById(R.id.merchName);
            merchImage = merchView.findViewById(R.id.merchIcon);
            merchRate = merchView.findViewById(R.id.merchRate);
        }
    }
}
