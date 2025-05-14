package com.prac.learning;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;

public class ProfileActivity extends AppCompatActivity {

    TextView profileTitle, emailText, totalQuestionsValue, correctValue, incorrectValue;
    TextView aiSummaryText, notificationText;

    Button shareBtn, historyBtn, upgradeBtn;


    DatabaseHelper dbHelper;

    String username, email;
    int correct = 0;
    int incorrect = 0;
    int total = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Bind Views
        profileTitle = findViewById(R.id.profileTitle);
        emailText = findViewById(R.id.emailText);
        totalQuestionsValue = findViewById(R.id.totalQuestionsValue);
        correctValue = findViewById(R.id.correctValue);
        incorrectValue = findViewById(R.id.incorrectValue);
        aiSummaryText = findViewById(R.id.aiSummaryText);
        notificationText = findViewById(R.id.notificationText);
        shareBtn = findViewById(R.id.shareBtn);
        historyBtn = findViewById(R.id.historyBtn);
        dbHelper = new DatabaseHelper(this);
        upgradeBtn = findViewById(R.id.upgradeBtn);

        username = getIntent().getStringExtra("username");
        email = getIntent().getStringExtra("email");

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);

        if (username == null || username.trim().isEmpty()) {
            username = prefs.getString("username", "Student");
        }
        if (email == null || email.trim().isEmpty()) {
            Cursor cursor = dbHelper.getUser(username);
            if (cursor != null && cursor.moveToFirst()) {
                email = cursor.getString(cursor.getColumnIndexOrThrow("email"));
                cursor.close();
            } else {
                email = prefs.getString("email", "unknown@email.com");
            }
        }


        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("username", username);
        editor.putString("email", email);
        editor.apply();

        loadQuizStatsFromDB();
        updateTierBadge();

        // Display stats
        emailText.setText("Email: " + email);
        totalQuestionsValue.setText(String.valueOf(total));
        correctValue.setText(String.valueOf(correct));
        incorrectValue.setText(String.valueOf(incorrect));

        // Dynamic AI Summary
        if (total == 0) {
            aiSummaryText.setText("📝 No quiz data yet. Start learning!");
        } else if (correct >= incorrect) {
            aiSummaryText.setText("🎉 Great job! Keep it up!");
        } else {
            aiSummaryText.setText("⚠️ Let's revisit some key topics to improve!");
        }

        // Dynamic notification
        notificationText.setText("🔔 Welcome back! You have " + total + " answered questions.");

        // Share profile
        shareBtn.setOnClickListener(v -> {
            String shareText = "📘 Student Profile\n"
                + "Username: " + username + "\n"
                + "Correct Answers: " + correct + "\n"
                + "Incorrect Answers: " + incorrect;
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
            startActivity(Intent.createChooser(shareIntent, "Share your profile"));
        });

        historyBtn.setOnClickListener(v -> {
            Cursor cursor = dbHelper.getQuizHistoryForUser(username);

            ArrayList<String> questions = new ArrayList<>();
            ArrayList<String> answers = new ArrayList<>();
            ArrayList<String> correctAnswers = new ArrayList<>();
            ArrayList<ArrayList<String>> optionsList = new ArrayList<>();

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    questions.add(cursor.getString(cursor.getColumnIndexOrThrow("question")));
                    String userAnswer = cursor.getString(cursor.getColumnIndexOrThrow("user_answer"));
                    String correctAnswer = cursor.getString(cursor.getColumnIndexOrThrow("correct_answer"));
                    String optionsCsv = cursor.getString(cursor.getColumnIndexOrThrow("options"));

                    answers.add(userAnswer);
                    correctAnswers.add(correctAnswer);
                    optionsList.add(new ArrayList<>(Arrays.asList(optionsCsv.split(","))));
                } while (cursor.moveToNext());
                cursor.close();
            }


            Intent intent = new Intent(ProfileActivity.this, HistoryActivity.class);
            intent.putExtra("username", username); // ✅ Required fix
            intent.putStringArrayListExtra("questions", questions);
            intent.putStringArrayListExtra("answers", answers);
            intent.putStringArrayListExtra("correctAnswers", correctAnswers);
            intent.putExtra("optionsList", optionsList);
            startActivity(intent);
        });
        upgradeBtn.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, UpgradeActivity.class);
            startActivity(intent);
        });

    }


    @Override
    protected void onResume() {
        super.onResume();
        updateTierBadge(); // Refresh tier badge
    }

    private void updateTierBadge() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String tier = prefs.getString("tier", "Starter");
        String tierBadge = tier.equals("Starter") ? "" : " 🌟 " + tier;
        profileTitle.setText("Username: " + username + tierBadge);
    }

    private void loadQuizStatsFromDB() {
        correct = 0;
        incorrect = 0;
        total = 0;

        Cursor cursor = dbHelper.getQuizHistoryForUser(username);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String userAnswer = cursor.getString(cursor.getColumnIndexOrThrow("user_answer"));
                String correctAnswer = cursor.getString(cursor.getColumnIndexOrThrow("correct_answer"));
                if (userAnswer.equals(correctAnswer)) {
                    correct++;
                } else {
                    incorrect++;
                }
            } while (cursor.moveToNext());
            cursor.close();
            total = correct + incorrect;
        }
    }
}
