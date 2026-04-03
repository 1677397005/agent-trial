package com.example.ollama;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;

/**
 * 流式响应示例
 * 使用 /api/generate 接口的 stream=true，逐 token 输出
 */
public class OllamaStreamingClient {

    private static final String OLLAMA_URL = "http://localhost:11434/api/generate";
    private static final OkHttpClient client = new OkHttpClient();
    private static final Gson gson = new Gson();

    public static void main(String[] args) {
        String model = "llama3.2";
        String prompt = "用一句话解释什么是多态";

        System.out.print("模型回复（流式）: ");
        streamGenerate(model, prompt);
        System.out.println();  // 换行
    }

    /**
     * 流式调用，逐 token 输出
     */
    public static void streamGenerate(String model, String prompt) {
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", model);
        requestBody.addProperty("prompt", prompt);
        requestBody.addProperty("stream", true);

        Request request = new Request.Builder()
                .url(OLLAMA_URL)
                .post(RequestBody.create(
                        MediaType.parse("application/json"),
                        gson.toJson(requestBody)))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                System.err.println("流式调用失败: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }
                // 流式响应：每一行都是一个 JSON 对象
                try (var body = response.body()) {
                    String line;
                    var source = body.source();
                    while (!source.exhausted()) {
                        line = source.readUtf8Line();
                        if (line != null && !line.isEmpty()) {
                            JsonObject json = gson.fromJson(line, JsonObject.class);
                            String token = json.get("response").getAsString();
                            System.out.print(token);
                            System.out.flush();

                            // 检查是否结束
                            if (json.has("done") && json.get("done").getAsBoolean()) {
                                break;
                            }
                        }
                    }
                }
            }
        });

        // 等待流式输出完成（简单起见，这里用 sleep）
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}