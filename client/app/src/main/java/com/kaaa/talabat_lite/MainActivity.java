package com.kaaa.talabat_lite;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    private EditText editTextIp, editTextPort;
    private Button buttonConnect;
    private Intent outIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView((R.layout.activity_main));
        initUI();
        setupListeners();
    }

    private void initUI(){
        // Initialize UI elements
        editTextIp = findViewById(R.id.editTextIp);
        editTextPort = findViewById(R.id.editTextPort);
        buttonConnect = findViewById(R.id.buttonConnect);
    }
    private void setupListeners(){
        buttonConnect.setOnClickListener(view -> new Thread(this::connectServer).start());
    }

    private boolean validIP(String IP){
        if(IP.isEmpty()) return false;
        String IPV4_PATTERN = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";
        Pattern pattern = Pattern.compile(IPV4_PATTERN);
        Matcher matcher = pattern.matcher(IP);
        return matcher.matches();
    }

    private void connectServer() {
        String IP = editTextIp.getText().toString().trim();
        String portNumStr = editTextPort.getText().toString().trim();
        if (!validIP(IP) || portNumStr.isEmpty()) {
            runOnUiThread(() -> Toast.makeText(MainActivity.this, "Invalid IP or port number", Toast.LENGTH_SHORT).show());
            return;
        }

        int portNum = Integer.parseInt((portNumStr));

        // Make IP and port number global for entire application
        socketHelper.getInstance().IP = IP;
        socketHelper.getInstance().portNum = portNum;

        try {
            socketHelper.getInstance().connect();
            socketHelper.getInstance().sendInt(1010);
            socketHelper.getInstance().close();
            runOnUiThread(() -> {
                Toast.makeText(MainActivity.this, "Connected to server", Toast.LENGTH_SHORT).show();
                outIntent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(outIntent);
            });

        } catch (IOException e) {
            runOnUiThread(() -> Toast.makeText(MainActivity.this, "Failed to Connect", Toast.LENGTH_SHORT).show());
        }
    }
}