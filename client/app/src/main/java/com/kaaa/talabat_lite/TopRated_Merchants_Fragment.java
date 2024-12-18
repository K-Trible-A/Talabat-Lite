package com.kaaa.talabat_lite;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class TopRated_Merchants_Fragment extends Fragment {
    TextView Merch_1_Name, Merch_2_Name, Merch_3_Name;
    TextView Merch_1_Rate, Merch_2_Rate, Merch_3_Rate;
    Button Rest,phar,Groc,cartView;
    ImageView merch1, merch2, merch3;
    Bitmap img1, img2, img3;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View ratedMerchants = inflater.inflate(R.layout.fragment_top_rated__merchants_, container, false);
        initUi(ratedMerchants);
        Rest.setOnClickListener(v -> {
            // Handle Restaurant button click
            // For example, navigate to a restaurant list or display a Toast
            Activity activity = requireActivity();
            Intent intent = new Intent(activity, CategorieActivity.class);
            intent.putExtra("businessType", 2); // pass businessType as int
            startActivity(intent);
        });
        phar.setOnClickListener(v -> {
            // Handle Pharmacy  button click
            Activity activity = requireActivity();
            Intent intent = new Intent(activity, CategorieActivity.class);
            intent.putExtra("businessType", 3); // pass businessType as int
            startActivity(intent);
        });

        Groc.setOnClickListener(v -> {
            // Handle Grocary button click
            Activity activity = requireActivity();
            Intent intent = new Intent(activity, CategorieActivity.class);
            intent.putExtra("businessType", 1); // pass businessType as int
            startActivity(intent);
        });
        cartView.setOnClickListener(v -> {
            Activity activity = requireActivity();
            Intent intent = new Intent(activity, CartActivity.class);
            startActivity(intent);
            // Handle ViewCart button click
        });
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
        Rest = ratedMerchants.findViewById(R.id.btnRestaurant);
        Groc = ratedMerchants.findViewById(R.id.btnGrocery);
        phar = ratedMerchants.findViewById(R.id.btnPharmacy);
        cartView = ratedMerchants.findViewById(R.id.btnViewCart);
    }
    private Bitmap getProfileImg(int id) throws IOException {
        Bitmap temp;
        try {
            // Create URL connection
            URL url = new URL(globals.serverURL + "/get_profile_image_merchId/" + id);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setDoInput(true);

            int responseCode = conn.getResponseCode();
            // Check the response code
            if (responseCode != HttpURLConnection.HTTP_OK) {

                return null;
            }
            // Read the response
            InputStream inputStream = conn.getInputStream();
            temp = BitmapFactory.decodeStream(inputStream);
            inputStream.close();
        } catch (IOException e) {
            return null;
        }
        return temp;
    }
    private void getTopRated() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {

            try {
                // Create URL connection
                URL url = new URL(globals.serverURL + "/customer/getTopRatedMerchants/" + globals.userId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                int responseCode = conn.getResponseCode();
                // Check the response code
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    Log.e("CustomerProfileFragment", "Error retrieving customer data");
                    return;
                }
                // Read the response
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
                in.close();
                // Parse the response JSON
                JSONObject jsonResponse = new JSONObject(response.toString());
                String Name1 = jsonResponse.getString("Name1");
                String Name2 = jsonResponse.getString("Name2");
                String Name3 = jsonResponse.getString("Name3");

                String Rate1 = jsonResponse.getString("Rate1");
                String Rate2 = jsonResponse.getString("Rate2");
                String Rate3 = jsonResponse.getString("Rate3");

                int merch1_id = jsonResponse.getInt("Id1");
                int merch2_id = jsonResponse.getInt("Id2");
                int merch3_id = jsonResponse.getInt("Id3");
                img1 = getProfileImg(merch1_id);
                img2 = getProfileImg(merch2_id);
                img3 = getProfileImg(merch3_id);


                if(conn.getResponseCode() == HttpURLConnection.HTTP_OK){
                    // Update UI only on the main thread
                    requireActivity().runOnUiThread(() -> {
                        DecimalFormat decimalFormat = new DecimalFormat("0.0");

                        Merch_1_Name.setText(" ");
                        Merch_1_Rate.setText(" ");
                        Merch_2_Name.setText(" ");
                        Merch_2_Rate.setText(" ");
                        Merch_3_Name.setText(" ");
                        Merch_3_Rate.setText(" ");
                        merch1.setVisibility(View.INVISIBLE);
                        merch2.setVisibility(View.INVISIBLE);
                        merch3.setVisibility(View.INVISIBLE);

                        if (!Name1.equals("there is no other merchants")) {
                            if (img1 != null)
                            {
                                merch1.setImageBitmap(img1);
                            }
                            Merch_1_Name.setText(Name1);
                            Merch_1_Rate.setText(formatRate(Rate1, decimalFormat));
                            merch1.setVisibility(View.VISIBLE);
                            merch1.setOnClickListener(v -> {
                                Activity activity = requireActivity();
                                Intent intent = new Intent(activity, CustomerViewOfMerchant.class);
                                intent.putExtra("merch_id", merch1_id); // pass merhcid as string
                                startActivity(intent);
                                // Handle the click event here
                                // Add your desired action, e.g., open a new activity, make a network call, etc.
                            });
                            Merch_1_Name.setOnClickListener(v -> {
                                Activity activity = requireActivity();
                                Intent intent = new Intent(activity, CustomerViewOfMerchant.class);
                                intent.putExtra("merch_id", merch1_id); // pass merhcid as string
                                startActivity(intent);
                                // Handle the click event here
                                // Add your desired action, e.g., open a new activity, make a network call, etc.
                            });
                            Merch_1_Rate.setOnClickListener(v -> {
                                Activity activity = requireActivity();
                                Intent intent = new Intent(activity, CustomerViewOfMerchant.class);
                                intent.putExtra("merch_id", merch1_id); // pass merhcid as string
                                startActivity(intent);
                                // Handle the click event here
                                // Add your desired action, e.g., open a new activity, make a network call, etc.
                            });
                        }
                        if (!Name2.equals("there is no other merchants")) {
                            if (img2 != null)
                            {
                                merch2.setImageBitmap(img2);
                            }
                            Merch_2_Name.setText(Name2);
                            Merch_2_Rate.setText(formatRate(Rate2, decimalFormat));
                            merch2.setVisibility(View.VISIBLE);
                            merch2.setOnClickListener(v -> {
                                Activity activity = requireActivity();
                                Intent intent = new Intent(activity, CustomerViewOfMerchant.class);
                                intent.putExtra("merch_id", merch2_id); // pass merhcid as string
                                startActivity(intent);
                                // Handle the click event here
                                // Add your desired action, e.g., open a new activity, make a network call, etc.
                            });
                            Merch_2_Name.setOnClickListener(v -> {
                                Activity activity = requireActivity();
                                Intent intent = new Intent(activity, CustomerViewOfMerchant.class);
                                intent.putExtra("merch_id", merch1_id); // pass merhcid as string
                                startActivity(intent);
                                // Handle the click event here
                                // Add your desired action, e.g., open a new activity, make a network call, etc.
                            });
                            Merch_2_Rate.setOnClickListener(v -> {
                                Activity activity = requireActivity();
                                Intent intent = new Intent(activity, CustomerViewOfMerchant.class);
                                intent.putExtra("merch_id", merch1_id); // pass merhcid as string
                                startActivity(intent);
                                // Handle the click event here
                                // Add your desired action, e.g., open a new activity, make a network call, etc.
                            });
                        }
                        if (!Name3.equals("there is no other merchants")) {
                            if (img3 != null)
                            {
                                merch3.setImageBitmap(img3);
                            }
                            Merch_3_Name.setText(Name3);
                            Merch_3_Rate.setText(formatRate(Rate3, decimalFormat));
                            merch3.setVisibility(View.VISIBLE);
                            merch3.setOnClickListener(v -> {
                                Activity activity = requireActivity();
                                Intent intent = new Intent(activity, CustomerViewOfMerchant.class);
                                intent.putExtra("merch_id", merch3_id); // pass merhcid as string
                                startActivity(intent);
                                // Handle the click event here
                                // Add your desired action, e.g., open a new activity, make a network call, etc.
                            });
                            Merch_3_Name.setOnClickListener(v -> {
                                Activity activity = requireActivity();
                                Intent intent = new Intent(activity, CustomerViewOfMerchant.class);
                                intent.putExtra("merch_id", merch1_id); // pass merhcid as string
                                startActivity(intent);
                                // Handle the click event here
                                // Add your desired action, e.g., open a new activity, make a network call, etc.
                            });
                            Merch_3_Rate.setOnClickListener(v -> {
                                Activity activity = requireActivity();
                                Intent intent = new Intent(activity, CustomerViewOfMerchant.class);
                                intent.putExtra("merch_id", merch1_id); // pass merhcid as string
                                startActivity(intent);
                                // Handle the click event here
                                // Add your desired action, e.g., open a new activity, make a network call, etc.
                            });
                        }

                    });
                }
                else{
                    Log.e("Top3RatedMerchants", conn.getResponseMessage());
                }
            }catch (IOException e) {
                Log.e("Top3RatedMerchants", "Failed to read response");
            } catch (JSONException e) {
                Log.e("Top3RatedMerchants", "Json error");
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
