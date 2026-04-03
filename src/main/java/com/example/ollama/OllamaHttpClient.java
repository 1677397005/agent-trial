package com.example.ollama;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;

/**
 * 基础 HTTP 调用示例
 * 使用 OkHttp 直接调用 Ollama 的 /api/generate 接口
 */
public class OllamaHttpClient {

    private static final String OLLAMA_URL = "http://localhost:11434/api/generate";
    private static final OkHttpClient client = new OkHttpClient();
    private static final Gson gson = new Gson();

    public static void main(String[] args) {
        String model = "llama3.2";      // 换成你本地已拉取的模型名
        String prompt = "用一句话解释什么是 Java 接口";

        String response = generate(model, prompt);
        System.out.println("模型回复: " + response);
    }

    /**
     * 同步调用 Ollama 生成文本
     */
    public static String generate(String model, String prompt) {
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", model);
        requestBody.addProperty("prompt", prompt);
        requestBody.addProperty("stream", false);  // 非流式

        Request request = new Request.Builder().url(OLLAMA_URL)
                .post(RequestBody.create(MediaType.parse("application/json"), gson.toJson(requestBody)))
                .build();

        try (Response response = client.newCall(request)
                .execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            String responseBody = response.body()
                    .string();
            JsonObject json = gson.fromJson(responseBody, JsonObject.class);
            return json.get("response")
                    .getAsString();
        } catch (IOException e) {
            System.err.println("调用失败: " + e.getMessage());
            return null;
        }
    }
}