package com.kaaa.talabat_lite;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
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
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ItemActivity extends AppCompatActivity {
    private ImageView itemImage;
    private TextView itemName, itemDescription, itemPrice, itemCount;
    private Button btnDecrease, btnIncrease, btnAddToCart;
    private int cnt = 1;


    public static class itemData {
        public String name, description;
        public Double price;
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
        int itemId = 1;
        loadItemData(itemId);
    }

    private void loadItemData(int itemId) {
        executorService.execute(() -> {
            try {
                itemData item = getItemData(itemId);
                // After background task, update UI on the main thread
                mainHandler.post(() -> {
                    itemName.setText(item.name);
                    // Round the price to 1 decimal place
                    String formattedPrice = String.format("%.1f", item.price);
                    itemPrice.setText(formattedPrice);
                    itemDescription.setText(item.description);
                    itemImage.setImageBitmap(item.img);
                });
            } catch (IOException e) {
                Log.e("ItemActivity", "Error loading item data", e);
            }
        });
    }
    private void showToast(String message) {
        runOnUiThread(() -> Toast.makeText(this, message, Toast.LENGTH_SHORT).show());
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
            temp.price = jsonResponse.getDouble("itemPrice");
            String imageBase64 = jsonResponse.getString("itemImg");
            Log.i("ImageSize", String.valueOf(imageBase64.length()));
            byte[] decodedImage = Base64.decode(imageBase64, Base64.DEFAULT);
            Log.i("ImageSize", String.valueOf(decodedImage.length));
            temp.img = BitmapFactory.decodeByteArray(decodedImage, 0, decodedImage.length);
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

        btnAddToCart.setOnClickListener(v -> {
            String message = cnt + " item(s) added to cart!";
            Toast.makeText(ItemActivity.this, message, Toast.LENGTH_SHORT).show();
        });
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
