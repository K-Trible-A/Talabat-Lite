package com.kaaa.talabat_lite;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class CourierActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    CourierOrdersFragment courierOrdersFragment;
    CourierProfileFragment courierProfileFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_courier);
        initUI();
        setupListeners();
    }

    protected void initUI() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        courierOrdersFragment = new CourierOrdersFragment();
        courierProfileFragment = new CourierProfileFragment();

        if (courierOrdersFragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, courierOrdersFragment)
                    .commit();
        }
    }
    protected void setupListeners() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.orders) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, courierOrdersFragment)
                        .commit();
                return true;
            }
            if (item.getItemId() == R.id.profile) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, courierProfileFragment)
                        .commit();
                return true;
            }
            return false;
        });
    }
}
