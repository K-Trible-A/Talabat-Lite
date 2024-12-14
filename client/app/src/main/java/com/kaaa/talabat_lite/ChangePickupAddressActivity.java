package com.kaaa.talabat_lite;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

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
    private void showToast(String message) {
        runOnUiThread(() -> Toast.makeText(this, message, Toast.LENGTH_SHORT).show());
    }
    private void changePickupAddress() {
        pickupAddress = pickupAddressField.getText().toString().trim();
        if (pickupAddress.isEmpty()) {
            runOnUiThread(() ->
                    Toast.makeText(ChangePickupAddressActivity.this, "Pickup address field is empty!", Toast.LENGTH_SHORT).show());
            return;
        }
        try {
            // Prepare the URL for the endpoint
            URL server = new URL(globals.serverURL + "/changePickupAddress/" + globals.userId);
            // Create the JSON payload
            JSONObject jsonPayload = new JSONObject();
            jsonPayload.put("pickupAddress", pickupAddress);
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

            if (responseCode == HttpURLConnection.HTTP_OK) {
                conn.disconnect();
                Intent resultIntent = new Intent();
                resultIntent.putExtra("updatedPickupAddress", pickupAddress);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }
            else {
                showToast("Changing error");
                Log.i("ChangePickupAddress", conn.getResponseMessage());
            }
            conn.disconnect();
        } catch (IOException e) {
            showToast("Failed to read response");
        } catch (JSONException e) {
            Log.e("ChangePickupAddress", "Json error");
        }
    }
}
