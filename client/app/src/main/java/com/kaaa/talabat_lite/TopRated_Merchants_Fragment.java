package com.kaaa.talabat_lite;


import android.os.Bundle;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.io.IOException;


public class TopRated_Merchants_Fragment extends Fragment {
    TextView Merch_1_Name,Merch_2_Name,Merch_3_Name;
    TextView Merch_1_Rate,Merch_2_Rate,Merch_3_Rate;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View ratedMerchants=inflater.inflate(R.layout.fragment_top_rated__merchants_, container, false);
        initUi(ratedMerchants);
        setupListeners();
        return ratedMerchants;
    }
    private void initUi(View ratedMerchants)
    {
        Merch_1_Name = ratedMerchants.findViewById(R.id.merchant1Name);
        Merch_2_Name = ratedMerchants.findViewById(R.id.merchant2Name);
        Merch_3_Name = ratedMerchants.findViewById(R.id.merchant3Name);
        Merch_1_Rate = ratedMerchants.findViewById(R.id.merchant1Rate);
        Merch_2_Rate = ratedMerchants.findViewById(R.id.merchant2Rate);
        Merch_3_Rate = ratedMerchants.findViewById(R.id.merchant3Rate);
    }
    private void setupListeners()
    {
        getTopRated();
    }
    private void getTopRated()
    {
        try
        {
            socketHelper.getInstance().connect();
            socketHelper.getInstance().sendInt(globals.GET_TOP_RATED_MERCHANTS);
            socketHelper.getInstance().sendInt(globals.userId);
            String Name1 = socketHelper.getInstance().recvString();
            String Rate1 = socketHelper.getInstance().recvString();
            String Name2 = socketHelper.getInstance().recvString();
            String Rate2 = socketHelper.getInstance().recvString();
            String Name3 = socketHelper.getInstance().recvString();
            String Rate3 = socketHelper.getInstance().recvString();
            int ok = socketHelper.getInstance().recvInt();
            if(ok==1)
            {
            Merch_1_Name.setText(Name1);
            Merch_1_Rate.setText(Rate1);
            Merch_2_Name.setText(Name2);
            Merch_2_Rate.setText(Rate2);
            Merch_3_Name.setText(Name3);
            Merch_3_Rate.setText(Rate3);
            }
            socketHelper.getInstance().close();
        } catch (IOException e) {
            Log.e("TopRated_Merchants_Fragment", "Fatal!!",e);
        }
    }
}