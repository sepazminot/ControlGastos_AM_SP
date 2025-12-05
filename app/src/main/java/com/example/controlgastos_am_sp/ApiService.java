package com.example.controlgastos_am_sp;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ApiService {
    // Define el endpoint: "v4/latest/{baseCurrency}"
    // La {baseCurrency} se reemplazará por el valor que pases como argumento.
    @GET("v4/latest/{baseCurrency}")
    Call<ExchangeRates> getLatestRates(
            @Path("baseCurrency") String baseCurrency // Ejemplo: "USD", "EUR", "COP"
    );
}
