package com.kaaa.talabat_lite;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ItemManagementActivity extends AppCompatActivity {

    private ImageView itemImage;
    private TextView itemName, itemDescription, itemPrice;
    private String itemNameStr, itemDescriptionStr;
    private int itemId;
    private float itemPriceF;
    private Button deleteButton;
    private Intent outIntent;
    private ExecutorService executorService;
    private Handler mainHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_item_management);

        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        outIntent = getIntent();
        if (outIntent != null) {
            itemNameStr = outIntent.getStringExtra("name");
            itemDescriptionStr = outIntent.getStringExtra("description");
            itemPriceF = outIntent.getFloatExtra("price", 0);
            itemId = outIntent.getIntExtra("id", -1);
        } else {
            Toast.makeText(this, "Invalid item data received", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initUI();
        setupListeners();
        loadItemData();
    }

    private void loadItemData() {
        executorService.execute(() -> {
            int retryCount = 3; // Number of retries
            boolean isLoaded = false;

            while (retryCount > 0 && !isLoaded) {
                try {
                    Bitmap itemImg = getItemData();
                    isLoaded = true; // Data successfully loaded
                    mainHandler.post(() -> {
                        if (!isFinishing()) {
                            itemName.setText(itemNameStr);
                            @SuppressLint("DefaultLocale")
                            String formattedPrice = String.format("%.1f", itemPriceF) + "$";
                            itemPrice.setText(formattedPrice);
                            itemDescription.setText(itemDescriptionStr);
                            itemImage.setImageBitmap(itemImg);
                        }
                    });
                } catch (IOException e) {
                    Log.e("ItemActivity", "Error loading item data, retries left: " + (retryCount - 1), e);
                    retryCount--;
                    if (retryCount == 0) {
                        mainHandler.post(() -> {
                            if (!isFinishing()) {
                                Toast.makeText(this, "Failed to load item data after multiple attempts.", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                } finally {
                    try {
                        socketHelper.getInstance().close(); // Always close the socket
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
    }
    private Bitmap getItemData() throws IOException {
        socketHelper.getInstance().connect();
        socketHelper.getInstance().sendInt(globals.GET_IMAGE);
        socketHelper.getInstance().sendInt(itemId);
        Bitmap temp = socketHelper.getInstance().recvImg();
        socketHelper.getInstance().close(); // Ensure socket is closed
        return temp;
    }

    private boolean deleteItem() throws IOException {
        socketHelper.getInstance().connect();
        socketHelper.getInstance().sendInt(globals.DELETE_ITEM);
        socketHelper.getInstance().sendInt(itemId);
        Log.d("Items", "Sent " + itemId);
        int ok = socketHelper.getInstance().recvInt();
        socketHelper.getInstance().close(); // Ensure socket is closed
        Log.d("Items", "Received " + ok);
        return ok == 1;
    }

    protected void initUI() {
        itemName = findViewById(R.id.item_name);
        itemDescription = findViewById(R.id.item_description);
        itemPrice = findViewById(R.id.item_price);
        itemImage = findViewById(R.id.item_image);
        deleteButton = findViewById(R.id.btn_delete);
    }

    protected void setupListeners() {
        deleteButton.setOnClickListener(v -> executorService.execute(() -> {
            try {
                boolean isDeleted = deleteItem();
                mainHandler.post(() -> {
                    if (!isFinishing()) {
                        if (isDeleted) {
                            Toast.makeText(ItemManagementActivity.this, "Item deleted successfully!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(ItemManagementActivity.this, MerchantActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(ItemManagementActivity.this, "Failed to delete item", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } catch (IOException e) {
                Log.e("ItemActivity", "Error deleting item", e);
                mainHandler.post(() -> Toast.makeText(this, "Error deleting item", Toast.LENGTH_SHORT).show());
            } finally {
                try {
                    socketHelper.getInstance().close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!executorService.isShutdown()) {
            executorService.shutdownNow();
        }
    }
}
