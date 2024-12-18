package com.kaaa.talabat_lite;

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
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

public class MerchantsSearchFragment extends Fragment {

    private Vector<merchantView> merchants;
    private Vector<merchantView> allMerchants; // Holds the original list for filtering
    private ListView listMerchants;
    private merchantArrayAdapter adapter;
    private SearchView searchView;
    private Handler uiHandler;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_merchant_search, container, false);

        // Initialize merchants list and UI handler
        merchants = new Vector<>();
        allMerchants = new Vector<>();
        uiHandler = new Handler(Looper.getMainLooper());

        // Initialize UI components
        initializeUI(view);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        // Ensure the lists are cleared before loading fresh data
        merchants.clear();
        //allMerchants.clear();

        // Clear the adapter to reset the ListView
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }

        // Load merchants data from the server
        loadMerchantsFromServer();
    }

    private void initializeUI(View view) {
        // Set up the ListView
        listMerchants = view.findViewById(R.id.merchantList);

        // Set up the SearchView
        searchView = view.findViewById(R.id.search_view);
        searchView.clearFocus();

        // Set the query listener to filter merchants on text change
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

        // Handle ListView item clicks to open the merchant details
        listMerchants.setOnItemClickListener((parent, view1, position, id) -> {
            // Get the selected merchant data
            merchantView selectedMerchant = merchants.get(position);

            // Create an Intent to start the new Activity for viewing merchant details
            Intent intent = new Intent(getActivity(), CustomerViewOfMerchant.class);
            intent.putExtra("merchantName", selectedMerchant.getMerchantName());
            intent.putExtra("merchantKeywords", selectedMerchant.getMerchantKeywords());
            intent.putExtra("merchantRating", selectedMerchant.getMerchantRate());

            // Start the activity
            if (getActivity() != null) {
                startActivity(intent);
            }
        });
    }
    private Bitmap getProfileImg(int id) throws IOException {
        Bitmap temp;
        try {
            // Create URL connection
            URL url = new URL(globals.serverURL + "/get_profile_image/" + id);
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
    private void loadMerchantsFromServer() {
        new Thread(() -> {
            HttpURLConnection conn = null;
            BufferedReader reader = null;
            try {
                // Connect to the server and retrieve merchants data
                URL url = new URL(globals.serverURL + "/getMerchantsSearchResults/empty");
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(15000);

                int responseCode = conn.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    Log.e("MerchantsSearchFragment", "Error retrieving merchant data, Response code: " + responseCode);
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

                Log.d("MerchantsSearchFragment", "Response JSON: " + response);

                // Parse the response JSON
                JSONObject jsonResponse = new JSONObject(response.toString());
                int count = jsonResponse.getInt("count");
                Log.d("MerchantsSearchFragment", "Count of items = " + count);

                if (count == 0) {
                    Log.d("MerchantsSearchFragment", "No merchants found.");
                    uiHandler.post(() -> showToast("No merchants available."));
                    return;
                }

                Vector<merchantView> retrievedMerchants = new Vector<>();
                for (int i = 0; i < count; i++) {
                    String businessName = jsonResponse.getString("businessName" + i);
                    String keywords = jsonResponse.getString("keywords" + i);
                    float rating = (float) jsonResponse.getDouble("rating" + i);
                    int id = jsonResponse.getInt("id" + i);
                    String rate = String.format("%.1f", rating);
                    Bitmap img = getProfileImg(id);
                    Log.d("MerchantsSearchFragment", "Merchant: " + businessName + ", Rating: " + rate);
                    retrievedMerchants.add(new merchantView(businessName, keywords, rate, img));
                }

                // Update UI on the main thread
                uiHandler.post(() -> {
                    allMerchants.clear();   // Clear the original list
                    allMerchants.addAll(retrievedMerchants);  // Add new merchants to original list
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

    // Function to filter the merchants list based on the search input
    private void filterList(String searchWord) {
        Vector<merchantView> filteredMerchants = new Vector<>();
        for (merchantView merchant : allMerchants) {
            if (merchant.getMerchantName().toLowerCase().contains(searchWord.toLowerCase()) ||
                    merchant.getMerchantKeywords().toLowerCase().contains(searchWord.toLowerCase())) {
                filteredMerchants.add(merchant);
            }
        }

        updateMerchantList(filteredMerchants);
    }

    // Function to show Toast messages
    private void showToast(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
}
