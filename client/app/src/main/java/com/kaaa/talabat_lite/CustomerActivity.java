package com.kaaa.talabat_lite;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class CustomerActivity extends AppCompatActivity  {
    public TopRated_Merchants_Fragment top_rated_fragment = new TopRated_Merchants_Fragment();
    public CustomerProfileFragment customer_profile = new CustomerProfileFragment();
    public MerchantsSearchFragment merchantsSearchFragment = new MerchantsSearchFragment();
    public CustomerOrdersFragment customerOrdersFragment = new CustomerOrdersFragment();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_customer); // Link to your XML layout
        if (getSupportFragmentManager().findFragmentById(R.id.container) == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, top_rated_fragment)
                    .commit();
        }
        setupListeners();
    }
    private void setupListeners(){
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Set a listener for item selection
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if(item.getItemId()==R.id.nav_home) {
                getSupportFragmentManager().beginTransaction().replace(R.id.container, top_rated_fragment).commit();
                return true;
            }
            else if(item.getItemId()==R.id.nav_search) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, merchantsSearchFragment)
                        .commit();
                return true;
            }
            else if(item.getItemId()==R.id.nav_profile)
            {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, customer_profile)
                        .commit();
                return true;
            }
            else if(item.getItemId()==R.id.nav_Orders)
            {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, customerOrdersFragment)
                        .commit();
                return true;
            }
            return false;
        });
    }
}