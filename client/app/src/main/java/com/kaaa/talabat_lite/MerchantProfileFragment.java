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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MerchantProfileFragment extends Fragment {

    private EditText profileBusinessType, profileKeywords, profilePickupAddress, profileRating;
    private TextView profileBusinessName;
    private ImageView profilePicture;
    private Button changeAddressButton, editProfilePictureButton;
    private String businessName, type, keywords, pickupAddress;
    float rating;
    private Bitmap profileImg;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private ActivityResultLauncher<Intent> changeAddressLauncher;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requireActivity().getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Navigate to the Home fragment
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.container, new MerchantHomeFragment()) // Replace with your Home fragment class
                        .commit();
                BottomNavigationView bottomNavigationView = requireActivity().findViewById(R.id.bottom_navigation);
                if (bottomNavigationView != null)
                {
                    bottomNavigationView.setSelectedItemId(R.id.home);
                }
            }
        });

        changeAddressLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        String newPickupAddress = result.getData().getStringExtra("updatedPickupAddress");
                        if (newPickupAddress != null) {
                            pickupAddress = newPickupAddress;
                            if (profilePickupAddress != null) {
                                profilePickupAddress.setText(newPickupAddress);
                            }
                        }
                    }
                }
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_merchant_profile, container, false);
        initUI(rootView);
        setupListeners();
        fetchMerchantData();  // Start fetching merchant data when fragment is loaded
        return rootView;
    }

    protected void initUI(View rootView) {
        profileBusinessName = rootView.findViewById(R.id.profileBusinessName);
        profileBusinessType = rootView.findViewById(R.id.profileBusinessType);
        profileKeywords = rootView.findViewById(R.id.profileKeywords);
        profilePickupAddress = rootView.findViewById(R.id.profilePickupAddress);
        changeAddressButton = rootView.findViewById(R.id.changeAddressButton);
        editProfilePictureButton = rootView.findViewById(R.id.editProfilePictureButton);
        profileRating = rootView.findViewById(R.id.profileRating);
        profilePicture = rootView.findViewById(R.id.profile_icon);

    }

    protected void setupListeners() {
        changeAddressButton.setOnClickListener(view -> changePickupAddress());
        editProfilePictureButton.setOnClickListener(view -> editProfilePicture());

    }

    private void changePickupAddress() {
        if (pickupAddress == null) {
            pickupAddress = "";  // Prevent null address from causing issues
        }
        Intent changePickupAddressIntent = new Intent(requireContext(), ChangePickupAddressActivity.class);
        changePickupAddressIntent.putExtra("currentAddress", pickupAddress);
        changeAddressLauncher.launch(changePickupAddressIntent);
    }
    private void editProfilePicture ()
    {
        Intent editProfilePictureIntent = new Intent(requireContext(),AddProfilePicActivity.class);
        startActivity(editProfilePictureIntent);
    }
    private Bitmap getProfileImg() throws IOException {
        Bitmap temp;
        try {
            // Create URL connection
            URL url = new URL(globals.serverURL + "/get_profile_image/" + globals.userId);
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
    private void getMerchantData() {
        try {
            profileImg = getProfileImg();
            // Create URL connection
            URL url = new URL(globals.serverURL + "/getMerchantData/" + globals.userId);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            int responseCode = conn.getResponseCode();
            // Check the response code
            if (responseCode != HttpURLConnection.HTTP_OK) {
                Log.e("MerchantProfileFragment", "Error retrieving merchant data");
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
            businessName = jsonResponse.getString("businessName");
            keywords = jsonResponse.getString("keywords");
            rating = (float) jsonResponse.getDouble("rating");
            type = jsonResponse.getString("type");
            pickupAddress = jsonResponse.getString("pickupAddress");
            mainHandler.post(this::updateUI);
        }catch (IOException e) {
            Log.e("MerchantProfileFragment", "Failed to read response");
        } catch (JSONException e) {
            Log.e("MerchantProfileFragment", "Json error");
        }
    }
    private void fetchMerchantData() {
        // Execute data fetch in background
        executor.execute(this::getMerchantData);
    }

    private void updateUI() {
        if (isAdded()) {
            profileBusinessName.setText(businessName != null ? businessName : "No name set");
            profileBusinessType.setText(type != null ? type : "No type set");
            profileKeywords.setText(keywords != null ? keywords : "No keywords set");
            profilePickupAddress.setText(pickupAddress != null ? pickupAddress : "No address set");
            profileRating.setText(String.valueOf(rating));
            if (profileImg != null) {
                profilePicture.setImageBitmap(profileImg);
            }
        }
    }
}
