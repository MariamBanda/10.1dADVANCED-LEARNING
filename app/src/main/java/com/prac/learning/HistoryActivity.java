package com.prac.learning;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;

public class HistoryActivity extends AppCompatActivity {

    LinearLayout historyContainer;
    DatabaseHelper dbHelper;
    String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        historyContainer = findViewById(R.id.historyContainer);
        ImageButton backBtn = findViewById(R.id.backBtn);
        dbHelper = new DatabaseHelper(this);

        // ✅ Get username from Intent or fallback to SharedPreferences
        username = getIntent().getStringExtra("username");
        if (username == null || username.trim().isEmpty()) {
            SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            username = prefs.getString("username", null);
        }

        if (username == null || username.trim().isEmpty()) {
            Toast.makeText(this, "Username missing. Cannot load history.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // ✅ Back button logic
        backBtn.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            String email = prefs.getString("email", "unknown@email.com");

            Intent intent = new Intent(HistoryActivity.this, ProfileActivity.class);
            intent.putExtra("username", username);
            intent.putExtra("email", email);
            startActivity(intent);
            finish();
        });

        // ✅ Load quiz history
        Cursor cursor = dbHelper.getQuizHistoryForUser(username);
        if (cursor != null && cursor.moveToFirst()) {
            int index = 1;
            do {
                String question = cursor.getString(cursor.getColumnIndexOrThrow("question"));
                String userAnswer = cursor.getString(cursor.getColumnIndexOrThrow("user_answer"));
                String correctAnswer = cursor.getString(cursor.getColumnIndexOrThrow("correct_answer"));
                String optionsCsv = cursor.getString(cursor.getColumnIndexOrThrow("options"));
                ArrayList<String> options = new ArrayList<>(Arrays.asList(optionsCsv.split(",")));

                addQuestionCard(index++, question, userAnswer, correctAnswer, options);
            } while (cursor.moveToNext());
            cursor.close();
        } else {
            Toast.makeText(this, "No quiz history found for user.", Toast.LENGTH_SHORT).show();
        }
    }

    private void addQuestionCard(int index, String question, String userAnswer, String correctAnswer, ArrayList<String> options) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundColor(Color.parseColor("#1976D2"));
        card.setPadding(24, 24, 24, 24);
        card.setElevation(6f);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.setMargins(0, 0, 0, 24);
        card.setLayoutParams(cardParams);

        TextView questionTitle = new TextView(this);
        questionTitle.setText(index + ". " + question);
        questionTitle.setTextColor(Color.WHITE);
        questionTitle.setTextSize(18f);
        questionTitle.setPadding(0, 0, 0, 16);
        card.addView(questionTitle);

        for (String option : options) {
            TextView answerView = new TextView(this);
            answerView.setTextSize(16f);
            answerView.setPadding(8, 8, 8, 8);

            if (option.equals(userAnswer) && option.equals(correctAnswer)) {
                answerView.setText("✅ " + option + " (Your Answer, Correct)");
                answerView.setTextColor(Color.parseColor("#A5D6A7"));
            } else if (option.equals(userAnswer)) {
                answerView.setText("❌ " + option + " (Your Answer)");
                answerView.setTextColor(Color.parseColor("#EF9A9A"));
            } else if (option.equals(correctAnswer)) {
                answerView.setText("✔ " + option + " (Correct Answer)");
                answerView.setTextColor(Color.parseColor("#81C784"));
            } else {
                answerView.setText("• " + option);
                answerView.setTextColor(Color.WHITE);
            }

            card.addView(answerView);
        }

        historyContainer.addView(card);
    }
}
