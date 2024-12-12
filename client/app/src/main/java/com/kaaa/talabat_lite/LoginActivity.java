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

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextEmail, editTextPass;
    private Button buttonLogin;
    private TextView registrationText;
    private ProgressBar progressBar;
    Intent outIntent;
    Intent afterLoginIntent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
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
            outIntent = new Intent(LoginActivity.this, UserRegistrationActivity.class);
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
            socketHelper.getInstance().connect();
            socketHelper.getInstance().sendInt(globals.AUTHENTICATE_CLIENT);

            socketHelper.getInstance().sendString(email);
            socketHelper.getInstance().sendString(pass);

            int ok = socketHelper.getInstance().recvInt();

            socketHelper.getInstance().close();

            if(ok != -1){
                globals.userId = ok;
                runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Login success!", Toast.LENGTH_SHORT).show());
                socketHelper.getInstance().connect();
                socketHelper.getInstance().sendInt(globals.CHECK_ACCOUNT_TYPE);
                socketHelper.getInstance().sendInt(globals.userId);
                int accountType = socketHelper.getInstance().recvInt();
                if (accountType == globals.MERCHANT)
                {
                    afterLoginIntent = new Intent(LoginActivity.this,MerchantActivity.class);
                    startActivity(afterLoginIntent);
                }
                else if(accountType == globals.CUSTOMER)
                {
                    afterLoginIntent = new Intent(LoginActivity.this,CustomerActivity.class);
                    startActivity(afterLoginIntent);
                }

            }
            else{
                runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Login failed!", Toast.LENGTH_SHORT).show());
            }
        } catch (IOException e) {
            runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Failed to Connect", Toast.LENGTH_SHORT).show());
        }
        runOnUiThread(() -> progressBar.setVisibility(View.GONE));
    }
}
