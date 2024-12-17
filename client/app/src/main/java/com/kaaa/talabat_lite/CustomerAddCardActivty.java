package com.kaaa.talabat_lite;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CustomerAddCardActivty extends AppCompatActivity {

    EditText cardNumber, expiryDate, CVV;
    Button submitButton;
    Intent outIntent;
    private static final String PREFS_NAME = "CardPrefs";
    private static final String KEY_CARD_NUMBER = "cardNumber";
    private static final String KEY_EXPIRY_DATE = "expiryDate";
    private static final String KEY_CVV = "CVV";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_customer_add_card_activty);
        initUI();
        loadSavedData();
        setupListeners();

    }

    protected void initUI() {

        cardNumber = findViewById(R.id.cardNumber);
        expiryDate = findViewById(R.id.expiryDate);
        CVV = findViewById(R.id.CVV);
        submitButton = findViewById(R.id.submitButton);
    }

    protected void setupListeners() {

        submitButton.setOnClickListener(v -> {
            String cardNumberStr = cardNumber.getText().toString().trim();
            String CVVStr = CVV.getText().toString().trim();
            String expiryDateStr = expiryDate.getText().toString().trim();
            if (cardNumberStr.length() < 16) {
                runOnUiThread(() -> Toast.makeText(CustomerAddCardActivty.this, "card number has 16 numbers!", Toast.LENGTH_SHORT).show());
                return;
            }
            if (expiryDateStr.length() < 5) {
                runOnUiThread(() -> Toast.makeText(CustomerAddCardActivty.this, "expiry date format is MM/YY", Toast.LENGTH_SHORT).show());
                return;
            }
            if (expiryDateStr.charAt(0) < '0' || expiryDateStr.charAt(0) > '9' || expiryDateStr.charAt(1) < '0' || expiryDateStr.charAt(1) > '9' || expiryDateStr.charAt(3) < '0' || expiryDateStr.charAt(3) > '9' || expiryDateStr.charAt(4) < '0' || expiryDateStr.charAt(4) > '9' || expiryDateStr.charAt(2) != '/') {
                runOnUiThread(() -> Toast.makeText(CustomerAddCardActivty.this, "expiry date format is MM/YY", Toast.LENGTH_SHORT).show());
                return;
            }
            if ((expiryDateStr.charAt(0) >= '1' && expiryDateStr.charAt(1) >= '3') || (expiryDateStr.charAt(0) == '0' && expiryDateStr.charAt(1) == '0') || (expiryDateStr.charAt(3) == '0' && expiryDateStr.charAt(4) == '0') || (expiryDateStr.charAt(0) >= '2')) {
                runOnUiThread(() -> Toast.makeText(CustomerAddCardActivty.this, "enter a valid date!", Toast.LENGTH_SHORT).show());
                return;
            }
            if (CVV.length() < 3) {
                runOnUiThread(() -> Toast.makeText(CustomerAddCardActivty.this, "CVV has 3 numbers!", Toast.LENGTH_SHORT).show());
                return;
            }
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {

                try {
                    // Prepare the URL for the endpoint
                    URL server = new URL(globals.serverURL + "/customer/addCard/" + globals.userId);
                    // Create the JSON payload
                    JSONObject jsonPayload = new JSONObject();
                    jsonPayload.put("cardNumber", cardNumberStr);
                    jsonPayload.put("expiryDate", expiryDateStr);
                    jsonPayload.put("CVV", CVVStr);
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
                        runOnUiThread(() -> Toast.makeText(CustomerAddCardActivty.this, "Card is Successfully added", Toast.LENGTH_SHORT).show());
                        outIntent = new Intent(CustomerAddCardActivty.this, CustomerActivity.class);
                        startActivity(outIntent);
                    }

                    conn.disconnect();

                } catch (IOException e) {
                    runOnUiThread(() -> Toast.makeText(CustomerAddCardActivty.this, "Failed to add card", Toast.LENGTH_SHORT).show());
                } catch (JSONException e) {
                    Log.e("CustomerAddCard", "Json error");
                }

            });
        });
    }

    private void loadSavedData() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String savedCardNumber = sharedPreferences.getString(KEY_CARD_NUMBER, "");
        String savedExpiryDate = sharedPreferences.getString(KEY_EXPIRY_DATE, "");
        String savedCVV = sharedPreferences.getString(KEY_CVV, "");

        // Set saved values in the input fields
        cardNumber.setText(savedCardNumber);
        expiryDate.setText(savedExpiryDate);
        CVV.setText(savedCVV);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clear saved data when the activity is destroyed
        clearSavedData();
    }
    private void clearSavedData() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_CARD_NUMBER);
        editor.remove(KEY_EXPIRY_DATE);
        editor.remove(KEY_CVV);
        editor.apply(); // Apply the changes asynchronously

    }
}
