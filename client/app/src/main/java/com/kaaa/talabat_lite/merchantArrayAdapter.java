package com.kaaa.talabat_lite;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;
import java.util.Vector;

public class merchantArrayAdapter extends ArrayAdapter<merchantView> {
    private Vector<merchantView> merchantsToView;
    private Context _context;

    // Constructor
    public merchantArrayAdapter(Context context, int resource, int textViewResourceId, List<merchantView> merchants) {
        super(context, resource, textViewResourceId, merchants);
        merchantsToView = (Vector<merchantView>) merchants;
        _context = context;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup listView) {
        ViewHolder viewHolder;

        if (convertView == null) {
            // Inflate the row layout
            LayoutInflater inflater = (LayoutInflater) _context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.merchant_list_view, listView, false);

            // Initialize the ViewHolder and find the views
            viewHolder = new ViewHolder();
            viewHolder.merchantPicture = convertView.findViewById(R.id.merchantImage);
            viewHolder.merchantName = convertView.findViewById(R.id.merchantName);
            viewHolder.merchantKeywords = convertView.findViewById(R.id.merchantKeywords);
            viewHolder.merchantRate = convertView.findViewById(R.id.merchantRate);
            viewHolder.star = convertView.findViewById(R.id.star);

            // Set the ViewHolder as the tag of the convertView
            convertView.setTag(viewHolder);
        } else {
            // Reuse the existing convertView and retrieve the ViewHolder
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // Set the data for the current position
        merchantView currentMerchant = merchantsToView.get(position);
        if (currentMerchant.getMerchantPicture()!=null) {
            viewHolder.merchantPicture.setImageBitmap(currentMerchant.getMerchantPicture());
        }
        viewHolder.merchantName.setText(currentMerchant.getMerchantName());
        viewHolder.merchantKeywords.setText(currentMerchant.getMerchantKeywords());
        viewHolder.merchantRate.setText(currentMerchant.getMerchantRate());

        return convertView;
    }

    // ViewHolder class to hold the views for each row item
    static class ViewHolder {
        ImageView merchantPicture;
        TextView merchantName;
        TextView merchantKeywords;
        TextView merchantRate;
        ImageView star;
    }
}
