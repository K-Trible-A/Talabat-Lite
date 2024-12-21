package com.kaaa.talabat_lite;

import static androidx.fragment.app.FragmentManager.TAG;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CustomerProfileFragment extends Fragment {
    private EditText City, Address;
    private TextView tvCurrentCity, tvCurrentDeliveryAddress;
    private Button btnPaymentMethod, btnSave, btnChangePicture, logOut;
    private ImageView imgCustomerPicture;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_customer_profile, container, false);
        // Initialize Views
        initUi(view);
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Navigate to the Home fragment
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.container, new TopRated_Merchants_Fragment()) // Replace with your Home fragment class
                        .commit();
                BottomNavigationView bottomNavigationView = requireActivity().findViewById(R.id.bottom_navigation);
                if (bottomNavigationView != null)
                {
                    bottomNavigationView.setSelectedItemId(R.id.home);
                }
            }
        });
        getdataofcustomer();
        // Save Profile Data
        btnSave.setOnClickListener(v -> saveProfileData());
        btnPaymentMethod.setOnClickListener(v -> {
            Activity activity = requireActivity();
            Intent intent = new Intent(activity, CustomerAddCardActivty.class);
            startActivity(intent);
        });
        btnChangePicture.setOnClickListener(v -> {
            Activity activity = requireActivity();
            Intent intent = new Intent(activity, AddCustomerImageActivity.class);
            startActivity(intent);
        });
        logOut.setOnClickListener(v->{
            Intent logoutIntent = new Intent(requireContext(), LoginActivity.class);
            logoutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(logoutIntent);
            requireActivity().finish();
        });
        return view;
    }

    @SuppressLint("SetTextI18n")
    private void initUi(View view) {
        City = view.findViewById(R.id.etCity);
        Address = view.findViewById(R.id.etAddress);
        btnPaymentMethod = view.findViewById(R.id.btnSetPaymentMethod);
        btnSave = view.findViewById(R.id.btnSave);
        tvCurrentCity = view.findViewById(R.id.tvCurrentCity);
        tvCurrentDeliveryAddress = view.findViewById(R.id.tvCurrentDeliveryAddress);
        imgCustomerPicture = view.findViewById(R.id.imgCustomerPicture);
        btnChangePicture = view.findViewById(R.id.btnChangePicture);
        logOut = view.findViewById(R.id.customer_logout);
    }

    private Bitmap getCustomerImage() {
        Bitmap temp;
        try {
            // Create URL connection
            URL url = new URL(globals.serverURL + "/customer/getImage/" + globals.userId);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setDoInput(true);

            int responseCode = conn.getResponseCode();
            // Check the response code
            if (responseCode != HttpURLConnection.HTTP_OK) {
                Log.i("CustomerProfileFragment", "No Customer image");
                return null;
            }
            // Read the response
            InputStream inputStream = conn.getInputStream();
            temp = BitmapFactory.decodeStream(inputStream);
            inputStream.close();
        } catch (IOException e) {
            Log.i("CustomerProfileFragment", "Error retrieving customer image");
            return null;
        }
        return temp;
    }

    @SuppressLint("RestrictedApi")
    private void getdataofcustomer() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                // Create URL connection
                URL url = new URL(globals.serverURL + "/customer/getData/" + globals.userId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                int responseCode = conn.getResponseCode();
                // Check the response code
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    Log.e("CustomerProfileFragment", "Error retrieving customer data");
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
                String currentCity = jsonResponse.getString("city");
                String currentDeliveryAddress = jsonResponse.getString("customerAddress");

                // Update the TextViews on the main thread
                requireActivity().runOnUiThread(() -> {
                    try {
                        if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                            tvCurrentCity.setText(MessageFormat.format("Current City: {0}", currentCity));
                            tvCurrentDeliveryAddress.setText(MessageFormat.format("Current Delivery Address: {0}", currentDeliveryAddress));
                        } else {
                            Log.e("CustomerProfileFragment", conn.getResponseMessage());
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            } catch (IOException e) {
                Log.e("CustomerProfileFragment", "Failed to read response");
            } catch (JSONException e) {
                Log.e("CustomerProfileFragment", "Json error");
            }

            // Update the customer image on the main thread
            Bitmap customerImage = getCustomerImage();
            if (customerImage != null) {
                requireActivity().runOnUiThread(() -> imgCustomerPicture.setImageBitmap(customerImage));
            } else {
                Log.e(TAG, "Error: Customer image is null.");
            }
        });
    }

    private void saveProfileData() {
        String city = City.getText().toString().trim();
        String address = Address.getText().toString().trim();

        if (city.isEmpty() && address.isEmpty()) {
            Toast.makeText(getContext(), "Please fill any of the fields", Toast.LENGTH_SHORT).show();
            return;
        }
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                // Prepare the URL for the endpoint
                URL server = new URL(globals.serverURL + "/customer/setData/" + globals.userId);
                // Create the JSON payload
                JSONObject jsonPayload = new JSONObject();
                jsonPayload.put("address", address);
                jsonPayload.put("city", city);

                // Open a connection to the server
                HttpURLConnection conn = (HttpURLConnection) server.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true); // To send a body
                // Send the request
                OutputStream os = conn.getOutputStream();
                os.write(jsonPayload.toString().getBytes(StandardCharsets.UTF_8));
                os.flush();
                os.close();
                // Get the response code
                int responseCode = conn.getResponseCode();

                requireActivity().runOnUiThread(() -> {
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        if (!city.isEmpty())
                            tvCurrentCity.setText(MessageFormat.format("Current City: {0}", city));
                        if (!address.isEmpty())
                            tvCurrentDeliveryAddress.setText(MessageFormat.format("Current Delivery Address: {0}", address));
                        Toast.makeText(getContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Failed to update profile", Toast.LENGTH_SHORT).show();
                    }
                });
                conn.disconnect();
            } catch (IOException e) {
                Log.e("CustomerProfileFragment", "Error: Edit customer data");
            } catch (JSONException e) {
                Log.e("CustomerProfileFragment", "Json error");
            }
        });
    }
}
