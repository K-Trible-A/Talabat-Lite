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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MerchantHomeFragment extends Fragment {

    private ItemAdapter itemAdapter;
    private List<ItemAdapter.itemData> itemList;
    private  ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    TextView merchantName, merchantRating, merchantKeywords;
    String merchantNameStr, merchantKeywordsStr;
    Button addItemButton;
    double rating;
    Intent addItemIntent;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (executor.isShutdown()) {
            executor = Executors.newSingleThreadExecutor();
        }
        View view = inflater.inflate(R.layout.fragment_merchant_home, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.itemRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        itemList = new ArrayList<>();
        itemAdapter = new ItemAdapter(getContext(), itemList);
        recyclerView.setAdapter(itemAdapter);
        initUI(view);
        setupListeners();
        fetchMerchantInfo();
        return view;
    }

    private void getMerchantInfo ()
    {
        try {
            // Create URL connection
            URL url = new URL(globals.serverURL + "/getMerchantInfoHome/" + globals.userId);
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
        if (executor.isShutdown()) {
            executor = Executors.newSingleThreadExecutor(); // Recreate the executor if it's shut down
        }
        executor.execute(() -> {
            try {
                URL url = new URL(globals.serverURL + "/get_items/" + globals.userId);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                // Check response code
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    Log.e("MerchantHomeFragment", "Error loading items from server");
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
                List<ItemAdapter.itemData> tempItemList = new ArrayList<>();
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

                // Update the main itemList and notify the adapter in the UI thread
                mainHandler.post(() -> {
                    if (isAdded()) { // Check if fragment is still attached
                        itemList.clear();
                        itemList.addAll(tempItemList);
                        itemAdapter.notifyDataSetChanged();
                    }
                });

            } catch (IOException e) {
                Log.e("MerchantHomeFragment", "Error loading items from server", e);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void fetchMerchantInfo() {
        if (executor.isShutdown()) {
            executor = Executors.newSingleThreadExecutor(); // Recreate executor if it was shut down
        }

        executor.execute(() -> {
            getMerchantInfo();
            mainHandler.post(() -> {
                if (isAdded()) { // Check if fragment is still attached
                    updateUI(); // Update UI after fetching merchant info
                    loadItemsFromServer(); // Load items only after merchant info is fetched
                }
            });
        });
    }

protected void initUI(View view)
    {
        merchantName = view.findViewById(R.id.merchantName);
        merchantKeywords = view.findViewById(R.id.merchantKeywords);
        merchantRating = view.findViewById(R.id.merchantRating);
        addItemButton = view.findViewById(R.id.addItemButton);
    }
    protected void setupListeners()
    {
        addItemButton.setOnClickListener(view -> new Thread(this::addItem).start());
    }
    private void addItem ()
    {
        addItemIntent = new Intent(requireContext(),AddItemActivity.class);
        startActivity(addItemIntent);
    }
    @SuppressLint("DefaultLocale")
    private void updateUI()
    {
        merchantName.setText(merchantNameStr);
        merchantRating.setText(String.format("Rating: %.1f", rating));
        merchantKeywords.setText(merchantKeywordsStr);
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (!executor.isShutdown()) {
            executor.shutdownNow();
        }
    }
}
