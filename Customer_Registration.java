package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;


public class Customer_Registration extends AppCompatActivity {

    EditText editTextStreet ;
     Button buttonCreat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_registration); // Assuming activity_main.xml is the layout

        // Initialize the views
        initUI();
        // Set OnClickListener for Submit button
        buttonCreat.setOnClickListener(this::onClick);
    }
    private void onClick(View v) {
        // Retrieve the data from the EditText fields
        String street = editTextStreet.getText().toString();
        // Basic validation
        if (street.isEmpty()) {
            Toast.makeText(Customer_Registration.this, "Please fill your street field", Toast.LENGTH_SHORT).show();
        }
        // Show the data in a Toast for demonstration
    }
    private void initUI()
    {
        editTextStreet = findViewById(R.id.editTextStreet);
        buttonCreat = findViewById(R.id.buttonCreat);
    }
}