package com.prac.learning;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.prac.learning.ApiClient;
import com.prac.learning.ApiService;
import com.prac.learning.QuizResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TaskActivity extends AppCompatActivity {

    TextView taskTitle, taskDescription, questionView;
    RadioGroup answersGroup;
    Button submitBtn;

    private List<QuizResponse.Quiz> quizList;
    private int currentIndex = 0;
    private ArrayList<String> questionHistory = new ArrayList<>();
    private ArrayList<String> answerHistory = new ArrayList<>();
    private ArrayList<String> correctAnswers = new ArrayList<>();
    private int score = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);

        taskTitle = findViewById(R.id.taskTitle);
        taskDescription = findViewById(R.id.taskDescription);
        questionView = findViewById(R.id.taskQuestion);
        answersGroup = findViewById(R.id.answersGroup);
        submitBtn = findViewById(R.id.submitBtn);

        String topic = getIntent().getStringExtra("topic");

        // Dynamically set heading and description
        taskTitle.setText(topic);
        taskDescription.setText("This task tests your knowledge on " + topic + ".");

        fetchQuiz(topic);

        submitBtn.setOnClickListener(v -> {
            int selectedId = answersGroup.getCheckedRadioButtonId();
            if (selectedId == -1) {
                Toast.makeText(this, "Please select an answer", Toast.LENGTH_SHORT).show();
                return;
            }

            RadioButton selectedOption = findViewById(selectedId);
            String selectedAnswer = selectedOption.getText().toString();

            QuizResponse.Quiz currentQuiz = quizList.get(currentIndex);
            questionHistory.add(currentQuiz.getQuestion());
            answerHistory.add(selectedAnswer);
            correctAnswers.add(currentQuiz.getCorrect());

            if (selectedAnswer.equals(currentQuiz.getCorrect())) {
                score++;
            }

            currentIndex++;
            if (currentIndex < quizList.size()) {
                loadQuestion();
            } else {
                // Pass all data to ResultActivity
                Intent intent = new Intent(TaskActivity.this, ResultActivity.class);
                intent.putExtra("score", score);
                intent.putExtra("totalQuestions", quizList.size()); // Send the total number of questions
                intent.putStringArrayListExtra("questions", questionHistory);
                intent.putStringArrayListExtra("answers", answerHistory);
                intent.putStringArrayListExtra("correctAnswers", correctAnswers); // Ensure you add this list
                startActivity(intent);
                finish();
            }
        });
    }

    private void fetchQuiz(String topic) {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<QuizResponse> call = apiService.getQuiz(topic);

        call.enqueue(new Callback<QuizResponse>() {
            @Override
            public void onResponse(Call<QuizResponse> call, Response<QuizResponse> response) {
                if (response.isSuccessful() && response.body() != null &&
                    response.body().getQuizList() != null &&
                    !response.body().getQuizList().isEmpty()) {

                    quizList = response.body().getQuizList();
                    loadQuestion();

                } else {
                    Toast.makeText(TaskActivity.this, "No quiz questions found", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<QuizResponse> call, Throwable t) {
                Toast.makeText(TaskActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadQuestion() {
        if (quizList == null || quizList.isEmpty() || currentIndex >= quizList.size()) {
            Toast.makeText(this, "No more questions to load", Toast.LENGTH_SHORT).show();
            return;
        }

        answersGroup.removeAllViews();
        QuizResponse.Quiz quiz = quizList.get(currentIndex);
        questionView.setText(quiz.getQuestion());

        for (String answer : quiz.getAnswers()) {
            RadioButton rb = new RadioButton(this);
            rb.setText(answer);
            rb.setTextSize(16f);
            rb.setTextColor(Color.BLACK);
            answersGroup.addView(rb);
        }
    }
}
