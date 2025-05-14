package com.prac.learning;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import android.app.AlertDialog;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.stripe.android.PaymentConfiguration;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class UpgradeActivity extends AppCompatActivity {

    Button starterBtn, intermediateBtn, advancedBtn;
    private PaymentSheet paymentSheet;
    private String clientSecret;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upgrade);

        PaymentConfiguration.init(
            getApplicationContext(),
            "pk_test_51Q2YsI2L4xRyYFjVgq77nPGCWSn2dXaAUbJ3cqpXLjrgb7rbVUA6kn9QDSPYak1AL3VZrn0TGkEFaA8pQwBKCQZo00SPKfViHY"
        );

        paymentSheet = new PaymentSheet(this, this::onPaymentSheetResult);

        starterBtn = findViewById(R.id.starterBtn);
        intermediateBtn = findViewById(R.id.intermediateBtn);
        advancedBtn = findViewById(R.id.advancedBtn);

        setupButton(starterBtn, "Starter");
        setupButton(intermediateBtn, "Intermediate");
        setupButton(advancedBtn, "Advanced");
    }

    private void setupButton(Button button, String tier) {
        button.setOnClickListener(v -> {
            animateButton(button);
            new AlertDialog.Builder(this)
                .setTitle("Upgrade Account")
                .setMessage("Confirm purchase of " + tier + " tier?")
                .setPositiveButton("Confirm", (dialog, which) -> {
                    Intent tierIntent = new Intent();
                    tierIntent.putExtra("tier", tier);
                    setIntent(tierIntent);
                    fetchPaymentIntentAndShowSheet(tier);
                })
                .setNegativeButton("Cancel", null)
                .show();
        });
    }


    private void animateButton(Button button) {
        ScaleAnimation anim = new ScaleAnimation(
            1f, 1.1f, 1f, 1.1f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        );
        anim.setDuration(200);
        anim.setRepeatCount(1);
        anim.setRepeatMode(Animation.REVERSE);
        button.startAnimation(anim);
    }

    private void fetchPaymentIntentAndShowSheet(String tier) {
        new Thread(() -> {
            try {
                int amount;
                switch (tier) {
                    case "Starter": amount = 99; break;
                    case "Intermediate": amount = 299; break;
                    case "Advanced": amount = 499; break;
                    default: amount = 99;
                }

                URL url = new URL("http://10.0.2.2:5001/create-payment-intent");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                String body = "{\"amount\": " + amount + ", \"currency\": \"usd\"}";
                OutputStream os = conn.getOutputStream();
                os.write(body.getBytes());
                os.close();

                Scanner scanner = new Scanner(conn.getInputStream());
                StringBuilder result = new StringBuilder();
                while (scanner.hasNext()) result.append(scanner.nextLine());
                scanner.close();

                JSONObject response = new JSONObject(result.toString());
                clientSecret = response.getString("clientSecret");

                runOnUiThread(() -> presentPaymentSheet());

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                    Toast.makeText(this, "❌ Failed to create payment intent", Toast.LENGTH_LONG).show()
                );
            }
        }).start();
    }

    private void presentPaymentSheet() {
        PaymentSheet.Configuration config = new PaymentSheet.Configuration.Builder("Innovate Learning Inc.")
            .googlePay(new PaymentSheet.GooglePayConfiguration(
                PaymentSheet.GooglePayConfiguration.Environment.Test,
                "US"
            ))
            .allowsDelayedPaymentMethods(true)
            .build();

        paymentSheet.presentWithPaymentIntent(clientSecret, config);
    }

    private void onPaymentSheetResult(PaymentSheetResult result) {
        if (result instanceof PaymentSheetResult.Completed) {

            String upgradedTier = getIntent().getStringExtra("tier");
            SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            prefs.edit().putString("tier", upgradedTier).apply();

            Toast.makeText(this, "✅ Payment successful!", Toast.LENGTH_LONG).show();


            Intent intent = new Intent(UpgradeActivity.this, ProfileActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP); // Avoid stacking
            intent.putExtra("username", prefs.getString("username", "Student"));
            intent.putExtra("email", prefs.getString("email", "unknown@email.com"));
            startActivity(intent);
            finish();

        } else if (result instanceof PaymentSheetResult.Canceled) {
            Toast.makeText(this, "❌ Payment canceled", Toast.LENGTH_SHORT).show();
        } else if (result instanceof PaymentSheetResult.Failed) {
            Toast.makeText(this, "❌ Payment failed", Toast.LENGTH_LONG).show();
        }
    }

}
