package com.kaaa.talabat_lite;

import static com.kaaa.talabat_lite.globals.GET_MERCHANT_DATA;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MerchantProfileFragment extends Fragment {

    EditText profileBusinessType, profileKeywords, profilePickupAddress, profileRating;
    TextView profileBusinessName;
    Button changeAddressButton;
    String businessName, type, keywords, pickupAddress;
    float rating;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    // Register ActivityResultLauncher to receive the updated pickup address from ChangePickupAddressActivity
    private final ActivityResultLauncher<Intent> changeAddressLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    String newPickupAddress = result.getData().getStringExtra("updatedPickupAddress");
                    if (newPickupAddress != null) {
                        pickupAddress = newPickupAddress;
                        profilePickupAddress.setText(newPickupAddress);  // Refresh the address in the UI
                    }
                }
            });

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
        profileRating = rootView.findViewById(R.id.profileRating);
    }

    protected void setupListeners() {
        changeAddressButton.setOnClickListener(view -> changePickupAddress());
    }

    private void changePickupAddress() {
        if (pickupAddress == null) {
            pickupAddress = "";  // Prevent null address from causing issues
        }
        // Launch ChangePickupAddressActivity for result
        Intent changePickupAddressIntent = new Intent(requireContext(), ChangePickupAddressActivity.class);
        changePickupAddressIntent.putExtra("currentAddress", pickupAddress);
        changeAddressLauncher.launch(changePickupAddressIntent);
    }

    private void getMerchantData() {
        try {
            socketHelper.getInstance().connect();
            socketHelper.getInstance().sendInt(GET_MERCHANT_DATA);
            socketHelper.getInstance().sendInt(globals.userId);
            businessName = socketHelper.getInstance().recvString();
            type = socketHelper.getInstance().recvString();
            keywords = socketHelper.getInstance().recvString();
            pickupAddress = socketHelper.getInstance().recvString(); // Fetch pickup address
            rating = socketHelper.getInstance().recvFloat();
            socketHelper.getInstance().close();
        } catch (IOException e) {
            requireActivity().runOnUiThread(() ->
                    Toast.makeText(requireContext(), "Error fetching data, please try again.", Toast.LENGTH_SHORT).show());
        }
    }

    private void fetchMerchantData() {
        executor.execute(() -> {
            getMerchantData();  // Execute data fetch in background
            requireActivity().runOnUiThread(this::updateUI);
        });
    }

    private void updateUI() {
        profileBusinessName.setText(businessName);
        profileBusinessType.setText(type);
        profileKeywords.setText(keywords);
        profilePickupAddress.setText(pickupAddress != null ? pickupAddress : "No address set");
        profileRating.setText(String.valueOf(rating));
    }
}
