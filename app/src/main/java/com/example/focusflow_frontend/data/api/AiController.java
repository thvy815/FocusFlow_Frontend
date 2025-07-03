package com.example.focusflow_frontend.data.api;

import java.util.Map;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.PUT;

public interface AiController {
    @POST("api/ai/chat")
    Call<String> chatWithAI(@Body Map<String, String> body);
    @PUT("/api/user/ai-usage")
    Call<String> incrementAiUsage(@Body Map<String, Integer> body);
}
