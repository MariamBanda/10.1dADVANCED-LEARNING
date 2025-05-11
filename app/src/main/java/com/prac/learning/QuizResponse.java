package com.prac.learning;

import java.io.Serializable;
import java.util.List;

public class QuizResponse {
    private List<Quiz> quizList;

    public List<Quiz> getQuizList() {
        return quizList;
    }

    public static class Quiz implements Serializable {
        private String question;
        private List<String> answers;
        private String correct;

        public String getQuestion() {
            return question;
        }

        public List<String> getAnswers() {
            return answers;
        }

        public String getCorrect() {
            return correct;
        }
    }
}
