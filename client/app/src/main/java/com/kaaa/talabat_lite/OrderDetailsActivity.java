package com.kaaa.talabat_lite;

import static java.security.AccessController.getContext;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OrderDetailsActivity extends AppCompatActivity {

    private TextView orderId, merchantName, orderStatus, createdAt, pickupAddress, customerAddress, totalAmount;
    private Button okButton;
    private String orderIdStr, merchantNameStr, orderStatusStr, createdAtStr, pickupAddressStr, customerAddressStr, totalAmountStr, okButtonStr;
    private String accountTypeStr;
    private ItemOrderAdapter itemOrderAdapter;
    private List<ItemOrderAdapter.itemData> itemList;
    private ExecutorService executorService;
    private Handler mainHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_order_details);
        RecyclerView recyclerView = findViewById(R.id.itemsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
        Intent outIntent = getIntent();
        orderIdStr = outIntent.getStringExtra("orderId");
        itemList = new ArrayList<>();
        itemOrderAdapter = new ItemOrderAdapter(this, itemList);
        recyclerView.setAdapter(itemOrderAdapter);
        initUI();
        getAccountType();
        setupListeners();
        loadOrderFromServer();
    }

    private void showToast(String message) {
        runOnUiThread(() -> Toast.makeText(this, message, Toast.LENGTH_SHORT).show());
    }

    private void getAccountType() {
        executorService.execute(() -> {
            try {
                URL url = new URL(globals.serverURL + "/getAccountType/" + globals.userId);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                // Check response code
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    Log.e("OrderDetailsActivity", "Error loading items from server");
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
                accountTypeStr = jsonResponse.getString("accountType");
            } catch (IOException e) {
                Log.e("OrderDetailsActivity", "Error loading Order data", e);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void loadOrderFromServer() {
        executorService.execute(() -> {
            try {
                URL url = new URL(globals.serverURL + "/getOrderDetails/" + orderIdStr);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                // Check response code
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    Log.e("OrderDetailsActivity", "Error loading items from server");
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
                merchantNameStr = jsonResponse.getString("businessName");
                pickupAddressStr = jsonResponse.getString("pickupAddress");
                totalAmountStr = String.valueOf(jsonResponse.getDouble("totalAmount"));
                orderStatusStr = jsonResponse.getString("orderStatus");
                createdAtStr = jsonResponse.getString("createdAt");
                customerAddressStr = jsonResponse.getString("customerAddress");
                JSONArray itemsArray = jsonResponse.getJSONArray("items");
                Log.i("OrderItemsCount", String.valueOf(itemsArray.length()));
                List<ItemOrderAdapter.itemData> tempItemList = new ArrayList<>();
                for (int i = 0; i < itemsArray.length(); i++) {
                    JSONObject itemJson = itemsArray.getJSONObject(i);
                    int quantity = itemJson.getInt("quantity");
                    String itemName = itemJson.getString("itemName");
                    float itemPrice = (float) itemJson.getDouble("itemPrice");
                    Log.i("Order_item_itemName", String.valueOf(itemName));
                    tempItemList.add(new ItemOrderAdapter.itemData(quantity, itemName, itemPrice));
                }
                mainHandler.post(() -> {
                    itemList.clear(); // Clear existing items
                    itemList.addAll(tempItemList);
                    orderId.setText(orderIdStr);
                    merchantName.setText(merchantNameStr);
                    pickupAddress.setText(pickupAddressStr);
                    totalAmount.setText(totalAmountStr);
                    orderStatus.setText(orderStatusStr);
                    createdAt.setText(createdAtStr);
                    customerAddress.setText(customerAddressStr);
                    // set the button text
                    if (accountTypeStr.equals("1")) {
                        okButton.setText(R.string.continue1);
                    } else {
                        okButton.setText(R.string.accept_order);
                    }
                    itemOrderAdapter.notifyDataSetChanged();
                });
            } catch (IOException e) {
                Log.e("OrderDetailsActivity", "Error loading Order data", e);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        });
    }


    protected void initUI() {
        //orderId,merchantName,orderStatus,createdAt,pickupAddress,customerAddress,totalAmount;
        orderId = findViewById(R.id.orderId);
        merchantName = findViewById(R.id.merchantName);
        orderStatus = findViewById(R.id.orderStatus);
        createdAt = findViewById(R.id.createdAt);
        pickupAddress = findViewById(R.id.pickupAddress);
        customerAddress = findViewById(R.id.customerAddress);
        totalAmount = findViewById(R.id.totalAmount);
        okButton = findViewById(R.id.okButton);
    }

    public void merchantAcceptOrder() {
        executorService.execute(() -> {
            try {
                URL url = new URL(globals.serverURL + "/merchantAcceptOrder/" + globals.userId);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                // Check response code
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    Log.e("merchantAcceptOrder", "Error loading items from server");
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
                if (jsonResponse.getInt("succeeded") == 1) {
                    Toast.makeText(this, "Order Accepted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Failed to accept order", Toast.LENGTH_SHORT).show();
                }
            } catch (IOException e) {
                Log.e("OrderDetailsActivity ->Merchant Accept Order", "Error loading Order data", e);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        });
    }
    public void courierAcceptOrder() {
        executorService.execute(() -> {
            try {
                URL url = new URL(globals.serverURL + "/courierAcceptOrder/" + globals.userId);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                // Check response code
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    Log.e("CourierAcceptOrder", "Error loading items from server");
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


                if (jsonResponse.getInt("succeeded") == 1) {
                    runOnUiThread(() -> Toast.makeText(OrderDetailsActivity.this, "Order Accepted", Toast.LENGTH_SHORT).show());
                } else {
                    runOnUiThread(() -> Toast.makeText(OrderDetailsActivity.this, "This order has already been accepted by your friend. Please select a different order. ", Toast.LENGTH_SHORT).show());
                }
            } catch (IOException e) {
                Log.e("OrderDetailsActivity ->Courier Accept Order", "Error loading Order data", e);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        });
    }

    protected void setupListeners() {
        okButton.setOnClickListener(v -> executorService.execute(() -> {

            if (accountTypeStr.equals("1")) {

            } else if (accountTypeStr.equals("2")) {       //Merchant
                merchantAcceptOrder();
            } else {                                      //Courier
                courierAcceptOrder();
            }
            finish();
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