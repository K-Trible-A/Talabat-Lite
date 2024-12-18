package com.kaaa.talabat_lite;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MerchantActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    MerchantHomeFragment merchantHomeFragment;
    MerchantOrdersFragment merchantOrdersFragment;
    MerchantProfileFragment merchantProfileFragment;
    ItemsSearchFragment itemsSearchFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_merchant);

        initUI();
        setupListeners();
        boolean openProfile = getIntent().getBooleanExtra("profile", false);
        if (openProfile) {
            // Open the Profile Fragment instead of the default Home Fragment
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, merchantProfileFragment)
                    .commit();
            // Set the BottomNavigationView to "Profile"
            bottomNavigationView.setSelectedItemId(R.id.profile);
        }

    }
    

    protected void initUI() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        merchantHomeFragment = new MerchantHomeFragment();
        merchantOrdersFragment = new MerchantOrdersFragment();
        merchantProfileFragment = new MerchantProfileFragment();
        itemsSearchFragment = new ItemsSearchFragment();


        if (merchantHomeFragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, merchantHomeFragment)
                    .commit();
        }
    }

    protected void setupListeners ()
    {

        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.home)
            {
                getSupportFragmentManager().beginTransaction().replace(R.id.container,merchantHomeFragment).commit();
                return true;
            }
            if (item.getItemId() == R.id.orders)
            {
                getSupportFragmentManager().beginTransaction().replace(R.id.container,merchantOrdersFragment).commit();
                return true;
            }
            if (item.getItemId() == R.id.profile)
            {
                getSupportFragmentManager().beginTransaction().replace(R.id.container,merchantProfileFragment).commit();
                return true;
            }
            if (item.getItemId() == R.id.search)
            {
                getSupportFragmentManager().beginTransaction().replace(R.id.container,itemsSearchFragment).commit();
                return true;
            }
            return false;
        });

    }
}
