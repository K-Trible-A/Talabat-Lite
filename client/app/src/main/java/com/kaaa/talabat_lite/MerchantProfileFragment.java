package com.kaaa.talabat_lite;

import static com.kaaa.talabat_lite.globals.GET_MERCHANT_DATA;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MerchantProfileFragment extends Fragment {

    EditText profileBusinessType, profileKeywords, profilePickupAddress,profileRating;
    TextView profileBusinessName;
    Button changeAddressButton;
    String businessName, type, keywords, pickupAddress;
    float rating;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    Intent changePickupAddressIntent;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_merchant_profile, container, false);
        initUI(rootView);
        setupListeners();
        return rootView;
    }
    protected void initUI(View rootView)
    {
      profileBusinessName = rootView.findViewById(R.id.profileBusinessName);
      profileBusinessType = rootView.findViewById(R.id.profileBusinessType);
      profileKeywords = rootView.findViewById(R.id.profileKeywords);
      profilePickupAddress = rootView.findViewById(R.id.profilePickupAddress);
      changeAddressButton = rootView.findViewById(R.id.changeAddressButton);
      profileRating = rootView.findViewById(R.id.profileRating);
    }
    protected void setupListeners()
    {
        fetchMerchantData();
        changeAddressButton.setOnClickListener(view -> new Thread(this::changePickupAddress).start());
    }
    private void changePickupAddress()
    {
          changePickupAddressIntent = new Intent(requireContext(), ChangePickupAddressActivity.class);
          changePickupAddressIntent.putExtra("currentAddress",pickupAddress);
          startActivity(changePickupAddressIntent);
    }
    private void getMerchantData ()
    {
        try
        {
            Log.d("MerchantProfileFragment", "Inside try block");
            socketHelper.getInstance().connect();
            socketHelper.getInstance().sendInt(GET_MERCHANT_DATA);
            socketHelper.getInstance().sendInt(globals.userId);
            Log.d("MerchantProfileFragment", "sending " + globals.userId);
            businessName = socketHelper.getInstance().recvString();
            type = socketHelper.getInstance().recvString();
            keywords = socketHelper.getInstance().recvString();
            pickupAddress = socketHelper.getInstance().recvString();
            rating = socketHelper.getInstance().recvFloat();
            int ok = socketHelper.getInstance().recvInt();
            Log.d("MerchantProfileFragment" , "Rate = " +  rating);
            socketHelper.getInstance().close();
        } catch (IOException e) {

            Log.e("MerchantProfileFragment", "Fatal!!",e);
        }
    }

    private void fetchMerchantData() {
        executor.execute(() -> {
            getMerchantData();
            mainHandler.post(this::updateUI);
        });
    }
    private void updateUI()
    {
        profileBusinessName.setText(businessName);
        profileBusinessType.setText(type);
        profileKeywords.setText(keywords);
        profilePickupAddress.setText(pickupAddress);
        profileRating.setText(String.valueOf(rating));
    }


}