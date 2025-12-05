package com.example.controlgastos_am_sp;

public interface ConversionRateCallback {
    void onSuccess(Double rate);

    /**
     * Llamado cuando la solicitud de la tasa de conversión falla.
     *
     * @param errorMsg Mensaje descriptivo del error.
     */
    void onFailure(String errorMsg);
}
