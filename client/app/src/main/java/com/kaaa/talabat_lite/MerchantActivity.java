package com.kaaa.talabat_lite;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MerchantActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    MerchantHomeFragment merchantHomeFragment = new MerchantHomeFragment();
    MerchantItemsFragment merchantItemsFragment;
    MerchantProfileFragment merchantProfileFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_merchant);
        initUI();
        setupListeners();
    }
    protected void initUI()
    {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        merchantItemsFragment = new MerchantItemsFragment();
        merchantProfileFragment = new MerchantProfileFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.container,merchantHomeFragment).commit();

    }
    protected void setupListeners ()
    {

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.home)
                {
                    getSupportFragmentManager().beginTransaction().replace(R.id.container,merchantHomeFragment).commit();
                    return true;
                }
                if (item.getItemId() == R.id.items)
                {
                    getSupportFragmentManager().beginTransaction().replace(R.id.container,merchantItemsFragment).commit();
                    return true;
                }
                if (item.getItemId() == R.id.profile)
                {
                    getSupportFragmentManager().beginTransaction().replace(R.id.container,merchantProfileFragment).commit();
                    return true;
                }
                return false;
            }
        });

    }
}