package com.example.controlgastos_am_sp;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class ExchangeRates {

    @SerializedName("result")
    private String result;

    @SerializedName("base")
    private String base;

    @SerializedName("updated")
    private String updated;

    @SerializedName("rates")
    private Map<String, Double> rates;

    public String getResult() {
        return result;
    }

    public String getBase() {
        return base;
    }

    public String getUpdated() {
        return updated;
    }

    public Map<String, Double> getRates() {
        return rates;
    }
}
