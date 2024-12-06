package com.kaaa.talabat_lite;

import static com.kaaa.talabat_lite.globals.CHANGE_PICKUP_ADDRESS;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

public class ChangePickupAddressActivity extends AppCompatActivity {

    String pickcupAddress;
    EditText pickupAddressField;
    Button submitAddressButton;
    Intent merchantIntent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_change_pickup_address);
        initUI();
        setupListeners();
    }
    protected void initUI()
    {
          pickupAddressField = findViewById(R.id.pickupAddress);
          pickcupAddress = getIntent().getStringExtra("currentAddress");
          pickupAddressField.setText(pickcupAddress);
          submitAddressButton =  findViewById(R.id.submitAddressButton);
    }
    protected void setupListeners()
    {
       submitAddressButton.setOnClickListener(view -> new Thread(this::changePickupAddress).start());
    }
    private void changePickupAddress()
    {
        pickcupAddress = pickupAddressField.getText().toString().trim();
        if (pickcupAddress.isEmpty())
        {
            runOnUiThread(() -> Toast.makeText(ChangePickupAddressActivity.this, "pickup address field is empty!", Toast.LENGTH_SHORT).show());
            return;
        }
        try
        {
            socketHelper.getInstance().connect();
            socketHelper.getInstance().sendInt(CHANGE_PICKUP_ADDRESS);
            socketHelper.getInstance().sendInt(globals.userId);
            socketHelper.getInstance().sendString(pickcupAddress);
            int ok = socketHelper.getInstance().recvInt();
            socketHelper.getInstance().close();
            if (ok == 1)
            {
                runOnUiThread(()-> Toast.makeText(ChangePickupAddressActivity.this, "Changed address successfully!", Toast.LENGTH_SHORT).show());
            }
            merchantIntent = new Intent(ChangePickupAddressActivity.this,MerchantActivity.class);
            merchantIntent.putExtra("fragment","profile");
            startActivity(merchantIntent);

        } catch (IOException e) {
            runOnUiThread(()-> Toast.makeText(ChangePickupAddressActivity.this, "Progress failed!", Toast.LENGTH_SHORT).show());
        }
    }
}