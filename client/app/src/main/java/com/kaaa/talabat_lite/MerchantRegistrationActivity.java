package com.example.merchant;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

public class MerchantRegistrationActivity extends AppCompatActivity {

    EditText businessName ,keywords ,pickupAddress ,nationalID;
    String cardNumberStr = "null";
    String expiryDateStr = "null";
    String CVVStr = "null";
    final String[] selectedRadio = new String[1];
    String IP = "192.168.1.6";
    int portNum = 57000;
    Button submit , addCard;
    int businessType = -1;
    RadioGroup radioType;
    Intent cardIntent;


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
        radioType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.grocery)
                    businessType = 1;
                else if (checkedId == R.id.restaurant)
                    businessType = 2;
                else if (checkedId == R.id.pharmacy)
                    businessType = 3;
                RadioButton selected = findViewById(checkedId);
                selectedRadio[0] = selected.getText().toString();
            }
        });
        addCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cardIntent = new Intent(MerchantRegistrationActivity.this,CardActivity.class);
                startActivityForResult(cardIntent,1);
            }
        });

        submit.setOnClickListener(view -> new Thread(this::addMerchant).start());

    }


    private void addMerchant()
    {
        socketHelper.getInstance().IP = IP;
        socketHelper.getInstance().portNum = portNum;
        String businessNameStr = businessName.getText().toString().trim();
        String keywordsStr = keywords.getText().toString().trim();
        String pickupAddressStr = pickupAddress.getText().toString().trim();
        String nationalIDStr = nationalID.getText().toString().trim();
        if (businessNameStr.isEmpty())
        {
            runOnUiThread(() -> Toast.makeText(MerchantRegistrationActivity.this, "Business name is empty!", Toast.LENGTH_SHORT).show());
            return;
        }
        if (businessType == -1)
        {
            runOnUiThread(() -> Toast.makeText(MerchantRegistrationActivity.this, "Choose business type!", Toast.LENGTH_SHORT).show());
            return;
        }
        if (keywordsStr.isEmpty())
        {
            runOnUiThread(() -> Toast.makeText(MerchantRegistrationActivity.this, "keywords is empty!", Toast.LENGTH_SHORT).show());
            return;
        }
        if (pickupAddressStr.isEmpty())
        {
            runOnUiThread(() -> Toast.makeText(MerchantRegistrationActivity.this, "pickup address field is empty!", Toast.LENGTH_SHORT).show());
            return;
        }
        if (nationalIDStr.isEmpty())
        {
            runOnUiThread(() -> Toast.makeText(MerchantRegistrationActivity.this, "national ID is empty!", Toast.LENGTH_SHORT).show());
            return;
        }
        if (nationalIDStr.length() < 16)
        {
            runOnUiThread(() -> Toast.makeText(MerchantRegistrationActivity.this, "national ID has 16 numbers!", Toast.LENGTH_SHORT).show());
            return;
        }

        try {
            socketHelper.getInstance().connect();
            socketHelper.getInstance().sendInt(1030);
            socketHelper.getInstance().sendString(businessNameStr);
            socketHelper.getInstance().sendInt(businessType);
            socketHelper.getInstance().sendString(keywordsStr);
            socketHelper.getInstance().sendString(pickupAddressStr);
            socketHelper.getInstance().sendString(nationalIDStr);
            socketHelper.getInstance().sendString(cardNumberStr);
            socketHelper.getInstance().sendString(expiryDateStr);
            socketHelper.getInstance().sendString(CVVStr);
            int ok = socketHelper.getInstance().recvInt();
            if (ok == 1) {
                runOnUiThread(() -> Toast.makeText(MerchantRegistrationActivity.this, "Data added successfully!", Toast.LENGTH_SHORT).show());
            }
            socketHelper.getInstance().close();

        } catch (IOException e) {
            Log.e("Socket", "Connection failed", e);
            runOnUiThread(() -> Toast.makeText(MerchantRegistrationActivity.this, "Failed to connect!", Toast.LENGTH_SHORT).show());
        }

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1)
        {
            if (resultCode == RESULT_OK)
            {
                cardNumberStr = data.getStringExtra("cardNumber");
                expiryDateStr = data.getStringExtra("expiryDate");
                CVVStr = data.getStringExtra("CVV");
            }
        }
    }
}