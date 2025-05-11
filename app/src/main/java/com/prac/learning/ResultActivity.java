package com.prac.learning;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class ResultActivity extends AppCompatActivity {

    TextView scoreText;
    LinearLayout resultsContainer;
    Button finishBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        scoreText = findViewById(R.id.scoreText);
        resultsContainer = findViewById(R.id.resultsContainer);
        finishBtn = findViewById(R.id.finishBtn);

        int score = getIntent().getIntExtra("score", 0);
        int totalQuestions = getIntent().getIntExtra("totalQuestions", 0);
        ArrayList<String> questions = getIntent().getStringArrayListExtra("questions");
        ArrayList<String> answers = getIntent().getStringArrayListExtra("answers");
        ArrayList<String> correctAnswers = getIntent().getStringArrayListExtra("correctAnswers");


        scoreText.setText("Your Score: " + score + "/" + totalQuestions);

        if (questions != null && answers != null && correctAnswers != null) {
            for (int i = 0; i < questions.size(); i++) {
                String questionText = questions.get(i);
                String userAnswer = answers.get(i);
                String correctAnswer = correctAnswers.get(i);

                // Create result card dynamically
                TextView resultCard = new TextView(this);
                resultCard.setText((i + 1) + ". " + questionText + "\n" +
                    "Your Answer: " + userAnswer + "\n" +
                    "Correct Answer: " + correctAnswer);
                resultCard.setTextColor(Color.WHITE); // Make text white
                resultCard.setTextSize(16f);
                resultCard.setPadding(32, 24, 32, 24);

                // Highlight correct and incorrect answers
                if (userAnswer.equals(correctAnswer)) {
                    resultCard.setBackgroundColor(Color.parseColor("#66BB6A"));
                    resultCard.setBackgroundColor(Color.parseColor("#EF5350"));
                }

                resultCard.setElevation(8f);

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
            Intent intent = new Intent(ResultActivity.this, DashboardActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
