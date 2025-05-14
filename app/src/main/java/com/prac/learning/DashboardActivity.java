package com.prac.learning;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardActivity extends AppCompatActivity {

    DatabaseHelper dbHelper;

    TextView greetingText, taskCountText;
    LinearLayout taskListContainer;
    String username;
    ArrayList<String> interests;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        dbHelper = new DatabaseHelper(this);

        greetingText = findViewById(R.id.greetingText);
        taskCountText = findViewById(R.id.taskCount);
        taskListContainer = findViewById(R.id.taskListContainer);
        ImageButton profileBtn = findViewById(R.id.profileBtn);

        username = getIntent().getStringExtra("username");

        if (username == null || username.trim().isEmpty()) {
            SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            username = prefs.getString("username", "Student");
        }

        greetingText.setText("Welcome, " + username + " 👋");

        // ✅ Safely get interests from Intent or SharedPreferences fallback
        interests = getIntent().getStringArrayListExtra("interests");
        if (interests == null || interests.isEmpty()) {
            SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            String stored = prefs.getString("interests", "");
            if (!stored.isEmpty()) {
                interests = new ArrayList<>(Arrays.asList(stored.split(",")));
            } else {
                interests = new ArrayList<>();
            }
        }

        final String email;
        Cursor cursor = dbHelper.getUser(username);
        String tempEmail = "unknown@email.com";
        if (cursor != null && cursor.moveToFirst()) {
            int emailIndex = cursor.getColumnIndex("email");
            if (emailIndex != -1) {
                tempEmail = cursor.getString(emailIndex);
            }
            cursor.close();
        }
        email = tempEmail;

        final int correct = 0;
        final int incorrect = 0;

        profileBtn.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, ProfileActivity.class);
            intent.putExtra("username", username);
            intent.putExtra("email", email);
            intent.putExtra("correct", correct);
            intent.putExtra("incorrect", incorrect);
            startActivity(intent);
        });

        fetchTasks();
    }

    private void fetchTasks() {
        if (interests == null || interests.isEmpty()) {
            taskCountText.setText("No interests found. Please select interests first.");
            return;
        }

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        String interestsCsv = String.join(",", interests);

        Call<TaskResponse> call = apiService.getTasks(interestsCsv);
        call.enqueue(new Callback<TaskResponse>() {
            @Override
            public void onResponse(Call<TaskResponse> call, Response<TaskResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Task> tasks = response.body().getTasks();
                    taskCountText.setText("You have " + tasks.size() + " Task(s) due");
                    showTasks(tasks);
                } else {
                    taskCountText.setText("Failed to load tasks");
                }
            }

            @Override
            public void onFailure(Call<TaskResponse> call, Throwable t) {
                taskCountText.setText("Failed to load tasks");
            }
        });
    }

    private void showTasks(List<Task> tasks) {
        LayoutInflater inflater = LayoutInflater.from(this);
        for (Task task : tasks) {
            View cardView = inflater.inflate(R.layout.item_task_card, taskListContainer, false);
            ((TextView) cardView.findViewById(R.id.taskTitle)).setText(task.getTitle());
            ((TextView) cardView.findViewById(R.id.taskDescription)).setText(task.getDescription());

            cardView.setOnClickListener(v -> {
                Intent intent = new Intent(this, TaskActivity.class);
                intent.putExtra("topic", task.getTitle());
                startActivity(intent);
            });

            taskListContainer.addView(cardView);
        }
    }
}
