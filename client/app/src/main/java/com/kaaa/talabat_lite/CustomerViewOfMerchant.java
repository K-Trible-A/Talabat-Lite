package com.kaaa.talabat_lite;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
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


public class CustomerViewOfMerchant extends AppCompatActivity {

    private ItemAdapter itemAdapter;
    private List<ItemAdapter.itemData> itemList;
    private  ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    TextView merchantName, merchantRating, merchantKeywords;
    String merchantNameStr, merchantKeywordsStr;
    float rating;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (executor.isShutdown()) {
            executor = Executors.newSingleThreadExecutor();
        }
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_customer_viewof_merchant); // Link to your XML layout
        RecyclerView recyclerView = findViewById(R.id.customerRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        itemList = new ArrayList<>();
        itemAdapter = new ItemAdapter(this, itemList);
        recyclerView.setAdapter(itemAdapter);
        initUI();
        fetchMerchantInfo();
    }
    private void getMerchantInfo ()
    {
        try {
            // Create URL connection
            URL url = new URL(globals.serverURL + "/customer/getMerchantInfoHome/" + getIntent().getIntExtra("merch_id", 0));
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            int responseCode = conn.getResponseCode();
            // Check the response code
            if (responseCode != HttpURLConnection.HTTP_OK) {
                Log.e("MerchantHomeFragment", "Error retrieving merchant data");
                return;
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
            merchantNameStr = jsonResponse.getString("businessName");
            merchantKeywordsStr = jsonResponse.getString("keywords");
            rating = (float) jsonResponse.getDouble("rating");
        }catch (IOException e) {
            Log.e("MerchantHomeFragment", "Failed to read response");
        } catch (JSONException e) {
            Log.e("MerchantHomeFragment", "Json error");
        }
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
    @SuppressLint("NotifyDataSetChanged")
    private void loadItemsFromServer() {
        new Thread(()->{
            try {
                URL url = new URL(globals.serverURL + "/customer/get_items/" + getIntent().getIntExtra("merch_id", 0));
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                // Check response code
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    Log.e("CustomerViewOfMerchant", "Error loading items from server");
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
                Log.i("HomeItems_items_count", String.valueOf(itemsArray.length()));

                List<ItemAdapter.itemData> tempItemList = new ArrayList<>();  // Temporary list to hold items

                for (int i = 0; i < itemsArray.length(); i++) {
                    JSONObject itemJson = itemsArray.getJSONObject(i);
                    int itemId = itemJson.getInt("itemId");
                    String itemName = itemJson.getString("itemName");
                    String itemDescription = itemJson.getString("itemDescription");
                    double itemPrice = itemJson.getDouble("itemPrice");
                    //int imageId = itemJson.getInt("imageId");
                    Log.i("HomeItems_items_itemId", String.valueOf(itemId));
                    Bitmap itemImg = getItemImage(itemId);
                    tempItemList.add(new ItemAdapter.itemData(itemId, itemName, itemDescription, (float) itemPrice, itemImg));
                }
                connection.disconnect();

                // Update the main itemList and notify the adapter in the UI thread
                mainHandler.post(() -> {
                    itemList.clear();
                    itemList.addAll(tempItemList);
                    itemAdapter.notifyDataSetChanged();

                });

            } catch (IOException e) {
                Log.e("MerchantHomeFragment", "Error loading items from server", e);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }).start();

    }


    private void fetchMerchantInfo() {
        executor.execute(() -> {
            getMerchantInfo();
            mainHandler.post(() -> {
                loadItemsFromServer();
                updateUI();
                // Load items only after merchant info is fetched
            });
        });
    }
    protected void initUI()
    {
        merchantName = findViewById(R.id.merchantName);
        merchantKeywords = findViewById(R.id.merchantKeywords);
        merchantRating = findViewById(R.id.merchantRating);
    }
    @SuppressLint("DefaultLocale")
    private void updateUI()
    {
        merchantName.setText(merchantNameStr);
        merchantRating.setText(String.format("Rating: %.1f", rating));
        merchantKeywords.setText(merchantKeywordsStr);
    }
}