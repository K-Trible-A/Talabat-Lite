package com.kaaa.talabat_lite;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

public class CustomerRegistration extends AppCompatActivity {

    private EditText editTextStreet ;
    private Button buttoncreat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_registration); // Assuming activity_main.xml is the layout

        // Initialize the views
        initUI();
        // Set OnClickListener for Submit button
        setupListeners();
    }
    private void setupListeners(){
        buttoncreat.setOnClickListener(view -> new Thread(this::addCustomerData).start());
    }
    private void addCustomerData() {
        // Retrieve the data from the EditText fields
        String street = editTextStreet.getText().toString();
        // Basic validation
        if (street.isEmpty()) {
            runOnUiThread(()->Toast.makeText(CustomerRegistration.this, "Please fill your street field", Toast.LENGTH_SHORT).show());
            return;
        }
        try {
            Intent intent = getIntent();
            int userId = intent.getIntExtra("userId",0);// 0 is the default value
            socketHelper.getInstance().connect();
            socketHelper.getInstance().sendInt(1021);
            socketHelper.getInstance().sendString(street);
            socketHelper.getInstance().sendInt(userId);
            int ok = socketHelper.getInstance().recvInt();
            socketHelper.getInstance().close();
            if (ok == 1) {
                runOnUiThread(()->Toast.makeText(CustomerRegistration.this, "Data added successfully!", Toast.LENGTH_SHORT).show());
            }

        } catch (IOException e) {
            runOnUiThread(()->Toast.makeText(CustomerRegistration.this, "Failed to connect!", Toast.LENGTH_SHORT).show());
        }
    }
    private void initUI()
    {
        editTextStreet = findViewById(R.id.editTextStreet);
        buttoncreat = findViewById(R.id.buttonCreat);
    }
}