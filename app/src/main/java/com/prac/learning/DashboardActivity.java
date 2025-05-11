package com.prac.learning;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.prac.learning.Task;
import com.prac.learning.ApiClient;
import com.prac.learning.ApiService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardActivity extends AppCompatActivity {

    TextView greetingText, taskCountText;
    LinearLayout taskListContainer;
    String username;
    ArrayList<String> interests;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        greetingText = findViewById(R.id.greetingText);
        taskCountText = findViewById(R.id.taskCount);
        taskListContainer = findViewById(R.id.taskListContainer);

        username = getIntent().getStringExtra("username");


        if (username == null || username.trim().isEmpty()) {
            username = "Student"; // Default value
        }


        greetingText.setText("Welcome, " + username + " 👋");

        interests = getIntent().getStringArrayListExtra("interests");

        fetchTasks();
    }

    private void fetchTasks() {
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
