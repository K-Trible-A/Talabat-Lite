package com.kaaa.talabat_lite;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MerchantHomeFragment extends Fragment {

    private ItemAdapter itemAdapter;
    private List<ItemAdapter.itemData> itemList;
    private ExecutorService executor;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private TextView merchantName, merchantRating, merchantKeywords;
    private String merchantNameStr, merchantKeywordsStr;
    private Button addItemButton;
    private float rating;

    // Register ActivityResultLauncher for AddItemActivity
    private final ActivityResultLauncher<Intent> addItemLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == AppCompatActivity.RESULT_OK) {
                    // Success, refresh the item list
                    loadItemsFromServer();
                    Toast.makeText(getContext(), "Item added successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Failed to add item", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        executor = Executors.newSingleThreadExecutor(); // Initialize executor here
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_merchant_home, container, false);

        // Initialize RecyclerView and adapter
        RecyclerView recyclerView = view.findViewById(R.id.itemRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        itemList = new ArrayList<>();
        itemAdapter = new ItemAdapter(getContext(), itemList);
        recyclerView.setAdapter(itemAdapter);

        // Initialize UI elements
        initUI(view);

        // Fetch merchant info and items
        fetchMerchantInfo();

        // Setup listeners
        setupListeners();

        return view;
    }

    private void fetchMerchantInfo() {
        executor.execute(() -> {
            getMerchantInfo();
            mainHandler.post(this::updateUI);
        });
    }

    private void getMerchantInfo() {
        try {
            Log.d("MerchantHomeFragment", "Before calling the server");
            socketHelper.getInstance().connect();
            socketHelper.getInstance().sendInt(globals.GET_MERCHANT_HOME_INFO);
            socketHelper.getInstance().sendInt(globals.userId);

            merchantNameStr = socketHelper.getInstance().recvString();
            merchantKeywordsStr = socketHelper.getInstance().recvString();
            rating = socketHelper.getInstance().recvFloat();
            socketHelper.getInstance().close();

            loadItemsFromServer();
            Log.d("MerchantHomeFragment", "Data fetched successfully");

        } catch (IOException e) {
            Log.e("MerchantHomeFragment", "Error fetching merchant info", e);
            showToast("Error fetching merchant info");
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadItemsFromServer() {
        new Thread(() -> {
            try {
                socketHelper.getInstance().connect();
                socketHelper.getInstance().sendInt(globals.GET_ITEMS);
                socketHelper.getInstance().sendInt(globals.userId);
                int itemCount = socketHelper.getInstance().recvInt();
                Log.d("MerchantHomeFragment", "Received " + itemCount + " items");

                List<ItemAdapter.itemData> tempItemList = new ArrayList<>();

                for (int i = 0; i < itemCount; i++) {
                    int itemId = socketHelper.getInstance().recvInt();
                    String itemName = socketHelper.getInstance().recvString();
                    float itemPrice = socketHelper.getInstance().recvFloat();
                    String itemDescription = socketHelper.getInstance().recvString();
                    Bitmap img = socketHelper.getInstance().recvImg();

                    tempItemList.add(new ItemAdapter.itemData(itemId, itemName, itemDescription, itemPrice, img));
                }

                // Update UI with new items on the main thread
                mainHandler.post(() -> {
                    itemList.clear();
                    itemList.addAll(tempItemList);
                    itemAdapter.notifyDataSetChanged();
                });

            } catch (IOException e) {
                Log.e("MerchantHomeFragment", "Error loading items from server", e);
                showToast("Error loading items from server");
            }
        }).start();
    }

    protected void initUI(View view) {
        merchantName = view.findViewById(R.id.merchantName);
        merchantKeywords = view.findViewById(R.id.merchantKeywords);
        merchantRating = view.findViewById(R.id.merchantRating);
        addItemButton = view.findViewById(R.id.addItemButton);
    }

    protected void setupListeners() {
        addItemButton.setOnClickListener(view -> {
            Intent addItemIntent = new Intent(requireContext(), AddItemActivity.class);
            addItemLauncher.launch(addItemIntent);  // Launch activity with result callback
        });
    }

    @SuppressLint("DefaultLocale")
    private void updateUI() {
        merchantName.setText(merchantNameStr);
        merchantRating.setText(String.format("Rating: %.1f", rating));
        merchantKeywords.setText(merchantKeywordsStr);
    }

    private void showToast(String message) {
        mainHandler.post(() -> Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        try {
            socketHelper.getInstance().close();
        } catch (IOException e) {
            Log.e("MerchantHomeFragment", "Error closing socket", e);
        }

        if (!executor.isShutdown()) {
            executor.shutdownNow();
        }
    }
}
