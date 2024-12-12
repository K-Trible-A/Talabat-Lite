package com.kaaa.talabat_lite;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
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
    float rating;
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
        try
        {
            socketHelper.getInstance().connect();
            socketHelper.getInstance().sendInt(globals.GET_MERCHANT_HOME_INFO);
            socketHelper.getInstance().sendInt(globals.userId);
            merchantNameStr = socketHelper.getInstance().recvString();
            merchantKeywordsStr = socketHelper.getInstance().recvString();
            rating = socketHelper.getInstance().recvFloat();
            socketHelper.getInstance().close();

        } catch (IOException e) {

        }
    }
    @SuppressLint("NotifyDataSetChanged")
    private void loadItemsFromServer() {
        if (executor.isShutdown()) {
            executor = Executors.newSingleThreadExecutor(); // Recreate the executor if it's shut down
        }

        executor.execute(() -> {
            try {
                socketHelper.getInstance().connect();
                socketHelper.getInstance().sendInt(globals.GET_ITEMS);
                socketHelper.getInstance().sendInt(globals.userId);
                int itemCount = socketHelper.getInstance().recvInt();

                List<ItemAdapter.itemData> tempItemList = new ArrayList<>();

                for (int i = 0; i < itemCount; i++) {
                    int itemId = socketHelper.getInstance().recvInt();
                    String itemName = socketHelper.getInstance().recvString();
                    float itemPrice = socketHelper.getInstance().recvFloat();
                    String itemDescription = socketHelper.getInstance().recvString();
                    Bitmap img = socketHelper.getInstance().recvImg();

                    tempItemList.add(new ItemAdapter.itemData(itemId, itemName, itemDescription, itemPrice, img));
                }
                socketHelper.getInstance().close();

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
