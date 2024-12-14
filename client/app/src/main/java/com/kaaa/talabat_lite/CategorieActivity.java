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

import java.io.IOException;
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
        RecyclerView recyclerView = findViewById(R.id.MerchantRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getBaseContext()));
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
                socketHelper.getInstance().connect();
                socketHelper.getInstance().sendInt(globals.GET_CATEGORIE);
                socketHelper.getInstance().sendInt(businessType);
                int merchCount = socketHelper.getInstance().recvInt();

                List<MerchantAdapter.MerchantData> tempMerchList = new ArrayList<>();

                for (int i = 0; i < merchCount; i++) {
                    int merchId = socketHelper.getInstance().recvInt();
                    String merchName = socketHelper.getInstance().recvString();
                    float merchRate = socketHelper.getInstance().recvFloat();
                    tempMerchList.add(new MerchantAdapter.MerchantData(merchId, merchName, merchRate));
                }
                socketHelper.getInstance().close();

                // Update the main itemList and notify the adapter in the UI thread
                mainHandler.post(() -> {
                        merchList.clear();
                        merchList.addAll(tempMerchList);
                        merchAdapter.notifyDataSetChanged();
                });
            } catch (IOException e) {
                Log.e("MerchantHomeFragment", "Error loading items from server", e);
            }
        });
    }
}