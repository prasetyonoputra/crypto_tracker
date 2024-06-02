package com.kurupuxx.cryptotracker;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.kurupuxx.cryptotracker.entity.CryptoPrice;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private static final String[] paths = {"ETH", "BTC", "ARB"};
    private TextView textPrice;
    private int selectedIndexCrypto = 0;
    private Handler handler;
    private CryptoPriceFetcher cryptoPriceFetcher;
    private Runnable fetchRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Spinner spinner = findViewById(R.id.crypto_list);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this,
                android.R.layout.simple_spinner_item, paths);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        textPrice = findViewById(R.id.crypto_selected_price);
        cryptoPriceFetcher = new CryptoPriceFetcher();
        handler = new Handler();

        fetchRunnable = new Runnable() {
            @Override
            public void run() {
                fetchCryptoData();
                handler.postDelayed(this, 5000); // Fetch every 5 seconds
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.post(fetchRunnable);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(fetchRunnable);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
        selectedIndexCrypto = position;
        fetchCryptoData();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Do nothing
    }

    private void fetchCryptoData() {
        String selectedCrypto = paths[selectedIndexCrypto];
        cryptoPriceFetcher.fetchCryptoPrice(selectedCrypto).thenAccept(this::updateUIWithCryptoData);
    }

    private void updateUIWithCryptoData(CryptoPrice cryptoPrice) {
        runOnUiThread(() -> {
            if (cryptoPrice != null) {
                String textToDisplay = cryptoPrice.getUSD() + " USD";
                textPrice.setText(textToDisplay);
            }
        });
    }
}
