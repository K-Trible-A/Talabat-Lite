package com.kaaa.talabat_lite;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class TopRated_Merchants_Fragment extends Fragment {
    TextView Merch_1_Name, Merch_2_Name, Merch_3_Name;
    TextView Merch_1_Rate, Merch_2_Rate, Merch_3_Rate;
    ImageView merch1, merch2, merch3;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View ratedMerchants = inflater.inflate(R.layout.fragment_top_rated__merchants_, container, false);
        initUi(ratedMerchants);
        getTopRated();
        return ratedMerchants;
    }

    private void initUi(View ratedMerchants) {
        Merch_1_Name = ratedMerchants.findViewById(R.id.merchant1Name);
        Merch_2_Name = ratedMerchants.findViewById(R.id.merchant2Name);
        Merch_3_Name = ratedMerchants.findViewById(R.id.merchant3Name);
        Merch_1_Rate = ratedMerchants.findViewById(R.id.merchant1Rate);
        Merch_2_Rate = ratedMerchants.findViewById(R.id.merchant2Rate);
        Merch_3_Rate = ratedMerchants.findViewById(R.id.merchant3Rate);
        merch1 = ratedMerchants.findViewById(R.id.merchant1Icon);
        merch2 = ratedMerchants.findViewById(R.id.merchant2Icon);
        merch3 = ratedMerchants.findViewById(R.id.merchant3Icon);
    }
    private void getTopRated() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                socketHelper.getInstance().connect();
                socketHelper.getInstance().sendInt(globals.GET_TOP_RATED_MERCHANTS);
                socketHelper.getInstance().sendInt(globals.userId);
                String Name1 = socketHelper.getInstance().recvString();
                String Rate1 = socketHelper.getInstance().recvString();
                String Name2 = socketHelper.getInstance().recvString();
                String Rate2 = socketHelper.getInstance().recvString();
                String Name3 = socketHelper.getInstance().recvString();
                String Rate3 = socketHelper.getInstance().recvString();
                int merch1_id=socketHelper.getInstance().recvInt();
                int merch2_id=socketHelper.getInstance().recvInt();
                int merch3_id=socketHelper.getInstance().recvInt();
                int ok = socketHelper.getInstance().recvInt();
                socketHelper.getInstance().close();

                // Update UI only on the main thread
                requireActivity().runOnUiThread(() -> {
                    DecimalFormat decimalFormat = new DecimalFormat("#.#");

                    Merch_1_Name.setText(" ");
                    Merch_1_Rate.setText(" ");
                    Merch_2_Name.setText(" ");
                    Merch_2_Rate.setText(" ");
                    Merch_3_Name.setText(" ");
                    Merch_3_Rate.setText(" ");
                    merch1.setVisibility(View.INVISIBLE);
                    merch2.setVisibility(View.INVISIBLE);
                    merch3.setVisibility(View.INVISIBLE);

                    if (ok == 1) {
                        if (!Name1.equals("there is no other merchants")) {
                            Merch_1_Name.setText(Name1);
                            Merch_1_Rate.setText(formatRate(Rate1, decimalFormat));
                            merch1.setVisibility(View.VISIBLE);
                            merch1.setOnClickListener(v -> {
                                Activity activity = requireActivity();
                                Intent intent = new Intent(activity, CustomerViewofMerchant.class);
                                intent.putExtra("merch_id", merch1_id); // pass merhcid as string
                                startActivity(intent);
                                // Handle the click event here
                                // Add your desired action, e.g., open a new activity, make a network call, etc.
                            });
                        }
                        if (!Name2.equals("there is no other merchants")) {
                            Merch_2_Name.setText(Name2);
                            Merch_2_Rate.setText(formatRate(Rate2, decimalFormat));
                            merch2.setVisibility(View.VISIBLE);
                            merch2.setOnClickListener(v -> {
                                Activity activity = requireActivity();
                                Intent intent = new Intent(activity, CustomerViewofMerchant.class);
                                intent.putExtra("merch_id", merch2_id); // pass merhcid as string
                                startActivity(intent);
                                // Handle the click event here
                                // Add your desired action, e.g., open a new activity, make a network call, etc.
                            });
                        }
                        if (!Name3.equals("there is no other merchants")) {
                            Merch_3_Name.setText(Name3);
                            Merch_3_Rate.setText(formatRate(Rate3, decimalFormat));
                            merch3.setVisibility(View.VISIBLE);
                            merch3.setOnClickListener(v -> {
                                Activity activity = requireActivity();
                                Intent intent = new Intent(activity, CustomerViewofMerchant.class);
                                intent.putExtra("merch_id", merch2_id); // pass merhcid as string
                                startActivity(intent);
                                // Handle the click event here
                                // Add your desired action, e.g., open a new activity, make a network call, etc.
                            });
                        }
                    }
                });
            } catch (IOException e) {
                Log.e("TopRated_Merchants_Fragment", "Connection Error", e);
            }
        });
    }

    private String formatRate(String rate, DecimalFormat decimalFormat) {
        try {
            float rateValue = Float.parseFloat(rate);
            return decimalFormat.format(rateValue);
        } catch (NumberFormatException e) {
            Log.e("TopRated_Merchants_Fragment", "Invalid rate format", e);
            return rate; // Return the original value if parsing fails
        }
    }
}