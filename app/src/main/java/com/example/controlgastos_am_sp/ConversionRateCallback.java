package com.example.controlgastos_am_sp;

public interface ConversionRateCallback {
    void onSuccess(Double rate);

    void onFailure(String errorMsg);
}
