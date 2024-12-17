package com.kaaa.talabat_lite;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class CartItemAdapter extends RecyclerView.Adapter<CartItemAdapter.ViewHolder> {

    // Inner class for cart item data
    public static class cartItemData {
        private final int id;
        private final int itemCount;
        private final String itemName;
        private final String merchName;
        private final float itemPrice;
        private final float itemTotalPrice;
        private final Bitmap itemImage;

        public cartItemData(int id, int itemCount, String itemName, String merchName, float itemPrice, float itemTotalPrice, Bitmap itemImage) {
            this.id = id;
            this.itemCount = itemCount;
            this.itemName = itemName;
            this.merchName = merchName;
            this.itemPrice = itemPrice;
            this.itemTotalPrice = itemTotalPrice;
            this.itemImage = itemImage;
        }
    }

    private final List<cartItemData> cartItemList;
    private final Context context;

    public CartItemAdapter(Context context, List<cartItemData> cartItemList) {
        // Ensure the list is not null to avoid crashes
        this.cartItemList = cartItemList != null ? cartItemList : new ArrayList<>();
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cart_item_row, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (position >= cartItemList.size()) {
            Log.e("CartItemAdapter", "Invalid position or null cart item list");
            return;
        }

        cartItemData cartItem = cartItemList.get(position);

        // Ensure all holder views are initialized
        if (holder.itemName == null || holder.itemMerch == null || holder.itemPrice == null ||
                holder.itemCount == null || holder.totalPrice == null || holder.itemImage == null ||
                holder.btnRemoveItem == null) {
            Log.e("CartItemAdapter", "ViewHolder contains null views.");
            return;
        }

        // Set item details
        holder.itemName.setText("Item Name : "+cartItem.itemName);
        holder.itemMerch.setText("Merchant Name : " +cartItem.merchName);
        holder.itemCount.setText(String.valueOf("Item Count : "+cartItem.itemCount));
        holder.totalPrice.setText("Total Price : "+String.format("%.1f", cartItem.itemTotalPrice));
        holder.itemPrice.setText("One Item Price : "+String.format("%.1f", cartItem.itemPrice));

        // Handle item image
        if (cartItem.itemImage != null) {
            holder.itemImage.setImageBitmap(cartItem.itemImage);
        } else {
            Log.w("CartItemAdapter", "Null image for item: " + cartItem.itemName);
            holder.itemImage.setImageResource(R.drawable.profile_icon); // Default image
        }

        // Item image click listener
        holder.itemImage.setOnClickListener(v -> {
            Context context = this.context;
            if (context != null) {
                Intent viewItem = new Intent(context, ItemActivity.class);
                viewItem.putExtra("id", cartItem.id);
                context.startActivity(viewItem);
            }
        });

        // Remove item button listener
        holder.btnRemoveItem.setOnClickListener(v -> {
            Log.d("CartItemAdapter", "Remove button clicked for item: " + cartItem.itemName);

            // Start a background thread to handle the network operation
            new Thread(() -> {
                boolean success = removeItemFromCart(cartItem.id);

                // Use Handler to update the UI on the main thread after network operation
                Handler mainHandler = new Handler(Looper.getMainLooper());
                mainHandler.post(() -> {
                    if (success) {
                        cartItemList.remove(position);
                        notifyItemRemoved(position);
                        Log.i("CartItemAdapter", "Item removed successfully");
                    } else {
                        Log.e("CartItemAdapter", "Error removing item from cart");
                    }
                });
            }).start();
        });
    }

    @Override
    public int getItemCount() {
        return cartItemList.size();
    }

    // Method to handle the network operation
    private boolean removeItemFromCart(int itemId) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(globals.serverURL + "/cart/removeItem/" + globals.userId + "/" + itemId);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // Check response code
            return connection.getResponseCode() == HttpURLConnection.HTTP_OK;
        } catch (IOException e) {
            Log.e("CartItemAdapter", "Error removing item from cart", e);
            return false;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView itemMerch, itemName, itemPrice, itemCount, totalPrice;
        ImageView itemImage;
        Button btnRemoveItem;

        public ViewHolder(View cartItemView) {
            super(cartItemView);

            try {
                itemName = cartItemView.findViewById(R.id.cartItemName);
                itemMerch = cartItemView.findViewById(R.id.cartItemMerch);
                itemPrice = cartItemView.findViewById(R.id.cartItemPrice);
                itemCount = cartItemView.findViewById(R.id.cartItemCount);
                totalPrice = cartItemView.findViewById(R.id.cartItemTotalPrice);
                itemImage = cartItemView.findViewById(R.id.cartItemImage);
                btnRemoveItem = cartItemView.findViewById(R.id.btnRemoveCartItem);
            } catch (Exception e) {
                Log.e("ViewHolder", "Error initializing views: " + e.getMessage());
            }
        }
    }
}
