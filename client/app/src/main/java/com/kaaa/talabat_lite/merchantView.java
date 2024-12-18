package com.kaaa.talabat_lite;

import android.graphics.Bitmap;

public class merchantView {
    private final String merchantName;
    private final String merchantKeywords;
    private final String merchantRate;

    private final Bitmap merchantPicture;

    public merchantView(String merchantName, String merchantKeywords, String merchantRate, Bitmap merchantPicture) {
    private final int merchantPicture;
    private final int merchantId;

    public merchantView(String merchantName, String merchantKeywords, String merchantRate, int merchantPicture, int merchantId) {
        this.merchantName = merchantName;
        this.merchantKeywords = merchantKeywords;
        this.merchantRate = merchantRate;
        this.merchantPicture = merchantPicture;
        this.merchantId = merchantId;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public String getMerchantKeywords() {
        return merchantKeywords;
    }

    public String getMerchantRate() {
        return merchantRate;
    }
    public int getMerchantId(){
        return merchantId;
    }

    public Bitmap getMerchantPicture() {
        return merchantPicture;
    }
}
