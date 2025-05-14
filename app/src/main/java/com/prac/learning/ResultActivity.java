package com.prac.learning;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class ResultActivity extends AppCompatActivity {

    TextView scoreText;
    LinearLayout resultsContainer;
    Button finishBtn;
    DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        scoreText = findViewById(R.id.scoreText);
        resultsContainer = findViewById(R.id.resultsContainer);
        finishBtn = findViewById(R.id.finishBtn);
        dbHelper = new DatabaseHelper(this);

        String username = getIntent().getStringExtra("username");
        ArrayList<String> interests = getIntent().getStringArrayListExtra("interests");

        if (username == null || username.isEmpty()) {
            username = getSharedPreferences("UserPrefs", MODE_PRIVATE).getString("username", null);
        }

        final String finalUsername = username;
        final ArrayList<String> finalInterests = interests;

        int score = getIntent().getIntExtra("score", 0);
        int totalQuestions = getIntent().getIntExtra("totalQuestions", 0);

        ArrayList<String> questions = getIntent().getStringArrayListExtra("questions");
        ArrayList<String> answers = getIntent().getStringArrayListExtra("answers");
        ArrayList<String> correctAnswers = getIntent().getStringArrayListExtra("correctAnswers");
        ArrayList<ArrayList<String>> optionsList = (ArrayList<ArrayList<String>>) getIntent().getSerializableExtra("optionsList");

        scoreText.setText("Your Score: " + score + "/" + totalQuestions);

        if (questions != null && answers != null && correctAnswers != null && optionsList != null) {
            for (int i = 0; i < questions.size(); i++) {
                String questionText = questions.get(i);
                String userAnswer = answers.get(i);
                String correctAnswer = correctAnswers.get(i);
                ArrayList<String> options = optionsList.get(i);

                String optionsCsv = options.stream().collect(Collectors.joining(","));
                dbHelper.insertQuizHistory(finalUsername, questionText, userAnswer, correctAnswer, optionsCsv);

                TextView resultCard = new TextView(this);
                resultCard.setText((i + 1) + ". " + questionText + "\n" +
                    "Your Answer: " + userAnswer + "\n" +
                    "✔ Correct Answer: " + correctAnswer);
                resultCard.setTextColor(Color.WHITE);
                resultCard.setTextSize(16f);
                resultCard.setPadding(32, 24, 32, 24);
                resultCard.setBackgroundColor(userAnswer.equals(correctAnswer) ?
                    Color.parseColor("#66BB6A") : Color.parseColor("#EF5350"));

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(0, 0, 0, 32);
                resultCard.setLayoutParams(params);

                resultsContainer.addView(resultCard);
            }
        }

        finishBtn.setOnClickListener(v -> {
            finish();
        });

    }
}
