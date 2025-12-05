package com.example.controlgastos_am_sp;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    public static String BASE_URL = " https://api.exchangerate-api.com/";
    private static Retrofit retrofit = null;
    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create()) // Permite mapear JSON a POJOs
                    .build();
        }
        return retrofit;
    }

    public static ApiService getExchangeService() {
        return getClient().create(ApiService.class);
    }
}
