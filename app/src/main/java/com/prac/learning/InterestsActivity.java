package com.prac.learning;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.prac.learning.ApiClient;
import com.prac.learning.ApiService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InterestsActivity extends AppCompatActivity {

    private GridLayout interestsContainer;
    private Button nextBtn;
    private List<Button> interestButtons = new ArrayList<>();
    private static final int MAX_SELECTION = 10;

    DatabaseHelper dbHelper;
    String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interests);

        interestsContainer = findViewById(R.id.interestsContainer);
        nextBtn = findViewById(R.id.nextBtn);

        dbHelper = new DatabaseHelper(this);
        username = getIntent().getStringExtra("username");

        fetchInterestsFromApi();

        nextBtn.setOnClickListener(v -> {
            ArrayList<String> selected = new ArrayList<>();
            for (Button btn : interestButtons) {
                if (btn.getTag() != null && (Boolean) btn.getTag()) {
                    selected.add(btn.getText().toString());
                }
            }

            if (selected.size() == 0) {
                Toast.makeText(this, "Please select at least one interest", Toast.LENGTH_SHORT).show();
            } else if (selected.size() > MAX_SELECTION) {
                Toast.makeText(this, "Please select up to 10 interests", Toast.LENGTH_SHORT).show();
            } else {
                // Save selected interests to DB
                String interestCsv = String.join(",", selected);
                dbHelper.updateUserInterests(username, interestCsv);

                // Go to Dashboard
                Intent intent = new Intent(InterestsActivity.this, DashboardActivity.class);
                intent.putExtra("username", username);
                intent.putStringArrayListExtra("interests", selected);
                startActivity(intent);
                finish();
            }
        });
    }

    private void fetchInterestsFromApi() {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<InterestResponse> call = apiService.getInterests();

        call.enqueue(new Callback<InterestResponse>() {
            @Override
            public void onResponse(Call<InterestResponse> call, Response<InterestResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (String interest : response.body().getInterests()) {
                        Button btn = new Button(InterestsActivity.this);
                        btn.setText(interest);
                        btn.setTextSize(16f);
                        btn.setTextColor(getResources().getColor(android.R.color.white));
                        btn.setBackgroundColor(getResources().getColor(R.color.blue));

                        // Toggle button state
                        btn.setOnClickListener(v -> {
                            boolean isSelected = btn.getTag() != null && (Boolean) btn.getTag();
                            btn.setBackgroundColor(getResources().getColor(isSelected ? R.color.blue : R.color.blue_dark));
                            btn.setTag(!isSelected);
                        });

                        interestsContainer.addView(btn);
                        interestButtons.add(btn);
                    }
                } else {
                    Toast.makeText(InterestsActivity.this, "Failed to load interests", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<InterestResponse> call, Throwable t) {
                Toast.makeText(InterestsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
