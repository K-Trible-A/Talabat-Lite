package com.kaaa.talabat_lite;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CartActivity extends AppCompatActivity {

    private CartItemAdapter cartAdapter;
    private final List<CartItemAdapter.cartItemData> cartItemDataList = new ArrayList<>();
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private RecyclerView recyclerView;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @SuppressLint("ObsoleteSdkInt")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.cartRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        cartAdapter = new CartItemAdapter(this, cartItemDataList);
        recyclerView.setAdapter(cartAdapter);

        // Initialize "Order Now" button
        Button btnOrderNow = findViewById(R.id.btnOrderNow);
        btnOrderNow.setOnClickListener(view ->
                Toast.makeText(CartActivity.this, "Order placed successfully!", Toast.LENGTH_SHORT).show()
        );

        // Fetch cart info
        fetchCartInfo();
    }

    private void fetchCartInfo() {
        if (executor.isShutdown()) {
            executor = Executors.newSingleThreadExecutor();
        }

        executor.execute(() -> {
            try {
                loadCartItemsFromServer();
            } catch (Exception e) {
                Log.e("CartActivityTag", "Error fetching cart items", e);
                mainHandler.post(() ->
                        Toast.makeText(CartActivity.this, "Failed to fetch cart items", Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadCartItemsFromServer() {
        List<CartItemAdapter.cartItemData> tempCartItemList = new ArrayList<>();

        try {
            // Connect to server
            socketHelper.getInstance().connect();

            // Request cart items
            socketHelper.getInstance().sendInt(globals.GET_CARTITEMS);
            socketHelper.getInstance().sendInt(globals.userId);

            int cartItemsCount = socketHelper.getInstance().recvInt();

            for (int i = 0; i < cartItemsCount; i++) {
                try {
                    // Receive item details from the server
                    String itemName = socketHelper.getInstance().recvString();
                    int itemCount = socketHelper.getInstance().recvInt();
                    float itemPrice = socketHelper.getInstance().recvFloat();
                    String merchName = socketHelper.getInstance().recvString();
                    float totalPrice = socketHelper.getInstance().recvFloat();
                    Bitmap itemImage = socketHelper.getInstance().recvImg();
                    int itemId = socketHelper.getInstance().recvInt();

                    // Add item to temporary list
                    tempCartItemList.add(new CartItemAdapter.cartItemData(
                            itemId, itemCount, itemName, merchName, itemPrice, totalPrice, itemImage
                    ));

                } catch (Exception e) {
                    Log.e("CartActivityTag", "Error loading cart item at index " + i, e);
                }
            }

            // Update the UI on the main thread
            mainHandler.post(() -> {
                cartItemDataList.clear();
                cartItemDataList.addAll(tempCartItemList);
                cartAdapter.notifyDataSetChanged();
            });

        } catch (IOException e) {
            Log.e("CartActivityTag", "Network error", e);
            mainHandler.post(() ->
                    Toast.makeText(CartActivity.this, "Network error. Please check your connection.", Toast.LENGTH_SHORT).show()
            );
        } catch (Exception e) {
            Log.e("CartActivityTag", "Unexpected error occurred while fetching cart items", e);
            mainHandler.post(() ->
                    Toast.makeText(CartActivity.this, "Unexpected error. Please try again later.", Toast.LENGTH_SHORT).show()
            );
        } finally {
            try {
                socketHelper.getInstance().close();
            } catch (IOException e) {
                Log.e("CartActivityTag", "Error closing socket", e);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!executor.isShutdown()) {
            executor.shutdownNow();
        }
    }
}
