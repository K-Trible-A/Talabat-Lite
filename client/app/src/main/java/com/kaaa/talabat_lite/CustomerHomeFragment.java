package com.kaaa.talabat_lite;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class CustomerHomeFragment extends Fragment {
    Button restaurant, pharmacy, grocery,cartView;
    private ListView listMerchants;
    private Vector<merchantView> merchants;
    private merchantArrayAdapter adapter;
    private Handler uiHandler;
    private  ExecutorService executor = Executors.newSingleThreadExecutor();
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (executor.isShutdown()) {
            executor = Executors.newSingleThreadExecutor();
        }
        View view = inflater.inflate(R.layout.fragment_customer_home, container, false);
        merchants = new Vector<>();
        uiHandler = new Handler(Looper.getMainLooper());
        initUi(view);
        setupListeners();
        //loadMerchantsFromServer();
        return view;
    }
    private void initUi(View view) {
        restaurant = view.findViewById(R.id.btnRestaurant);
        grocery = view.findViewById(R.id.btnGrocery);
        pharmacy = view.findViewById(R.id.btnPharmacy);
        cartView = view.findViewById(R.id.btnViewCart);
        listMerchants =  view.findViewById(R.id.merchantList);

        // Handle ListView item clicks to open the merchant details
        listMerchants.setOnItemClickListener((parent, view1, position, id) -> {
            // Get the selected merchant data
            merchantView selectedMerchant = merchants.get(position);

            // Create an Intent to start the new Activity for viewing merchant details
            Intent intent = new Intent(getActivity(), CustomerViewOfMerchant.class);
            intent.putExtra("merchantName", selectedMerchant.getMerchantName());
            intent.putExtra("merchantKeywords", selectedMerchant.getMerchantKeywords());
            intent.putExtra("merchantRating", selectedMerchant.getMerchantRate());
            intent.putExtra("merch_id", selectedMerchant.getMerchantId());

            // Start the activity
            if (getActivity() != null) {
                startActivity(intent);
            }
        });
    }
    public void onResume() {
        super.onResume();

        // Ensure the lists are cleared before loading fresh data
        merchants.clear();

        // Clear the adapter to reset the ListView
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }

        // Load merchants data from the server
        loadMerchantsFromServer();
    }
    private void loadMerchantsFromServer() {
        new Thread(() -> {
            HttpURLConnection conn = null;BufferedReader reader = null;
            try {
                // Connect to the server and retrieve merchants data
                URL url = new URL(globals.serverURL + "/getTopRatedMerchants/" + globals.userId);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(15000);

                int responseCode = conn.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    Log.e("MerchantsHomeFragment", "Error retrieving merchant data, Response code: " + responseCode);
                    uiHandler.post(() -> showToast("Server error: " + responseCode));
                    return;
                }

                // Read response from the server
                reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                Log.d("CustomerHomeFragment", "Response JSON: " + response);

                // Parse the response JSON
                JSONArray merchantsArray = new JSONObject(response.toString()).getJSONArray("merchants");
                Log.d("CustomerHomeFragment", "Count of Merchants = " + merchantsArray.length());
                Vector<merchantView> retrievedMerchants = new Vector<>();
                for (int i = 0; i < merchantsArray.length(); i++) {
                    JSONObject merchant = merchantsArray.getJSONObject(i);
                    String businessName = merchant.getString("businessName");
                    String keywords = merchant.getString("keywords");
                    float rating = (float) merchant.getDouble("rating");
                    String rate = String.format("%.1f", rating);
                    int merchantId = merchant.getInt("merchantId");
                    Bitmap img = getProfileImg(merchantId);
                    Log.d("MerchantsSearchFragment", "Merchant: " + businessName + ", Rating: " + rate);
                    retrievedMerchants.add(new merchantView(businessName, keywords, rate, img, merchantId));
                }

                // Update UI on the main thread
                uiHandler.post(() -> {
                    updateMerchantList(retrievedMerchants);   // Update filtered list
                });
            } catch (IOException e) {
                Log.e("MerchantsSearchFragment", "Failed to read response", e);
                uiHandler.post(() -> showToast("Network error: " + e.getMessage()));
            } catch (JSONException e) {
                Log.e("MerchantsSearchFragment", "JSON parsing error", e);
                uiHandler.post(() -> showToast("Data error: " + e.getMessage()));
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        Log.e("MerchantsSearchFragment", "Error closing reader", e);
                    }
                }
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }).start();
    }
    private Bitmap getProfileImg(int id) throws IOException {
        Bitmap temp;
        try {
            // Create URL connection
            URL url = new URL(globals.serverURL + "/get_profile_image_merchId/" + id);
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
    private void updateMerchantList(Vector<merchantView> retrievedMerchants) {
        merchants.clear();
        merchants.addAll(retrievedMerchants);

        if (adapter == null) {
            adapter = new merchantArrayAdapter(
                    getActivity(),
                    R.layout.merchant_list_view,
                    R.id.merchantName,
                    merchants
            );
            listMerchants.setAdapter(adapter);
            adapter = null;
        } else {
            adapter.notifyDataSetChanged();
            Log.d("MerchantsSearchFragment", "Notifying adapter of data changes.");
        }
    }
    protected void setupListeners()
    {
        restaurant.setOnClickListener(v -> {
            // Handle Restaurant button click
            // For example, navigate to a restaurant list or display a Toast
            Activity activity = requireActivity();
            Intent intent = new Intent(activity, CategorieActivity.class);
            intent.putExtra("businessType", 2); // pass businessType as int
            startActivity(intent);
        });
        pharmacy.setOnClickListener(v -> {
            // Handle Pharmacy  button click
            Activity activity = requireActivity();
            Intent intent = new Intent(activity, CategorieActivity.class);
            intent.putExtra("businessType", 3); // pass businessType as int
            startActivity(intent);
        });

        grocery.setOnClickListener(v -> {
            // Handle Grocary button click
            Activity activity = requireActivity();
            Intent intent = new Intent(activity, CategorieActivity.class);
            intent.putExtra("businessType", 1); // pass businessType as int
            startActivity(intent);
        });
        cartView.setOnClickListener(v -> {
            Activity activity = requireActivity();
            Intent intent = new Intent(activity, CartActivity.class);
            startActivity(intent);
            // Handle ViewCart button click
        });
    }
    private void showToast(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
}
