package com.kaaa.talabat_lite;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MerchantOrdersFragment extends Fragment {

    private MerchantOrderAdapter merchantOrderAdapter;
    private List<MerchantOrderAdapter.orderData> orderList;
    private  ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    Button refreshButton;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (executor.isShutdown()) {
            executor = Executors.newSingleThreadExecutor();
        }
        View view = inflater.inflate(R.layout.fragment_merchant_orders, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.ordersRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        orderList = new ArrayList<>();
        merchantOrderAdapter = new MerchantOrderAdapter(getContext(), orderList);
        recyclerView.setAdapter(merchantOrderAdapter);
        initUI(view);
        setupListeners();
        fetchOrders();
        return view;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void getOrdersFromServer() {
        if (executor.isShutdown()) {
            executor = Executors.newSingleThreadExecutor(); // Recreate the executor if it's shut down
        }
        executor.execute(() -> {
            try {
                URL url = new URL(globals.serverURL + "/getMerchantActiveOrders/" + globals.userId);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                // Check response code
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    Log.e("MerchantOrdersFragment", "Error loading items from server");
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
                JSONArray ordersArray = jsonResponse.getJSONArray("orders");
                Log.i("merchantActiveOrdersCount", String.valueOf(ordersArray.length()));
                List<MerchantOrderAdapter.orderData> tempOrderList = new ArrayList<>();
                for (int i = 0; i < ordersArray.length(); i++) {
                    JSONObject itemJson = ordersArray.getJSONObject(i);
                    int orderId = itemJson.getInt("orderId");
                    String customerName = itemJson.getString("customerName");
                    float totalAmount = (float)itemJson.getDouble("totalAmount");
                    Log.i("MechantActiveOrders_order_orderId", String.valueOf(orderId));
                    tempOrderList.add(new MerchantOrderAdapter.orderData(orderId, customerName, totalAmount));
                }
                // Update the main OrderList and notify the adapter in the UI thread

                mainHandler.post(() -> {
                    if (isAdded()) { // Check if fragment is still attached
                        orderList.clear();
                        orderList.addAll(tempOrderList);
                        merchantOrderAdapter.notifyDataSetChanged();
                    }
                });



            } catch (IOException e) {
                Log.e("MerchantOrdersFragment", "Error loading items from server", e);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        });
    }

    protected void fetchOrders() {
        if (executor.isShutdown()) {
            executor = Executors.newSingleThreadExecutor(); // Recreate executor if it was shut down
        }
        executor.execute(() -> {
            mainHandler.post(() -> Toast.makeText(getContext(), "orders fetched", Toast.LENGTH_SHORT).show());
            getOrdersFromServer();
        });
    }

    protected void initUI(View view)
    {

        refreshButton = view.findViewById(R.id.refreshButton);
    }
    protected void refresh(){
        fetchOrders();

    }
    protected void setupListeners()
    {
        refreshButton.setOnClickListener(view -> new Thread(this::refresh).start());
    }
    @SuppressLint("DefaultLocale")
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (!executor.isShutdown()) {
            executor.shutdownNow();
        }
    }
}
