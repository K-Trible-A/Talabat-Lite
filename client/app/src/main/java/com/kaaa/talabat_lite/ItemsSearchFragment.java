package com.kaaa.talabat_lite;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.appcompat.widget.SearchView;
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

public class ItemsSearchFragment extends Fragment {

    private RecyclerView itemsRecyclerView;
    private ItemAdapter itemAdapter;
    private SearchView searchView;
    private List<ItemAdapter.itemData> allItems; // Original list from the server
    private List<ItemAdapter.itemData> displayedItems; // Displayed list
    private Handler uiHandler;
    private ExecutorService executor;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_items_search, container, false);

        // Initialize executor and UI thread handler
        executor = Executors.newSingleThreadExecutor();
        uiHandler = new Handler(Looper.getMainLooper());

        // Initialize data lists
        allItems = new ArrayList<>();
        displayedItems = new ArrayList<>();

        // Set up the UI components
        initializeUI(view);

        // Load items from the server
        loadItemsFromServer();

        return view;
    }

    private void initializeUI(View view) {
        // Set up RecyclerView
        itemsRecyclerView = view.findViewById(R.id.itemsRecyclerView);
        itemsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        itemAdapter = new ItemAdapter(getContext(), displayedItems);
        itemsRecyclerView.setAdapter(itemAdapter);

        // Set up SearchView
        searchView = view.findViewById(R.id.item_search_bar);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterList(newText);
                return false;
            }
        });
    }

    private void loadItemsFromServer() {
        executor.execute(() -> {
            HttpURLConnection conn = null;
            BufferedReader reader = null;
            try {
                URL url = new URL(globals.serverURL + "/getItemsSearchResults/" + globals.userId);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(15000);

                if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    Log.e("ItemsSearchFragment", "Failed to load items, Response Code: " + conn.getResponseCode());
                    uiHandler.post(() -> Toast.makeText(getContext(), "Failed to load items.", Toast.LENGTH_SHORT).show());
                    return;
                }

                // Read server response
                reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                // Parse JSON response
                JSONArray itemsArray = new JSONObject(response.toString()).getJSONArray("items");
                List<ItemAdapter.itemData> tempItems = new ArrayList<>();
                for (int i = 0; i < itemsArray.length(); i++) {
                    JSONObject item = itemsArray.getJSONObject(i);
                    int id = item.getInt("itemId");
                    String name = item.getString("itemName");
                    String description = item.getString("itemDescription");
                    double price = item.getDouble("itemPrice");
                    Bitmap img = getItemImage(id);

                    tempItems.add(new ItemAdapter.itemData(id, name, description, (float) price, img));
                }

                // Update UI on the main thread
                uiHandler.post(() -> {
                    allItems.clear();
                    allItems.addAll(tempItems);
                    updateItemList(tempItems);
                });

            } catch (IOException | JSONException e) {
                Log.e("ItemsSearchFragment", "Error loading items", e);
                uiHandler.post(() -> Toast.makeText(getContext(), "Failed to load items: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        Log.e("ItemsSearchFragment", "Error closing reader", e);
                    }
                }
                if (conn != null) {
                    conn.disconnect();
                }
            }
        });
    }

    private Bitmap getItemImage(int itemId) {
        Bitmap bitmap = null;
        HttpURLConnection conn = null;
        InputStream inputStream = null;
        try {
            URL url = new URL(globals.serverURL + "/get_item_image/" + itemId);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(15000);

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                inputStream = conn.getInputStream();
                bitmap = BitmapFactory.decodeStream(inputStream);
            }
        } catch (IOException e) {
            Log.e("ItemsSearchFragment", "Error loading item image", e);
        } finally {
            try {
                if (inputStream != null) inputStream.close();
                if (conn != null) conn.disconnect();
            } catch (IOException e) {
                Log.e("ItemsSearchFragment", "Error closing image stream", e);
            }
        }
        return bitmap;
    }

    private void filterList(String searchText) {
        List<ItemAdapter.itemData> filteredItems = new ArrayList<>();
        for (ItemAdapter.itemData item : allItems) {
            if (item.name.toLowerCase().contains(searchText.toLowerCase()) ||
                    item.description.toLowerCase().contains(searchText.toLowerCase())) {
                filteredItems.add(item);
            }
        }
        updateItemList(filteredItems);
    }

    private void updateItemList(List<ItemAdapter.itemData> items) {
        displayedItems.clear();
        displayedItems.addAll(items);
        itemAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
}
