package com.kaaa.talabat_lite;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MerchantActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    MerchantHomeFragment merchantHomeFragment;
    MerchantOrdersFragment merchantOrdersFragment;
    MerchantProfileFragment merchantProfileFragment;
    MerchantSearchFragment merchantSearchFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_merchant);
        initUI();
        setupListeners();

        // Check the passed fragment name and display the correct fragment
        String fragmentName = getIntent().getStringExtra("fragment");
        if (fragmentName != null) {
            switch (fragmentName) {
                case "home":
                    loadFragment(merchantHomeFragment);
                    bottomNavigationView.setSelectedItemId(R.id.home);
                    break;
                case "orders":
                    loadFragment(merchantOrdersFragment);
                    bottomNavigationView.setSelectedItemId(R.id.orders);
                    break;
                case "profile":
                    loadFragment(merchantProfileFragment);
                    bottomNavigationView.setSelectedItemId(R.id.profile);
                    break;
                case "search":
                    loadFragment(merchantSearchFragment);
                    bottomNavigationView.setSelectedItemId(R.id.search);
                    break;
                default:
                    loadFragment(merchantHomeFragment);
                    bottomNavigationView.setSelectedItemId(R.id.home);
                    break;
            }
        }
    }



    protected void initUI() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        merchantHomeFragment = new MerchantHomeFragment();
        merchantOrdersFragment = new MerchantOrdersFragment();
        merchantProfileFragment = new MerchantProfileFragment();
        merchantSearchFragment = new MerchantSearchFragment();

        // Load home fragment initially
        loadFragment(merchantHomeFragment);
    }
    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }

    protected void setupListeners ()
    {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.home)
            {
                loadFragment(merchantHomeFragment);
                return true;
            }
            if (item.getItemId() == R.id.orders)
            {
                loadFragment(merchantOrdersFragment);
                return true;
            }
            if (item.getItemId() == R.id.profile)
            {
                loadFragment(merchantProfileFragment);
                return true;
            }
            if (item.getItemId() == R.id.search)
            {
                loadFragment(merchantSearchFragment);
                return true;
            }
            return false;
        });

    }
}