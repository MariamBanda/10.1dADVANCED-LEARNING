package com.prac.learning;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {
    @GET("/get_tasks")
    Call<TaskResponse> getTasks(@Query("interests") String interestsCsv);


    @GET("get_interests")
    Call<InterestResponse> getInterests();

    @GET("generate_quiz")
    Call<QuizResponse> getQuiz(@Query("topic") String topic);

}


