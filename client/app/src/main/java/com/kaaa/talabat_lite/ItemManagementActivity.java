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

    ImageView itemImage;
    TextView itemName, itemDescription, itemPrice;
    String itemNameStr, itemDescriptionStr;
    int itemId;
    float itemPriceF;
    Button deleteButton;
    Intent outIntent;
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
        itemNameStr = outIntent.getStringExtra("name");
        itemDescriptionStr= outIntent.getStringExtra("description");
        itemPriceF = outIntent.getFloatExtra("price",0);
        itemId = outIntent.getIntExtra("id",-1);
        initUI();
        setupListeners();
        loadItemData();
    }
    private void loadItemData() {
        executorService.execute(() -> {
            try {
                Bitmap itemImg = getItemData();
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
    private Bitmap getItemData() throws IOException {
        Bitmap temp;
        socketHelper.getInstance().connect();
        socketHelper.getInstance().sendInt(globals.GET_IMAGE); // only the image is needed, we have the other data
        socketHelper.getInstance().sendInt(itemId);
        temp = socketHelper.getInstance().recvImg();
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
         socketHelper.getInstance().connect();
         socketHelper.getInstance().sendInt(globals.DELETE_ITEM);
         socketHelper.getInstance().sendInt(itemId);
         int ok = socketHelper.getInstance().recvInt();
         return ok == 1;
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
                        outIntent = new Intent(ItemManagementActivity.this,MerchantActivity.class);
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