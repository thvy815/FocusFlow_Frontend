package com.example.focusflow_frontend.data.api;

import java.util.Map;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AiController {
    @POST("api/ai/chat")
    Call<String> chatWithAI(@Body Map<String, String> body);
}
