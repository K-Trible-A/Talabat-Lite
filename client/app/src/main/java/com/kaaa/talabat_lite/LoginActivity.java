package com.kaaa.talabat_lite;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextEmail, editTextPass;
    private Button buttonLogin;
    private TextView registrationText;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initUI();
        setupListeners();
    }
    private void initUI(){
        // Initialize UI elements
        editTextEmail = findViewById(R.id.login_email);
        editTextPass = findViewById(R.id.login_pass);
        buttonLogin = findViewById(R.id.login_button);
        registrationText = findViewById(R.id.login_registration);
        progressBar = findViewById(R.id.progress_bar);
    }
    private void setupListeners(){
        buttonLogin.setOnClickListener(view -> new Thread(this::loginAuth).start());
        registrationText.setOnClickListener(v -> {
            Intent outIntent = new Intent(LoginActivity.this, UserRegistrationActivity.class);
            startActivity(outIntent);
        });
    }
    private void loginAuth(){
        runOnUiThread(() -> progressBar.setVisibility(View.VISIBLE));

        String email = editTextEmail.getText().toString().trim();
        String pass = editTextPass.getText().toString().trim();

        if (email.isEmpty() || pass.isEmpty()) {
            runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Please fill both fields", Toast.LENGTH_SHORT).show());
            runOnUiThread(() -> progressBar.setVisibility(View.GONE));
            return;
        }

        try {
            // API URL
            URL server = new URL(globals.serverURL + "/login");
            HttpURLConnection conn = (HttpURLConnection) server.openConnection();
            // Setup request
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            conn.setDoOutput(true);
            // Create JSON payload
            JSONObject json = new JSONObject();
            json.put("email", email);
            json.put("password", pass);
            // Send the request
            OutputStream os = conn.getOutputStream();
            os.write(json.toString().getBytes(StandardCharsets.UTF_8));
            os.flush();
            os.close();
            // Get the response code
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                runOnUiThread(()-> Toast.makeText(this, "Login success", Toast.LENGTH_SHORT).show());
                // Read the response
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
                in.close();
                // Parse the response JSON
                JSONObject jsonResponse = new JSONObject(response.toString());
                globals.userId = jsonResponse.getInt("userId");
                int accountType = jsonResponse.getInt("accountType");
                Log.i("userId", String.valueOf(globals.userId));
                Log.i("AccountType", String.valueOf(accountType));
                if(accountType == 1){
                    globals.isCustomer = true;
                    Intent afterLoginIntent = new Intent(this, CustomerActivity.class);
                    afterLoginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(afterLoginIntent);
                }
                else if(accountType == 2){
                    Intent afterLoginIntent = new Intent(LoginActivity.this, MerchantActivity.class);
                    // remove the activity from the stack
                    afterLoginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(afterLoginIntent);
                }
                else if(accountType == 3){
                    Intent afterLoginIntent = new Intent(LoginActivity.this, CourierActivity.class);
                    afterLoginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(afterLoginIntent);
                }
            } else if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                runOnUiThread(()-> Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show());
            } else {
                runOnUiThread(()-> Toast.makeText(this, "Server error: " + responseCode, Toast.LENGTH_SHORT).show());
            }
            conn.disconnect();
        } catch (JSONException | IOException e) {
            runOnUiThread(()-> Toast.makeText(this, "Server not reached", Toast.LENGTH_SHORT).show());
            //Log.e("LoginActivity", "Connection error", e);
        }
        runOnUiThread(() -> progressBar.setVisibility(View.GONE));
    }
}
