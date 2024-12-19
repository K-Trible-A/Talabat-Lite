package com.kaaa.talabat_lite;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class CustomerActivity extends AppCompatActivity {
    private final TopRated_Merchants_Fragment topRatedFragment = new TopRated_Merchants_Fragment();
    private final CustomerProfileFragment customerProfileFragment = new CustomerProfileFragment();
    private final MerchantsSearchFragment merchantsSearchFragment = new MerchantsSearchFragment();
    private final CustomerOrdersFragment customerOrdersFragment = new CustomerOrdersFragment();
    private static final String TAG = "CustomerActivity"; // Log tag for debugging

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer); // Ensure this layout is correct
        setupListeners();
    }

    private void setupListeners() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        if (bottomNavigationView == null) {
            Log.e(TAG, "BottomNavigationView not found in layout.");
            Toast.makeText(this, "Navigation setup failed.", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean openOrders = getIntent().getBooleanExtra("orders", false);

        if (openOrders) {
            // Open the Orders Fragment instead of the default Home Fragment
            loadFragment(customerOrdersFragment, "OrdersFragment");
            bottomNavigationView.setSelectedItemId(R.id.nav_Orders);
        } else if (getSupportFragmentManager().findFragmentById(R.id.container) == null) {
            // Default fragment to load
            loadFragment(topRatedFragment, "TopRatedFragment");
        }

        // Set a listener for BottomNavigationView item selection
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                loadFragment(topRatedFragment, "TopRatedFragment");
                return true;
            } else if (item.getItemId() == R.id.nav_search) {
                loadFragment(merchantsSearchFragment, "SearchFragment");
                return true;
            } else if (item.getItemId() == R.id.nav_profile) {
                loadFragment(customerProfileFragment, "ProfileFragment");
                return true;
            } else if (item.getItemId() == R.id.nav_Orders) {
                loadFragment(customerOrdersFragment, "OrdersFragment");
                return true;
            } else {
                return false;
            }
        });
    }

    private void loadFragment(androidx.fragment.app.Fragment fragment, String tag) {
        if (getSupportFragmentManager().findFragmentByTag(tag) == fragment) {
            Log.d(TAG, "Fragment already loaded: " + tag);
            return;
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, fragment, tag)
                .commit();
    }
}
