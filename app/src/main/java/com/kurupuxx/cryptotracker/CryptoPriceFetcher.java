package com.kurupuxx.cryptotracker;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.kurupuxx.cryptotracker.entity.CryptoPrice;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CryptoPriceFetcher {

    private static final String TAG = "CryptoPriceFetcher";
    private static final OkHttpClient httpClient = new OkHttpClient();

    public CompletableFuture<CryptoPrice> fetchCryptoPrice(String cryptoSymbol) {
        CompletableFuture<CryptoPrice> future = new CompletableFuture<>();

        String url = "https://min-api.cryptocompare.com/data/price?fsym=" + cryptoSymbol + "&tsyms=USD";
        Request request = new Request.Builder()
                .url(url)
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "API call failed: ", e);
                future.completeExceptionally(e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.e(TAG, "Unexpected code " + response);
                    future.completeExceptionally(new IOException("Unexpected response code: " + response.code()));
                    return;
                }

                try {
                    Gson gson = new Gson();
                    assert response.body() != null;
                    CryptoPrice cryptoPrice = gson.fromJson(response.body().string(), CryptoPrice.class);
                    future.complete(cryptoPrice);
                } catch (JsonSyntaxException e) {
                    Log.e(TAG, "JSON parsing error: ", e);
                    future.completeExceptionally(e);
                }
            }
        });

        return future;
    }
}
