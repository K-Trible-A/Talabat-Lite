package com.kaaa.talabat_lite;

import static com.kaaa.talabat_lite.globals.ADD_MERCHANT;
import static com.kaaa.talabat_lite.globals.ADD_USER;

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

public class MerchantRegistrationActivity extends AppCompatActivity {

    EditText businessName ,keywords ,pickupAddress ,nationalID;
    String cardNumberStr = "";
    String expiryDateStr = "";
    String CVVStr = "";
    final String[] selectedRadio = new String[1];
    Button submit , addCard;
    int businessType = -1;
    RadioGroup radioType;
    Intent cardIntent;
    Intent loginIntent;

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
        radioType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.grocery)
                businessType = 1;
            else if (checkedId == R.id.restaurant)
                businessType = 2;
            else if (checkedId == R.id.pharmacy)
                businessType = 3;
            RadioButton selected = findViewById(checkedId);
            selectedRadio[0] = selected.getText().toString();
        });
        addCard.setOnClickListener(v -> {
            cardIntent = new Intent(MerchantRegistrationActivity.this, CardActivity.class);
            cardActivityResultLauncher.launch(cardIntent);

        });

        submit.setOnClickListener(view -> new Thread(this::addMerchant).start());

    }


    private void addMerchant()
    {
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
        if (cardNumberStr.isEmpty() || CVVStr.isEmpty() || expiryDateStr.isEmpty())
        {
            runOnUiThread(() -> Toast.makeText(MerchantRegistrationActivity.this, "Fill card information!", Toast.LENGTH_SHORT).show());
            return;
        }

        try {
            socketHelper.getInstance().connect();
            Intent intent = getIntent();
            String name = intent.getStringExtra("userName");
            String phone = intent.getStringExtra("userPhone");
            String email = intent.getStringExtra("userEmail");
            String password = intent.getStringExtra("userPassword");
            String country = intent.getStringExtra("userCountry");
            String city = intent.getStringExtra("userCity");
            int accountType=intent.getIntExtra("accountType",0);
            socketHelper.getInstance().sendInt(ADD_USER);
            if (name != null) {
                socketHelper.getInstance().sendString(name);
            }
            if (phone != null) {
                socketHelper.getInstance().sendString(phone);
            }
            if (email != null) {
                socketHelper.getInstance().sendString(email);
            }
            if (password != null) {
                socketHelper.getInstance().sendString(password);
            }
            if (country != null) {
                socketHelper.getInstance().sendString(country);
            }
            if (city != null) {
                socketHelper.getInstance().sendString(city);
            }
            socketHelper.getInstance().sendInt(accountType);
            int ok1 = socketHelper.getInstance().recvInt();
            if(ok1==1) {
                int userId=socketHelper.getInstance().recvInt();
                socketHelper.getInstance().close();
                socketHelper.getInstance().connect();
                socketHelper.getInstance().sendInt(ADD_MERCHANT);
                socketHelper.getInstance().sendInt(userId);
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
                    loginIntent = new Intent(MerchantRegistrationActivity.this,LoginActivity.class);
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(loginIntent);
                }
                socketHelper.getInstance().close();
            }
            else {
                socketHelper.getInstance().close();
                runOnUiThread(() -> Toast.makeText(MerchantRegistrationActivity.this, "This email or phoneNumber has already registered", Toast.LENGTH_SHORT).show());
            }

        } catch (IOException e) {
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
                assert data != null;
                cardNumberStr = data.getStringExtra("cardNumber");
                expiryDateStr = data.getStringExtra("expiryDate");
                CVVStr = data.getStringExtra("CVV");
            }
        }
    }
}
