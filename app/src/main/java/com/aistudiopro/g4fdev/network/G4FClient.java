package com.aistudiopro.g4fdev.network;

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public final class G4FClient {

    private static final String G4F_ENDPOINT = "https://api.g4f.icu/v1/chat/completions"; 
    private static final MediaType JSON_MEDIA_TYPE = MediaType.get("application/json; charset=utf-8");
    
    private final OkHttpClient httpClient;
    private final Handler mainHandler;

    public interface G4FCallback {
        void onSuccess(String responseText);
        void onFailure(String errorMessage);
    }

    public G4FClient() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public void generateCode(@NonNull String prompt, @NonNull final G4FCallback callback) {
        try {
            JSONObject payload = new JSONObject();
            payload.put("model", "gpt-4"); 
            
            JSONArray messages = new JSONArray();
            
            JSONObject systemMessage = new JSONObject();
            systemMessage.put("role", "system");
            systemMessage.put("content", "You are an elite AI Code Developer. Write production-ready, highly optimized, and clean code with clear explanations.");
            messages.put(systemMessage);

            JSONObject userMessage = new JSONObject();
            userMessage.put("role", "user");
            userMessage.put("content", prompt);
            messages.put(userMessage);

            payload.put("messages", messages);
            payload.put("stream", false);

            RequestBody body = RequestBody.create(payload.toString(), JSON_MEDIA_TYPE);

            Request request = new Request.Builder()
                    .url(G4F_ENDPOINT)
                    .post(body)
                    .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .addHeader("Accept", "application/json")
                    .addHeader("Accept-Language", "en-US,en;q=0.9")
                    .addHeader("Origin", "https://g4f.icu")
                    .addHeader("Referer", "https://g4f.icu/")
                    .build();

            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    postFailure(callback, "Network Error: " + e.getMessage());
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    try (Response resp = response) {
                        if (!resp.isSuccessful()) {
                            postFailure(callback, "G4F Server Error: Code " + resp.code());
                            return;
                        }

                        String responseBody = resp.body().string();
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        
                        String aiResponse = jsonResponse.getJSONArray("choices")
                                .getJSONObject(0)
                                .getJSONObject("message")
                                .getString("content");

                        postSuccess(callback, aiResponse);
                    } catch (Exception e) {
                        postFailure(callback, "Parsing Error: " + e.getMessage());
                    }
                }
            });

        } catch (Exception e) {
            callback.onFailure("Initialization Error: " + e.getMessage());
        }
    }

    private void postSuccess(final G4FCallback callback, final String result) {
        mainHandler.post(() -> callback.onSuccess(result));
    }

    private void postFailure(final G4FCallback callback, final String error) {
        mainHandler.post(() -> callback.onFailure(error));
    }
}
