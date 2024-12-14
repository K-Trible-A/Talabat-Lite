package com.kaaa.talabat_lite;

import android.annotation.SuppressLint;
import android.content.Intent;
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

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ItemManagementActivity extends AppCompatActivity {

    private ImageView itemImage;
    private TextView itemName, itemDescription, itemPrice;
    private String itemNameStr, itemDescriptionStr;
    private int itemId;
    private float itemPriceF;
    private Button deleteButton;
    private ExecutorService executorService;
    private Handler mainHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_item_management);
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
        Intent outIntent = getIntent();
        itemNameStr = outIntent.getStringExtra("name");
        itemDescriptionStr= outIntent.getStringExtra("description");
        itemPriceF = outIntent.getFloatExtra("price",0);
        itemId = outIntent.getIntExtra("id",-1);
        initUI();
        setupListeners();
        loadItemData();
    }
    private void showToast(String message) {
        runOnUiThread(() -> Toast.makeText(this, message, Toast.LENGTH_SHORT).show());
    }
    private void loadItemData() {
        executorService.execute(() -> {
            try {
                Bitmap itemImg = getItemImage();
                // After background task, update UI on the main thread
                mainHandler.post(() -> {
                    itemName.setText(itemNameStr);
                    // Round the price to 1 decimal place
                    @SuppressLint("DefaultLocale") String formattedPrice = String.format("%.1f", itemPriceF) + "$";
                    itemPrice.setText(formattedPrice);
                    itemDescription.setText(itemDescriptionStr);
                    itemImage.setImageBitmap(itemImg);
                });
            } catch (IOException e) {
                Log.e("ItemActivity", "Error loading item data", e);
            }
        });
    }

    private Bitmap getItemImage() throws IOException {
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
                showToast("Error retrieving item's data");
                return null;
            }
            // Read the response
            InputStream inputStream = conn.getInputStream();
            temp = BitmapFactory.decodeStream(inputStream);
            inputStream.close();
        } catch (IOException e) {
            showToast("Failed to read response");
            return null;
        }
        return temp;
    }
    protected void initUI ()
    {
        itemName = findViewById(R.id.item_name);
        itemDescription = findViewById(R.id.item_description);
        itemPrice = findViewById(R.id.item_price);
        itemImage = findViewById(R.id.item_image);
        deleteButton = findViewById(R.id.btn_delete);
    }
    private boolean deleteItem() throws IOException
    {
        URL url = new URL(globals.serverURL + "/delete_item/" + itemId);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        int responseCode = conn.getResponseCode();
        return responseCode == HttpURLConnection.HTTP_OK;
    }
    protected void setupListeners ()
    {
        deleteButton.setOnClickListener(v -> executorService.execute(() -> {
            try {
                // Send delete request to server
                boolean isDeleted = deleteItem();
                // After background task, update UI on the main thread
                mainHandler.post(() -> {

                    if (isDeleted)
                    {
                        Toast.makeText(ItemManagementActivity.this,"Item deleted successfully!",Toast.LENGTH_SHORT).show();
                        Intent outIntent = new Intent(ItemManagementActivity.this,MerchantActivity.class);
                        startActivity(outIntent);
                    }
                    else
                    {
                        Toast.makeText(ItemManagementActivity.this, "Failed to delete item", Toast.LENGTH_SHORT).show();
                    }

                });
            } catch (IOException e) {
                Log.e("ItemActivity", "Error deleting item", e);
                mainHandler.post(() -> Toast.makeText(ItemManagementActivity.this, "Error deleting item", Toast.LENGTH_SHORT).show());
            }
        }));
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up resources

        if (!executorService.isShutdown()) {
            executorService.shutdownNow();
        }
    }
}