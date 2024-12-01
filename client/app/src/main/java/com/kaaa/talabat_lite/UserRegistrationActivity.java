package com.kaaa.talabat_lite;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

public class UserRegistrationActivity extends AppCompatActivity {

    EditText editTextName, editTextPhone, editTextEmail, editTextPassword, editTextCountry, editTextCity;
    Button buttonSubmit;
    RadioGroup radioGroupUserType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_registration); // This loads the updated layout

        // Initialize the views
        initUI();
        // Set OnClickListener for the Submit button
        setupListeners();
    }
    private void initUI()
    {
        editTextName = findViewById(R.id.editTextName);
        editTextPhone = findViewById(R.id.editTextPhone);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextCountry = findViewById(R.id.editTextCountry);
        editTextCity = findViewById(R.id.editTextCity);
        buttonSubmit = findViewById(R.id.buttonSubmit);
        radioGroupUserType = findViewById(R.id.radioGroupUserType);
    }
    private void setupListeners(){
        buttonSubmit.setOnClickListener(view -> new Thread(this::addUserData).start());
    }
    private void addUserData() {
        // Retrieve the user input
        String name = editTextName.getText().toString();
        String phone = editTextPhone.getText().toString();
        String email = editTextEmail.getText().toString();
        String password = editTextPassword.getText().toString();
        String country = editTextCountry.getText().toString();
        String city = editTextCity.getText().toString();

        // Get the selected RadioButton from RadioGroup
        int selectedId = radioGroupUserType.getCheckedRadioButtonId();
        RadioButton selectedRadioButton = findViewById(selectedId);
        String userType = selectedRadioButton != null ? selectedRadioButton.getText().toString() : "Not Selected";

        // Validate the fields
        if (name.isEmpty() || phone.isEmpty() || email.isEmpty() || password.isEmpty() || country.isEmpty() || city.isEmpty()) {

            runOnUiThread(()->Toast.makeText(UserRegistrationActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show());
            return;
        }
        if (selectedId == -1) { // No radio button selected
            runOnUiThread(()->Toast.makeText(UserRegistrationActivity.this, "Please select a user type", Toast.LENGTH_SHORT).show());
            return;
        }
        try {
            socketHelper.getInstance().connect();
            socketHelper.getInstance().sendInt(1020);
            socketHelper.getInstance().sendString(name);
            socketHelper.getInstance().sendString(phone);
            socketHelper.getInstance().sendString(email);
            socketHelper.getInstance().sendString(password);
            socketHelper.getInstance().sendString(country);
            socketHelper.getInstance().sendString(city);
            if (userType.equals("Customer")) socketHelper.getInstance().sendInt(1);
            else if (userType.equals("Merchant")) socketHelper.getInstance().sendInt(2);
            else socketHelper.getInstance().sendInt(3);
            int ok = socketHelper.getInstance().recvInt();
            socketHelper.getInstance().close();
            if (ok == 1) {
                runOnUiThread(()->Toast.makeText(UserRegistrationActivity.this, "Data added successfully!", Toast.LENGTH_SHORT).show());
                if (userType.equals("Customer")) {
                    // go to next activity
                    Intent intent = new Intent(UserRegistrationActivity.this, CustomerRegistration.class);
                    startActivity(intent);
                }
                else if (userType.equals("Merchant")) {
                    Intent intent = new Intent(UserRegistrationActivity.this, MerchantRegistrationActivity.class);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(UserRegistrationActivity.this, CourierRegistrationActivity.class);
                    startActivity(intent);
                }
            }
            else
            {
                runOnUiThread(()->Toast.makeText(UserRegistrationActivity.this, "this email has already registered", Toast.LENGTH_SHORT).show());
            }

        } catch (IOException e) {
            runOnUiThread(()->Toast.makeText(UserRegistrationActivity.this, "Failed to connect!", Toast.LENGTH_SHORT).show());
        }
        // Show the data
    }
}