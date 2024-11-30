package com.kaaa.talabat_lite;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

public class CourierRegistrationActivity extends AppCompatActivity {

    EditText nationalID;
    String cardNumberStr = "null";
    String expiryDateStr = "null";
    String CVVStr = "null";
    final String[] selectedRadio = new String[1];
    Button submit , addCard;
    int vehicleType = -1;
    RadioGroup radioType;
    Intent cardIntent;


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
            cardIntent = new Intent(CourierRegistrationActivity.this, CardActivity.class);
            cardActivityResultLauncher.launch(cardIntent);

        });

        submit.setOnClickListener(view -> new Thread(this::addCourier).start());

    }


    private void addCourier()
    {
        String nationalIDStr = nationalID.getText().toString().trim();
        if (nationalIDStr.isEmpty())
        {
            runOnUiThread(() -> Toast.makeText(CourierRegistrationActivity.this, "national ID is empty!", Toast.LENGTH_SHORT).show());
            return;
        }
        if (nationalIDStr.length() < 16)
        {
            runOnUiThread(() -> Toast.makeText(CourierRegistrationActivity.this, "national ID has 16 numbers!", Toast.LENGTH_SHORT).show());
            return;
        }

        try {
            socketHelper.getInstance().connect();
            socketHelper.getInstance().sendInt(1000);
            socketHelper.getInstance().sendInt(vehicleType);
            socketHelper.getInstance().sendString(nationalIDStr);
            socketHelper.getInstance().sendString(cardNumberStr);
            socketHelper.getInstance().sendString(expiryDateStr);
            socketHelper.getInstance().sendString(CVVStr);
            int ok = socketHelper.getInstance().recvInt();
            if (ok == 1) {
                runOnUiThread(() -> Toast.makeText(CourierRegistrationActivity.this, "Data added successfully!", Toast.LENGTH_SHORT).show());
            }
            socketHelper.getInstance().close();

        } catch (IOException e) {
            runOnUiThread(() -> Toast.makeText(CourierRegistrationActivity.this, "Failed to connect!", Toast.LENGTH_SHORT).show());
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
