package com.example.kaaa;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class CardActivity extends AppCompatActivity {

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
        setContentView(R.layout.activity_card);
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

    protected void setupListeners ()
    {

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String cardNumberStr = cardNumber.getText().toString().trim();
                String CVVStr = CVV.getText().toString().trim();
                String expiryDateStr = expiryDate.getText().toString().trim();
                if (cardNumberStr.length() < 16)
                {
                    runOnUiThread(() -> Toast.makeText(CardActivity.this, "card number has 16 numbers!", Toast.LENGTH_SHORT).show());
                    return;
                }
                if (expiryDateStr.length() < 5)
                {
                    runOnUiThread(() -> Toast.makeText(CardActivity.this, "expiry date format is MM/YY", Toast.LENGTH_SHORT).show());
                    return;
                }
                if (expiryDateStr.charAt(0) < '0' || expiryDateStr.charAt(0) > '9' || expiryDateStr.charAt(1) < '0' || expiryDateStr.charAt(1) > '9' || expiryDateStr.charAt(3) < '0' || expiryDateStr.charAt(3) > '9' || expiryDateStr.charAt(4) < '0' || expiryDateStr.charAt(4) > '9' || expiryDateStr.charAt(2) != '/')
                {
                    runOnUiThread(() -> Toast.makeText(CardActivity.this, "expiry date format is MM/YY", Toast.LENGTH_SHORT).show());
                    return;
                }
                if ((expiryDateStr.charAt(0) >= '1' && expiryDateStr.charAt(1) >= '3') || (expiryDateStr.charAt(0) == '0' && expiryDateStr.charAt(1) == '0') || (expiryDateStr.charAt(3) == '0' && expiryDateStr.charAt(4) == '0') || (expiryDateStr.charAt(0) >= '2'))
                {
                    runOnUiThread(() -> Toast.makeText(CardActivity.this, "enter a valid date!", Toast.LENGTH_SHORT).show());
                    return;
                }
                if (CVV.length() < 3)
                {
                    runOnUiThread(() -> Toast.makeText(CardActivity.this, "CVV has 3 numbers!", Toast.LENGTH_SHORT).show());
                    return;
                }
                saveData(cardNumberStr, expiryDateStr, CVVStr);
                outIntent = new Intent(CardActivity.this, MerchantRegistrationActivity.class);
                outIntent.putExtra("cardNumber",cardNumberStr);
                outIntent.putExtra("expiryDate",expiryDateStr);
                outIntent.putExtra("CVV",CVVStr);
                setResult(RESULT_OK,outIntent);
                finish();
            }
        });
    }
    private void saveData(String cardNumberStr, String expiryDateStr, String CVVStr) {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_CARD_NUMBER, cardNumberStr);
        editor.putString(KEY_EXPIRY_DATE, expiryDateStr);
        editor.putString(KEY_CVV, CVVStr);
        editor.apply(); // Commit changes asynchronously
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
}
