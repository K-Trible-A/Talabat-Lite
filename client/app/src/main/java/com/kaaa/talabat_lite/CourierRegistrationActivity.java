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

public class CourierRegistrationActivity extends AppCompatActivity {

    private EditText nationalID;
    private String cardNumberStr = "null", expiryDateStr = "null", CVVStr = "null";
    final String[] selectedRadio = new String[1];
    private Button submit , addCard;
    private int vehicleType = -1;
    private RadioGroup radioType;


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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_courier_registration);
        initUI();
        setupListeners();
    }

    protected void initUI() {
        nationalID = findViewById(R.id.nationalID);
        submit = findViewById(R.id.submit);
        addCard = findViewById(R.id.addCard);
        radioType = findViewById(R.id.radioType);
    }

    private void setupListeners ()
    {
        radioType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.car)
                vehicleType = 1;
            else if (checkedId == R.id.motorcycle)
                vehicleType = 2;
            else if (checkedId == R.id.bicycle)
                vehicleType = 3;
            RadioButton selected = findViewById(checkedId);
            selectedRadio[0] = selected.getText().toString();
        });
        addCard.setOnClickListener(v -> {
            Intent cardIntent = new Intent(CourierRegistrationActivity.this, CardActivity.class);
            cardActivityResultLauncher.launch(cardIntent);
        });
        submit.setOnClickListener(view -> new Thread(this::addCourier).start());
    }
    private void showToast(String message) {
        runOnUiThread(() -> Toast.makeText(this, message, Toast.LENGTH_SHORT).show());
    }
    private void addCourier() {
        String nationalIDStr = nationalID.getText().toString().trim();
        if (nationalIDStr.isEmpty()) {
            runOnUiThread(() -> Toast.makeText(CourierRegistrationActivity.this, "national ID is empty!", Toast.LENGTH_SHORT).show());
            return;
        }
        if (nationalIDStr.length() < 16) {
            runOnUiThread(() -> Toast.makeText(CourierRegistrationActivity.this, "national ID has 16 numbers!", Toast.LENGTH_SHORT).show());
            return;
        }


        Intent intent = getIntent();
        String name = intent.getStringExtra("userName");
        String phone = intent.getStringExtra("userPhone");
        String email = intent.getStringExtra("userEmail");
        String password = intent.getStringExtra("userPassword");
        String country = intent.getStringExtra("userCountry");
        String city = intent.getStringExtra("userCity");
        int accountType = intent.getIntExtra("accountType", 0);

        try {
            // Prepare the URL for the endpoint
            URL server = new URL(globals.serverURL + "/registration/courier");
            // Create the JSON payload
            JSONObject jsonPayload = new JSONObject();
            jsonPayload.put("phone", phone);
            jsonPayload.put("email", email);
            jsonPayload.put("name", name);
            jsonPayload.put("password", password);
            jsonPayload.put("country", country);
            jsonPayload.put("city", city);
            jsonPayload.put("accountType", accountType);

            jsonPayload.put("vehicleType", vehicleType);
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
                Intent outIntent = new Intent(this, LoginActivity.class);
                finish();
                startActivity(outIntent);
            }
            else if(responseCode == HttpURLConnection.HTTP_CONFLICT){
                showToast("Account with same email or phone number");
            }
            else {
                showToast("Registration error");
                Log.i("CourierRegistration", conn.getResponseMessage());
            }
            conn.disconnect();
        } catch (IOException e) {
            showToast("Failed to read response");
        } catch (JSONException e) {
            Log.e("CustomerRegistration", "Json error");
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
