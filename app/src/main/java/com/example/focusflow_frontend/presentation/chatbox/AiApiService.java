package com.example.focusflow_frontend.presentation.chatbox;

import java.util.Map;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AiApiService {
    @POST("api/ai/chat")
    Call<String> chatWithAI(@Body Map<String, String> body);
}

