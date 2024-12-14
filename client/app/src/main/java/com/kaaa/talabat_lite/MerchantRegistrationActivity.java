package com.kaaa.talabat_lite;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class MerchantRegistrationActivity extends AppCompatActivity {

    private EditText businessName ,keywords ,pickupAddress ,nationalID;
    private String cardNumberStr = "", expiryDateStr = "", CVVStr = "";
    final String[] selectedRadio = new String[1];
    private Button submit , addCard;
    private int businessType = -1;
    private RadioGroup radioType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_merchant_registration);
        initUI();
        setupListeners();
    }
    protected void initUI() {
        businessName = findViewById(R.id.businessName);
        keywords = findViewById(R.id.keywords);
        pickupAddress = findViewById(R.id.pickupAddress);
        nationalID = findViewById(R.id.nationalID);
        submit = findViewById(R.id.submit);
        addCard = findViewById(R.id.addCard);
        radioType = findViewById(R.id.radioType);
    }
    private void setupListeners ()
    {
        radioType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.grocery)
                businessType = 1;
            else if (checkedId == R.id.restaurant)
                businessType = 2;
            else if (checkedId == R.id.pharmacy)
                businessType = 3;
            RadioButton selected = findViewById(checkedId);
            selectedRadio[0] = selected.getText().toString();
        });
        addCard.setOnClickListener(v -> {
            Intent cardIntent = new Intent(MerchantRegistrationActivity.this, CardActivity.class);
            cardActivityResultLauncher.launch(cardIntent);
        });
        submit.setOnClickListener(view -> new Thread(this::addMerchant).start());
    }
    private final ActivityResultLauncher<Intent> cardActivityResultLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        cardNumberStr = data.getStringExtra("cardNumber");
                        expiryDateStr = data.getStringExtra("expiryDate");
                        CVVStr = data.getStringExtra("CVV");
                        Toast.makeText(this, "Card details received!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Card entry canceled", Toast.LENGTH_SHORT).show();
                }
            });

    private void showToast(String message) {
        runOnUiThread(() -> Toast.makeText(this, message, Toast.LENGTH_SHORT).show());
    }
    private boolean validateInputs() {
        String businessNameStr = businessName.getText().toString().trim();
        String keywordsStr = keywords.getText().toString().trim();
        String pickupAddressStr = pickupAddress.getText().toString().trim();
        String nationalIDStr = nationalID.getText().toString().trim();
        if (businessNameStr.isEmpty()) {
            showToast("Business name is empty!");
            return false;
        }
        if (businessType == -1) {
            showToast("Choose business type!");
            return false;
        }
        if (keywordsStr.isEmpty()) {
            showToast("Keywords are empty!");
            return false;
        }
        if (pickupAddressStr.isEmpty()) {
            showToast("Pickup address field is empty!");
            return false;
        }
        if (nationalIDStr.isEmpty()) {
            showToast("National ID is empty!");
            return false;
        }
        if (nationalIDStr.length() < 16) {
            showToast("National ID must have 16 numbers!");
            return false;
        }
        if (cardNumberStr.isEmpty() || CVVStr.isEmpty() || expiryDateStr.isEmpty()) {
            showToast("Fill in card information!");
            return false;
        }
        return true;
    }

    private void addMerchant()
    {
        if(!validateInputs()) return;
        Intent intent = getIntent();
        String name = intent.getStringExtra("userName");
        String phone = intent.getStringExtra("userPhone");
        String email = intent.getStringExtra("userEmail");
        String password = intent.getStringExtra("userPassword");
        String country = intent.getStringExtra("userCountry");
        String city = intent.getStringExtra("userCity");
        int accountType=intent.getIntExtra("accountType",0);
        String businessNameStr = businessName.getText().toString().trim();
        String keywordsStr = keywords.getText().toString().trim();
        String pickupAddressStr = pickupAddress.getText().toString().trim();
        String nationalIDStr = nationalID.getText().toString().trim();
        try {
            // Prepare the URL for the endpoint
            URL server = new URL(globals.serverURL + "/registration/merchant");
            // Create the JSON payload
            JSONObject jsonPayload = new JSONObject();
            jsonPayload.put("phone", phone);
            jsonPayload.put("email", email);
            jsonPayload.put("name", name);
            jsonPayload.put("password", password);
            jsonPayload.put("country", country);
            jsonPayload.put("city", city);
            jsonPayload.put("accountType", accountType);
            jsonPayload.put("businessName", businessNameStr);
            jsonPayload.put("businessType", businessType);
            jsonPayload.put("keywords", keywordsStr);
            jsonPayload.put("pickupAddress", pickupAddressStr);
            jsonPayload.put("nationalID", nationalIDStr);
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
                showToast("Registration success");
                Intent loginIntent = new Intent(MerchantRegistrationActivity.this,LoginActivity.class);
                finish();
                startActivity(loginIntent);
            }
            else if(responseCode == HttpURLConnection.HTTP_CONFLICT){
                showToast("Account with same email or phone number");
            }
            else {
                showToast("Registration error");
                Log.i("MerchantRegistration", conn.getResponseMessage());
            }
            conn.disconnect();

        } catch (IOException e) {
            showToast("Failed to read response");
        } catch (JSONException e) {
            Log.e("MerchantRegistration", "Json error");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1)
        {
            if (resultCode == RESULT_OK)
            {
                assert data != null;
                cardNumberStr = data.getStringExtra("cardNumber");
                expiryDateStr = data.getStringExtra("expiryDate");
                CVVStr = data.getStringExtra("CVV");
            }
        }
    }
}
