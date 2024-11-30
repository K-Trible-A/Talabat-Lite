package com.kaaa.talabat_lite;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
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

    private void loadItemData(int itemId) {
        executorService.execute(() -> {
            try {
                itemData item = getItemData(itemId);
                // After background task, update UI on the main thread
                mainHandler.post(() -> {
                    itemName.setText(item.name);
                    itemPrice.setText(String.valueOf(item.price));
                    itemDescription.setText(item.description);
                    itemImage.setImageBitmap(item.img);
                });
            } catch (IOException e) {
                Log.e("ItemActivity", "Error loading item data", e);
            }
        });
    }

    private itemData getItemData(int itemId) throws IOException {
        itemData temp = new itemData();
        socketHelper.getInstance().connect();
        socketHelper.getInstance().sendInt(globals.RETRIEVE_ITEM);
        socketHelper.getInstance().sendInt(itemId);

        temp.name = socketHelper.getInstance().recvString();
        temp.img = socketHelper.getInstance().recvImg();
        temp.description = socketHelper.getInstance().recvString();
        temp.price = socketHelper.getInstance().recvFloat();

        socketHelper.getInstance().close();
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
