package com.kaaa.talabat_lite;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CourierProfileFragment extends Fragment {
    private EditText name,email,vehicleType,nationalId,phoneNumber,country,city;
    private String courierName,courierEmail,courierVehicleType,courierNationalId,courierPhoneNumber,courierCountry,courierCity;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requireActivity().getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Navigate to the Home fragment
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.container, new CourierOrdersFragment())
                        .commit();
                BottomNavigationView bottomNavigationView = requireActivity().findViewById(R.id.bottom_navigation);
                if (bottomNavigationView != null)
                {
                    bottomNavigationView.setSelectedItemId(R.id.profile);
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_courier_profile, container, false);
        initUI(rootView);
        fetchCourierData();  // Start fetching merchant data when fragment is loaded
        return rootView;
    }

    protected void initUI(View rootView) {
        name = rootView.findViewById(R.id.name);
        email = rootView.findViewById(R.id.email);
        vehicleType = rootView.findViewById(R.id.vehicleType);
        nationalId = rootView.findViewById(R.id.nationalId);
        phoneNumber = rootView.findViewById(R.id.phoneNumber);
        country = rootView.findViewById(R.id.country);
        city = rootView.findViewById(R.id.city);
    }

    private void getCourierData() {
        try {
            // Create URL connection
            URL url = new URL(globals.serverURL + "/getCourierData/" + globals.userId);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            int responseCode = conn.getResponseCode();
            // Check the response code
            if (responseCode != HttpURLConnection.HTTP_OK) {
                Log.e("CourierProfileFragment", "Error retrieving courier data");
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
            courierName = jsonResponse.getString("name");
            courierEmail = jsonResponse.getString("email");
            String type = jsonResponse.getString("vehicleType");
            if(type.equals("1"))
                courierVehicleType = "Car";
            else if(type.equals("2"))
                courierVehicleType = "Motorcycle";
            else
                courierVehicleType = "Bicycle";
            courierNationalId = jsonResponse.getString("nationalId");
            courierPhoneNumber = jsonResponse.getString("phoneNumber");
            courierCountry = jsonResponse.getString("country");
            courierCity = jsonResponse.getString("city");
            mainHandler.post(this::updateUI);
        }catch (IOException e) {
            Log.e("MerchantProfileFragment", "Failed to read response");
        } catch (JSONException e) {
            Log.e("MerchantProfileFragment", "Json error");
        }
    }
    private void fetchCourierData() {
        // Execute data fetch in background
        executor.execute(this::getCourierData);
    }

    private void updateUI() {
        if (isAdded()) {
            name.setText(courierName);
            email.setText(courierEmail);
            vehicleType.setText(courierVehicleType);
            nationalId.setText(courierNationalId);
            phoneNumber.setText(courierPhoneNumber);
            country.setText(courierCountry);
            city.setText(courierCity);
        }
    }
}
