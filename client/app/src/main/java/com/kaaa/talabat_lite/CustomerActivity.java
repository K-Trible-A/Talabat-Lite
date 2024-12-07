package com.kaaa.talabat_lite;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class CustomerActivity extends AppCompatActivity  {
    private Button Phar,Groc,Rest;
    TopRated_Merchants_Fragment fragment = new TopRated_Merchants_Fragment();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer); // Link to your XML layout
        getSupportFragmentManager().beginTransaction().replace(R.id.container,fragment).commit();
        initiUi();
        setupListeners();
    }
    private void setupListeners(){
        Phar.setOnClickListener(view -> new Thread(this::goto_Pharmacy).start());
        Rest.setOnClickListener(view -> new Thread(this::goto_Restaurant).start());
        Groc.setOnClickListener(view -> new Thread(this::goto_Grocery).start());
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Set a listener for item selection
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if(item.getItemId()==R.id.nav_home) {
                Toast.makeText(this, "Home Selected", Toast.LENGTH_SHORT).show();
                return true;
            }
            else if(item.getItemId()==R.id.nav_search) {
                Toast.makeText(this, "Search Selected", Toast.LENGTH_SHORT).show();
                return true;
            }
            else if(item.getItemId()==R.id.nav_profile)
            {
                    Toast.makeText(this, "Profile Selected", Toast.LENGTH_SHORT).show();
                    return true;
            }
            return false;
        });
    }
    private void goto_Restaurant()
    {
        runOnUiThread(()->Toast.makeText(CustomerActivity.this, "Restaurant Selected", Toast.LENGTH_SHORT).show());
    }
    private void goto_Grocery()
    {
           runOnUiThread(()->Toast.makeText(CustomerActivity.this, "Grocery Selected", Toast.LENGTH_SHORT).show());
    }
    private void goto_Pharmacy()
    {
        runOnUiThread(()->Toast.makeText(CustomerActivity.this, "Pharmacy Selected", Toast.LENGTH_SHORT).show());
    }
    protected void initiUi()
    {
        Rest = findViewById(R.id.btnRestaurant);
        Groc = findViewById(R.id.btnGrocery);
        Phar = findViewById(R.id.btnPharmacy);
    }
}