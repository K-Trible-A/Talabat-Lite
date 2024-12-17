package com.kaaa.talabat_lite;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CartActivity extends AppCompatActivity {

    private CartItemAdapter cartAdapter;
    private final List<CartItemAdapter.cartItemData> cartItemDataList = new ArrayList<>();
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        // Initialize RecyclerView
        RecyclerView recyclerView = findViewById(R.id.cartRecyclerView);
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
    private Bitmap getItemImage(int itemId) {
        Bitmap temp;
        try {
            // Create URL connection
            URL url = new URL(globals.serverURL + "/get_item_image/" + itemId);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setDoInput(true);

            int responseCode = conn.getResponseCode();
            // Check the response code
            if (responseCode != HttpURLConnection.HTTP_OK) {
                return null;
            }
            // Read the response
            InputStream inputStream = conn.getInputStream();
            temp = BitmapFactory.decodeStream(inputStream);
            inputStream.close();
        } catch (IOException e) {
            return null;
        }
        return temp;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadCartItemsFromServer() {
        List<CartItemAdapter.cartItemData> tempCartItemList = new ArrayList<>();

        try {
            URL url = new URL(globals.serverURL + "/customer/getCartItems/" + globals.userId);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            // Check response code
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                Toast.makeText(CartActivity.this, "Unexpected error. Please try again later.", Toast.LENGTH_SHORT).show();
                Log.e("cartActivit", "Error loading cart items from server");
                return;
            }
            // Read the response
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            in.close();
            // Parse the response JSON
            JSONObject jsonResponse = new JSONObject(response.toString());
            JSONArray itemsArray = jsonResponse.getJSONArray("items");
            for (int i = 0; i < itemsArray.length(); i++) {
                JSONObject itemJson = itemsArray.getJSONObject(i);

                String itemName = itemJson.getString("itemName");
                int itemCount = itemJson.getInt("itemCount");
                float itemPrice = (float) itemJson.getDouble("itemPrice");
                String merchName = itemJson.getString("merchName");
                float totalPrice = (float) itemJson.getDouble("TotalPrice");
                int itemId = itemJson.getInt("itemId");
                Bitmap itemImage = getItemImage(itemJson.getInt("imageId"));

                // Add item to temporary list
                tempCartItemList.add(new CartItemAdapter.cartItemData(
                        itemId, itemCount, itemName, merchName, itemPrice, totalPrice, itemImage
                ));
            }

            // Update the UI on the main thread
            mainHandler.post(() -> {
                cartItemDataList.clear();
                cartItemDataList.addAll(tempCartItemList);
                cartAdapter.notifyDataSetChanged();
            });

        } catch (IOException e) {
            Log.e("MerchantHomeFragment", "Error loading items from server", e);
        } catch (JSONException e) {
            throw new RuntimeException(e);
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
