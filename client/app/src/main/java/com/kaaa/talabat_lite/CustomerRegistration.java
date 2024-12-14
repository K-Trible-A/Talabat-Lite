package com.kaaa.talabat_lite;

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

public class CustomerRegistration extends AppCompatActivity {
    private EditText editTextStreet ;
    private Button buttonCreate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_registration); // Assuming activity_main.xml is the layout
        // Initialize the views
        initUI();
        // Set OnClickListener for Submit button
        setupListeners();
    }
    private void initUI()
    {
        editTextStreet = findViewById(R.id.editTextStreet);
        buttonCreate = findViewById(R.id.buttonCreat);
    }
    private void setupListeners(){
        buttonCreate.setOnClickListener(view -> new Thread(this::addCustomerData).start());
    }
    private void showToast(String message) {
        runOnUiThread(() -> Toast.makeText(this, message, Toast.LENGTH_SHORT).show());
    }
    private void addCustomerData() {
        // Retrieve the data from the EditText fields
        String deliveryAddress = editTextStreet.getText().toString();
        // Basic validation
        if (deliveryAddress.isEmpty()) {
            showToast("Please fill your deliveryAddress field");
            return;
        }
        Intent intent = getIntent();
        String name = intent.getStringExtra("userName");
        String phone = intent.getStringExtra("userPhone");
        String email = intent.getStringExtra("userEmail");
        String password = intent.getStringExtra("userPassword");
        String country = intent.getStringExtra("userCountry");
        String city = intent.getStringExtra("userCity");
        int accountType=intent.getIntExtra("accountType",0);
        try {
            // Prepare the URL for the endpoint
            URL server = new URL(globals.serverURL + "/registration/customer");
            // Create the JSON payload
            JSONObject jsonPayload = new JSONObject();
            jsonPayload.put("phone", phone);
            jsonPayload.put("email", email);
            jsonPayload.put("name", name);
            jsonPayload.put("password", password);
            jsonPayload.put("country", country);
            jsonPayload.put("city", city);
            jsonPayload.put("accountType", accountType);
            jsonPayload.put("deliveryAddress", deliveryAddress);

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
                showToast("Registration success");
                Intent outIntent = new Intent(this, LoginActivity.class);
                finish();
                startActivity(outIntent);
            }
            else if(responseCode == HttpURLConnection.HTTP_CONFLICT){
                showToast("Account with same email or phone number");
            }
            else {
                showToast("Registration error");
                Log.i("CustomerRegistration", conn.getResponseMessage());
            }
            conn.disconnect();
        } catch (IOException e) {
            showToast("Failed to read response");
        } catch (JSONException e) {
            Log.e("CustomerRegistration", "Json error");
        }
    }
}
