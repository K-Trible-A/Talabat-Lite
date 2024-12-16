package com.kaaa.talabat_lite;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;


public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> {
    public static class itemData {
        int id;
        public String name, description;
        public float price;
        Bitmap img;
        public itemData (int id, String name , String description , float price , Bitmap img)
        {
            this.name = name;
            this.description = description;
            this.price = price;
            this.img = img;
            this.id = id;
        }
    }

    private List<itemData> itemList;
    private Context context;

    public ItemAdapter(Context context, List<itemData> itemList) {
        this.context = context;
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_row, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        itemData item = itemList.get(position);
        holder.itemName.setText(item.name);
        holder.itemPrice.setText(String.format("%.1f",item.price) + "$");
        holder.itemDescription.setText(item.description);
        holder.itemImage.setImageBitmap(item.img);

        holder.itemView.setOnClickListener(v -> {
            Intent itemManagementActivity = new Intent(context,ItemManagementActivity.class);
            itemManagementActivity.putExtra("id",item.id);
            itemManagementActivity.putExtra("name",item.name);
            itemManagementActivity.putExtra("price",item.price);
            itemManagementActivity.putExtra("description",item.description);
            context.startActivity(itemManagementActivity);
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView itemName, itemPrice, itemDescription;
        ImageView itemImage;
        public ViewHolder(View itemView) {
            super(itemView);
            itemName = itemView.findViewById(R.id.itemName);
            itemImage = itemView.findViewById(R.id.itemImage);
            itemPrice = itemView.findViewById(R.id.itemPrice);
            itemDescription = itemView.findViewById(R.id.itemDescription);
        }
    }
}
