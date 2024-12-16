package com.kaaa.talabat_lite;

public class merchantView {
    private final String merchantName;
    private final String merchantKeywords;
    private final String merchantRate;
    private final int merchantPicture;

    public merchantView(String merchantName, String merchantKeywords, String merchantRate, int merchantPicture) {
        this.merchantName = merchantName;
        this.merchantKeywords = merchantKeywords;
        this.merchantRate = merchantRate;
        this.merchantPicture = merchantPicture;
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

    public int getMerchantPicture() {
        return merchantPicture;
    }
}
