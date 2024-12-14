package com.kaaa.talabat_lite;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CustomerProfileFragment extends Fragment {

    private EditText Name, Password, Country, City, Address;

    TextView tvCurrentName ,tvCurrentCountry ,tvCurrentPassword ,tvCurrentCity ,tvCurrentDeliveryAddress ;

    private Button btnPaymentMethod, btnSave;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_customer_profile, container, false);

        // Initialize Views
        initUi(view);
        getdataofcustomer();


        // Save Profile Data
        btnSave.setOnClickListener(v -> saveProfileData());

        btnPaymentMethod.setOnClickListener(v -> {
            Activity activity = requireActivity();
            Intent intent = new Intent(activity, CardActivity.class);
            startActivity(intent);
        });
        return view;
    }
    @SuppressLint("SetTextI18n")
    private void initUi(View view)
    {
        Name = view.findViewById(R.id.etName);
        Password = view.findViewById(R.id.etPassword);
        Country = view.findViewById(R.id.etCountry);
        City = view.findViewById(R.id.etCity);
        Address = view.findViewById(R.id.etAddress);
        btnPaymentMethod = view.findViewById(R.id.btnSetPaymentMethod);
        btnSave = view.findViewById(R.id.btnSave);
        tvCurrentName = view.findViewById(R.id.tvCurrentName);
        tvCurrentCountry = view.findViewById(R.id.tvCurrentCountry);
        tvCurrentPassword = view.findViewById(R.id.tvCurrentPassword);
        tvCurrentCity = view.findViewById(R.id.tvCurrentCity);
        tvCurrentDeliveryAddress = view.findViewById(R.id.tvCurrentDeliveryAddress);
    }
    private void getdataofcustomer()
    {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                socketHelper.getInstance().connect();
                socketHelper.getInstance().sendInt(globals.GET_CUSTOMER_DATA);
                socketHelper.getInstance().sendInt(globals.userId);
                String currentName = socketHelper.getInstance().recvString();
                String currentPassword = socketHelper.getInstance().recvString();
                String currentCountry = socketHelper.getInstance().recvString();
                String currentCity = socketHelper.getInstance().recvString();
                String currentDeliveryAddress = socketHelper.getInstance().recvString();
                int ok = socketHelper.getInstance().recvInt();
                socketHelper.getInstance().close();
                if (ok == 1) {
                    tvCurrentName.setText(MessageFormat.format("Current Name: {0}", currentName));
                    tvCurrentCountry.setText(MessageFormat.format("Current Country: {0}", currentCountry));
                    tvCurrentPassword.setText(MessageFormat.format("Current Password: {0}", currentPassword));
                    tvCurrentCity.setText(MessageFormat.format("Current City: {0}", currentCity));
                    tvCurrentDeliveryAddress.setText(MessageFormat.format("Current Delivery Address: {0}", currentDeliveryAddress));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
    private void saveProfileData() {
        String name = Name.getText().toString().trim();
        String password = Password.getText().toString().trim();
        String country = Country.getText().toString().trim();
        String city = City.getText().toString().trim();
        String address = Address.getText().toString().trim();

        if (name.isEmpty() && password.isEmpty() && country.isEmpty() && city.isEmpty() && address.isEmpty()) {
            Toast.makeText(getContext(), "Please fill any of the fields", Toast.LENGTH_SHORT).show();
            return;
        }
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                socketHelper.getInstance().connect();
                socketHelper.getInstance().sendInt(globals.EDIT_CUSTOMER_DATA);
                socketHelper.getInstance().sendInt(globals.userId);

                socketHelper.getInstance().sendString(name);
                socketHelper.getInstance().sendString(password);
                socketHelper.getInstance().sendString(country);
                socketHelper.getInstance().sendString(city);
                socketHelper.getInstance().sendString(address);

                int ok = socketHelper.getInstance().recvInt();
                socketHelper.getInstance().close();

                getActivity().runOnUiThread(() -> {
                    if (ok == 1) {
                        if (!name.isEmpty())
                            tvCurrentName.setText(MessageFormat.format("Current Name: {0}", name));
                        if (!country.isEmpty())
                            tvCurrentCountry.setText(MessageFormat.format("Current Country: {0}", country));
                        if (!password.isEmpty())
                            tvCurrentPassword.setText(MessageFormat.format("Current Password: {0}", password));
                        if (!city.isEmpty())
                            tvCurrentCity.setText(MessageFormat.format("Current City: {0}", city));
                        if (!address.isEmpty())
                            tvCurrentDeliveryAddress.setText(MessageFormat.format("Current Delivery Address: {0}", address));

                        Toast.makeText(getContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Failed to update profile", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (IOException e) {
                getActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Error saving profile: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }
        });
        // Save the data (to a database, server, or shared preferences)
        Toast.makeText(getContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
    }
}