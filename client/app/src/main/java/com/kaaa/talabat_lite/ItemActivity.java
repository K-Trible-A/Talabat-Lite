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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ItemActivity extends AppCompatActivity {
    private ImageView itemImage;
    private TextView itemName, itemDescription, itemPrice, itemCount;
    private Button btnDecrease, btnIncrease, btnAddToCart;
    private int cnt = 1;


    public static class itemData {
        public String name, description;
        public float price;
        Bitmap img;
    }

    private ExecutorService executorService; // Executor for background tasks
    private Handler mainHandler; // Handler to update the UI thread

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);
        initUI();
        setupListeners();
        // Initialize ExecutorService and Handler
        executorService = Executors.newSingleThreadExecutor(); // For single background task
        mainHandler = new Handler(Looper.getMainLooper()); // For updating UI from the background thread
        // Just for testing, but ItemActivity must receive itemId from the previous activity!
        int itemId = 5;
        loadItemData(itemId);
    }
    private void showToast(String message) {
        runOnUiThread(() -> Toast.makeText(this, message, Toast.LENGTH_SHORT).show());
    }
    private void loadItemData(int itemId) {
        executorService.execute(() -> {
            try {
                Intent intent = getIntent();
                int itemId_final = intent.getIntExtra("id",itemId);
                itemData item = getItemData(itemId_final);
                // After background task, update UI on the main thread
                mainHandler.post(() -> {
                    assert item != null;
                    itemName.setText(item.name);
                    // Round the price to 1 decimal place
                    @SuppressLint("DefaultLocale") String formattedPrice = String.format("%.1f", item.price);
                    itemPrice.setText(formattedPrice);
                    itemDescription.setText(item.description);
                    itemImage.setImageBitmap(item.img);
                });
            } catch (IOException e) {
                Log.e("ItemActivity", "Error loading item data", e);
            }
        });
    }
    private Bitmap getItemImage(int itemId) throws IOException {
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


    private itemData getItemData(int itemId) throws IOException {
        itemData temp = new itemData();
        try {
            // Create URL connection
            URL url = new URL(globals.serverURL + "/get_item/" + itemId);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            int responseCode = conn.getResponseCode();
            // Check the response code
            if (responseCode != HttpURLConnection.HTTP_OK) {
                showToast("Error retrieving item's data");
                return null;
            }
            // Read the response
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            in.close();
            // Parse the response JSON
            JSONObject jsonResponse = new JSONObject(response.toString());
            temp.name = jsonResponse.getString("itemName");
            temp.description = jsonResponse.getString("itemDescription");
            temp.price = (float) jsonResponse.getDouble("itemPrice");

            temp.img = getItemImage(itemId);
        }catch (IOException e) {
            showToast("Failed to read response");
            return null;
        } catch (JSONException e) {
            showToast("Json response error");
            return null;
        }
        return temp;
    }


    private void initUI() {
        // Initialize UI elements
        itemImage = findViewById(R.id.item_image);
        itemName = findViewById(R.id.item_name);
        itemDescription = findViewById(R.id.item_description);
        itemPrice = findViewById(R.id.item_price);
        btnDecrease = findViewById(R.id.btn_decrease);
        btnIncrease = findViewById(R.id.btn_increase);
        btnAddToCart = findViewById(R.id.btn_add_to_cart);
        itemCount = findViewById(R.id.item_count);
    }

    private void setupListeners() {
        btnIncrease.setOnClickListener(v -> {
            cnt++;
            itemCount.setText(String.valueOf(cnt));
        });

        btnDecrease.setOnClickListener(v -> {
            if (cnt > 1) {
                cnt--;
                itemCount.setText(String.valueOf(cnt));
            }
        });

        btnAddToCart.setOnClickListener(v -> executorService.execute(() -> {
            Intent intent = getIntent();
            int itemId_final = intent.getIntExtra("id", 0);

            try{
                URL server = new URL(globals.serverURL + "/cart/add_item/" + globals.userId);
                // Create the JSON payload
                JSONObject jsonPayload = new JSONObject();
                jsonPayload.put("itemId", itemId_final);
                jsonPayload.put("itemCount", cnt);

                // Open a connection to the server
                HttpURLConnection conn = (HttpURLConnection) server.openConnection();
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
                    // Read the response
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) {
                        response.append(line);
                    }
                    in.close();
                    // Parse the response JSON
                    JSONObject jsonResponse = new JSONObject(response.toString());
                    //int merchId = jsonResponse.getInt("merchId");
                    int ok = jsonResponse.getInt("ok");

                    if(ok==1)
                    {
                        String message = cnt + " item(s) added to cart!";
                        runOnUiThread(()->{
                            Toast.makeText(ItemActivity.this, message, Toast.LENGTH_SHORT).show();

                            // Schedule the intent to start after the toast duration
                            // Optional, if you want to close the current activity
                            new Handler(Looper.getMainLooper()).postDelayed(this::finish, 1000); // Approximate Toast duration in milliseconds
                        });
                    }
                    else if (ok==0)
                    {
                        String message =" item(s) was in cart and update its quantity to "+cnt+" is successfully done !";
                        runOnUiThread(()->{
                            Toast.makeText(ItemActivity.this, message, Toast.LENGTH_SHORT).show();
                            Runnable runnable = () -> {
                                Intent outIntent = new Intent(ItemActivity.this, CartActivity.class);
                                finish();
                                startActivity(outIntent);
                            };
                            new Handler(Looper.getMainLooper()).postDelayed(runnable, 1000); // Approximate Toast duration in milliseconds
                        });
                    }
                }
                else {
                    showToast("Uploading error");
                    Log.i("AddItem", conn.getResponseMessage());
                }
                conn.disconnect();
            } catch (IOException | JSONException ignored) {

            }
        }));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up resources
        if (executorService != null) {
            executorService.shutdown(); // Shut down the executor when activity is destroyed
        }
    }
}