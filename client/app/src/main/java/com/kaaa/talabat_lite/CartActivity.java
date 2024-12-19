package com.kaaa.talabat_lite;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

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
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CartActivity extends AppCompatActivity {

    private CartItemAdapter cartAdapter;
    private final List<CartItemAdapter.cartItemData> cartItemDataList = new ArrayList<>();
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    int firstItemId = 0;
    float totalAmount = 0;

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
        btnOrderNow.setOnClickListener(view -> new Thread(this::saveOrder).start());


        // Fetch cart info
        fetchCartInfo();
    }
    private void removeCartItems()
    {
        try
        {
            URL url = new URL(globals.serverURL + "/delete_cart/");
            JSONObject jsonPayload = new JSONObject();
            jsonPayload.put("firstItemId", firstItemId);
            jsonPayload.put("userId", globals.userId);
            // Open a connection to the server
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true); // To send a body
            // Send the request
            OutputStream os = conn.getOutputStream();
            os.write(jsonPayload.toString().getBytes(StandardCharsets.UTF_8));
            os.flush();
            os.close();
            // Get the response code
            int responseCode = conn.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                showToast("Deleted Cart Items successfully!");
            }
            else {
                showToast("Deletion Error!!");
                Log.i("CartActivity", conn.getResponseMessage());
            }
            conn.disconnect();

        } catch (IOException e) {
            showToast("Failed to read response");
        } catch (JSONException e) {
            Log.e("CartActivity", "Json error");
        }
    }
    private void saveOrder()
    {
        if (firstItemId == 0)
        {
            showToast("Cart is empty !");
            return;
        }
        try
        {
            URL url = new URL(globals.serverURL + "/save_order/");
            JSONObject jsonPayload = new JSONObject();
            jsonPayload.put("userId", globals.userId);
            jsonPayload.put("firstItemId", firstItemId);
            jsonPayload.put("totalAmount", totalAmount);
            Log.d("Cart","id = " + globals.userId);
            Log.d("Cart","item id = " + firstItemId);
            Log.d("Cart","total = " + totalAmount);
            // Open a connection to the server
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true); // To send a body
            // Send the request
            OutputStream os = conn.getOutputStream();
            os.write(jsonPayload.toString().getBytes(StandardCharsets.UTF_8));
            os.flush();
            os.close();

            // Get the response code
            int responseCode = conn.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                showToast("Saving order success");
                removeCartItems();
                Intent outIntent = new Intent(CartActivity.this, CustomerActivity.class);
                outIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(outIntent);
                finish();
            }
            else if(responseCode == HttpURLConnection.HTTP_CONFLICT){
                showToast("Account with same email or phone number");
            }
            else {
                showToast("Registration error");
                Log.i("CartActivity", conn.getResponseMessage());
            }
            conn.disconnect();

        } catch (IOException e) {
            showToast("Failed to read response");
        } catch (JSONException e) {
            Log.e("MerchantRegistration", "Json error");
        }
    }
    private void showToast(String message) {
        runOnUiThread(() -> Toast.makeText(this, message, Toast.LENGTH_SHORT).show());
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
                        Toast.makeText(CartActivity.this, "No items in the cart", Toast.LENGTH_SHORT).show()
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
            // Construct the URL to fetch cart items for the user
            URL url = new URL(globals.serverURL + "/cart/items/" + globals.userId);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            // Check if the response is OK
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                Toast.makeText(CartActivity.this, "Unexpected error. Please try again later.", Toast.LENGTH_SHORT).show();
                Log.e("cartActivity", "Error loading cart items from server");
                return;
            }
            // Read the response from the server
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            in.close();
            // Parse the JSON response
            JSONObject jsonResponse = new JSONObject(response.toString());
            JSONArray itemsArray = jsonResponse.getJSONArray("items");
            // Iterate through each item in the response
            for (int i = 0; i < itemsArray.length(); i++) {
                JSONObject itemJson = itemsArray.getJSONObject(i);
                // Extract the necessary fields from the JSON response
                String itemName = itemJson.getString("itemName");
                int itemCount = itemJson.getInt("itemCount");
                float itemPrice = (float) itemJson.getDouble("itemPrice");
                String merchName = itemJson.getString("merchName");
                float totalPrice = (float) itemJson.getDouble("TotalPrice");
                totalAmount += totalPrice;
                int itemId = itemJson.getInt("itemId");
                int imageId = itemJson.getInt("imageId");
                // Assuming you have a method `getItemImage(imageId)` to get the Bitmap for the item image
                Bitmap itemImage = getItemImage(imageId);
                // Add the item to the temporary list
                tempCartItemList.add(new CartItemAdapter.cartItemData(
                        itemId, itemCount, itemName, merchName, itemPrice, totalPrice, itemImage
                ));
            }
            // Update the UI on the main thread with the loaded cart items
            mainHandler.post(() -> {
                cartItemDataList.clear();  // Clear the existing data
                cartItemDataList.addAll(tempCartItemList);  // Add the new data
                cartAdapter.notifyDataSetChanged();  // Notify the adapter to update the view
            });
        } catch (IOException e) {
            Log.e("CartActivity", "Error loading items from server", e);
        } catch (JSONException e) {
            Log.e("CartActivity", "Error parsing JSON response", e);
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
