package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.RadioButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {

    EditText editTextName, editTextPhone, editTextEmail, editTextPassword, editTextCountry, editTextCity;
    Button buttonSubmit;
    RadioGroup radioGroupUserType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // This loads the updated layout

        // Initialize the views
        editTextName = findViewById(R.id.editTextName);
        editTextPhone = findViewById(R.id.editTextPhone);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextCountry = findViewById(R.id.editTextCountry);
        editTextCity = findViewById(R.id.editTextCity);
        buttonSubmit = findViewById(R.id.buttonSubmit);
        radioGroupUserType = findViewById(R.id.radioGroupUserType);

        // Set OnClickListener for the Submit button
        buttonSubmit.setOnClickListener(this::onClick);
    }
    private void onClick(View v) {
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

            Toast.makeText(MainActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedId == -1) { // No radio button selected
            Toast.makeText(MainActivity.this, "Please select a user type", Toast.LENGTH_SHORT).show();
            return;
        }
        // Show the data
        if (userType.equals("Customer")){
            // go to next activitiy
            Intent intent = new Intent(MainActivity.this, Customer_Registration.class);
            startActivity(intent);
        }
        else {
            Toast.makeText(MainActivity.this, "You will register as "+userType, Toast.LENGTH_SHORT).show();
        }
    }
}