package com.example.controlgastos_am_sp;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ApiService {
    @GET("v4/latest/{baseCurrency}")
    Call<ExchangeRates> getLatestRates(
            @Path("baseCurrency") String baseCurrency
    );
}
