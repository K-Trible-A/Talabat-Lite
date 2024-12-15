package com.kaaa.talabat_lite;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

public class MerchantsSearchFragment extends Fragment {

    private Vector<merchantView> merchants;
    private Vector<merchantView> allMerchants; // This will hold the original list of merchants
    private ListView listMerchants;
    private merchantArrayAdapter adapter;
    private SearchView searchView;
    private Handler uiHandler;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_merchant_search, container, false);

        // Initialize merchants list and UI handler
        merchants = new Vector<>();
        allMerchants = new Vector<>(); // Initialize the original list
        uiHandler = new Handler(Looper.getMainLooper());

        // Initialize UI components
        initializeUI(view);

        loadMerchantsFromServer();
        // Load merchants from the server
        return view;
    }

    private void initializeUI(View view) {
        // Set up the ListView
        listMerchants = view.findViewById(R.id.merchantList);

        // Set up the SearchView
        searchView = view.findViewById(R.id.search_view);
        searchView.clearFocus();

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

        // Handle ListView item clicks
        listMerchants.setOnItemClickListener((parent, view1, position, id) -> {
            // Get the selected merchant data
            merchantView selectedMerchant = merchants.get(position);

            // Create an Intent to start the new Activity
            Intent intent = new Intent(getActivity(), CustomerViewOfMerchant.class);

            // Pass the merchant data to the new activity
            intent.putExtra("merchantName", selectedMerchant.getMerchantName());
            intent.putExtra("merchantKeywords", selectedMerchant.getMerchantKeywords());
            intent.putExtra("merchantRating", selectedMerchant.getMerchantRate());
            // Start the activity
            startActivity(intent);
        });
    }


    private void loadMerchantsFromServer() {
        new Thread(() -> {
            HttpURLConnection conn = null;
            BufferedReader reader = null;
            try {
                // Connect to the server
                URL url = new URL(globals.serverURL + "/getMerchantsSearchResults/" + "empty");
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(15000);

                int responseCode = conn.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    Log.e("MerchantsSearchFragment", "Error retrieving merchant data, Response code: " + responseCode);
                    uiHandler.post(() -> Toast.makeText(getContext(), "Server error: " + responseCode, Toast.LENGTH_SHORT).show());
                    return;
                }

                // Read response from the server
                reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                // Parse the response JSON
                JSONObject jsonResponse = new JSONObject(response.toString());
                int count = jsonResponse.getInt("count");
                Vector<merchantView> retrievedMerchants = new Vector<>();
                for (int i = 0; i < count; i++) {
                    String businessName = jsonResponse.getString("businessName" + i);
                    String keywords = jsonResponse.getString("keywords" + i);
                    float rating = (float) jsonResponse.getDouble("rating" + i);
                    String rate = String.format("%.1f", rating);
                    retrievedMerchants.add(new merchantView(businessName, keywords, rate, R.drawable.profile_icon));
                }

                // Update UI on the main thread
                uiHandler.post(() -> {
                    // Store the full merchant list
                    allMerchants.clear();
                    allMerchants.addAll(retrievedMerchants);

                    // Update the displayed list
                    updateMerchantList(retrievedMerchants);
                });

            } catch (IOException e) {
                Log.e("MerchantsSearchFragment", "Failed to read response", e);
                uiHandler.post(() -> Toast.makeText(getContext(), "Network error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            } catch (JSONException e) {
                Log.e("MerchantsSearchFragment", "JSON parsing error", e);
                uiHandler.post(() -> Toast.makeText(getContext(), "Data error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            } finally {
                // Clean up resources
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

    private void updateMerchantList(Vector<merchantView> retrievedMerchants) {
        // Update the merchants list and notify the adapter
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
        } else {
            adapter.notifyDataSetChanged();
        }
    }

    private void filterList(String searchWord) {
        new Thread(() -> {
            HttpURLConnection conn = null;
            BufferedReader reader = null;

            try {
                // Construct the server URL with the search query
                URL url = new URL(globals.serverURL + "/getMerchantsSearchResults/" + searchWord);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(15000);

                int responseCode = conn.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    Log.e("MerchantsSearchFragment", "Error retrieving filtered data, Response code: " + responseCode);
                    uiHandler.post(() -> Toast.makeText(getContext(), "Server error: " + responseCode, Toast.LENGTH_SHORT).show());
                    return;
                }

                // Read response from the server
                reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                // Parse the response JSON
                JSONObject jsonResponse = new JSONObject(response.toString());
                int count = jsonResponse.getInt("count");
                Vector<merchantView> filteredMerchants = new Vector<>();
                for (int i = 0; i < count; i++) {
                    String businessName = jsonResponse.getString("businessName" + i);
                    String keywords = jsonResponse.getString("keywords" + i);
                    float rating = (float) jsonResponse.getDouble("rating" + i);
                    String rate = String.format("%.1f", rating);
                    filteredMerchants.add(new merchantView(businessName, keywords, rate, R.drawable.profile_icon));
                }

                // Update UI on the main thread
                uiHandler.post(() -> updateMerchantList(filteredMerchants));

            } catch (IOException e) {
                Log.e("MerchantsSearchFragment", "Failed to read response", e);
                uiHandler.post(() -> Toast.makeText(getContext(), "Network error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            } catch (JSONException e) {
                Log.e("MerchantsSearchFragment", "JSON parsing error", e);
                uiHandler.post(() -> Toast.makeText(getContext(), "Data error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            } finally {
                // Clean up resources
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

}
