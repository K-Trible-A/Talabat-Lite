package com.kaaa.talabat_lite;

import static com.kaaa.talabat_lite.globals.CHANGE_PICKUP_ADDRESS;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

public class ChangePickupAddressActivity extends AppCompatActivity {

    String pickupAddress;
    EditText pickupAddressField;
    Button submitAddressButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_pickup_address);
        initUI();
        setupListeners();
    }

    protected void initUI() {
        pickupAddressField = findViewById(R.id.pickupAddress);
        pickupAddress = getIntent().getStringExtra("currentAddress");
        pickupAddressField.setText(pickupAddress);
        submitAddressButton = findViewById(R.id.submitAddressButton);
    }

    protected void setupListeners() {
        submitAddressButton.setOnClickListener(view -> new Thread(this::changePickupAddress).start());
    }

    private void changePickupAddress() {
        pickupAddress = pickupAddressField.getText().toString().trim();
        if (pickupAddress.isEmpty()) {
            runOnUiThread(() ->
                    Toast.makeText(ChangePickupAddressActivity.this, "Pickup address field is empty!", Toast.LENGTH_SHORT).show());
            return;
        }

        try {
            socketHelper.getInstance().connect();
            socketHelper.getInstance().sendInt(CHANGE_PICKUP_ADDRESS);
            socketHelper.getInstance().sendInt(globals.userId);
            socketHelper.getInstance().sendString(pickupAddress);
            int ok = socketHelper.getInstance().recvInt();
            socketHelper.getInstance().close();

            if (ok == 1) {
                runOnUiThread(() ->
                        Toast.makeText(ChangePickupAddressActivity.this, "Address changed successfully!", Toast.LENGTH_SHORT).show());
            } else {
                runOnUiThread(() ->
                        Toast.makeText(ChangePickupAddressActivity.this, "Failed to change address.", Toast.LENGTH_SHORT).show());
            }

            // Return the updated pickup address back to MerchantProfileFragment
            Intent resultIntent = new Intent();
            resultIntent.putExtra("updatedPickupAddress", pickupAddress);
            setResult(Activity.RESULT_OK, resultIntent);
            finish();
        } catch (IOException e) {
            runOnUiThread(() ->
                    Toast.makeText(ChangePickupAddressActivity.this, "Error updating address.", Toast.LENGTH_SHORT).show());
        }
    }
}
