package com.kaaa.talabat_lite;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CategorieActivity extends AppCompatActivity {
    private MerchantAdapter merchAdapter;
    private List<MerchantAdapter.MerchantData> merchList;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_categorie);
        if (executor.isShutdown()) {
            executor = Executors.newSingleThreadExecutor();
        }
        RecyclerView recyclerView = findViewById(R.id.CategorieRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        merchList = new ArrayList<>();
        merchAdapter = new MerchantAdapter(getBaseContext(), merchList);
        recyclerView.setAdapter(merchAdapter);
        fetchCategorie();
    }
    private void fetchCategorie() {
        if (executor.isShutdown()) {
            executor = Executors.newSingleThreadExecutor(); // Recreate executor if it was shut down
        }

        executor.execute(() -> {
            // Load items only after merchant info is fetched
            mainHandler.post(this::loadItemsFromServer);
            });
    }
    @SuppressLint("NotifyDataSetChanged")
    private void loadItemsFromServer() {
        if (executor.isShutdown()) {
            executor = Executors.newSingleThreadExecutor(); // Recreate the executor if it's shut down
        }
        executor.execute(() -> {
            try {
                Intent intent = getIntent();
                int businessType=intent.getIntExtra("businessType",0);
                // Create URL connection
                URL url = new URL(globals.serverURL + "/customer/getCategory/" + businessType);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                int responseCode = conn.getResponseCode();
                // Check the response code
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    Log.e("CategoryActivity", "Error retrieving merchant data");
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
                int merchCount = jsonResponse.getInt("merchantCount");
                List<MerchantAdapter.MerchantData> tempMerchList = new ArrayList<>();
                JSONArray itemsArray = jsonResponse.getJSONArray("merchants");
                for (int i = 0; i < merchCount; i++) {
                    JSONObject merchantJson = itemsArray.getJSONObject(i);
                    int merchId = merchantJson.getInt("merchId");
                    String merchName = merchantJson.getString("merchName");
                    float merchRate = (float) merchantJson.getDouble("merchRate");
                    tempMerchList.add(new MerchantAdapter.MerchantData(merchId, merchName, merchRate));
                }
                conn.disconnect();

                // Update the main itemList and notify the adapter in the UI thread
                mainHandler.post(() -> {
                        merchList.clear();
                        merchList.addAll(tempMerchList);
                        merchAdapter.notifyDataSetChanged();
                });
            } catch (JSONException e) {
                Log.e("MerchantHomeFragment", "Error loading items from server", e);
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}