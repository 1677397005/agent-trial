package com.example.ollama;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;

/**
 * 模型管理示例
 * 列出本地模型、查看模型信息、删除模型
 */
public class OllamaModelManager {

    private static final String OLLAMA_BASE = "http://localhost:11434/api";
    private static final OkHttpClient client = new OkHttpClient();
    private static final Gson gson = new Gson();

    public static void main(String[] args) {
        // 1. 列出本地所有模型
        listModels();

        // 2. 查看指定模型信息
        showModelInfo("llama3.2");

        // 3. 删除模型（谨慎使用，默认注释）
        // deleteModel("test-model");
    }

    /**
     * 列出本地已安装的模型
     */
    public static void listModels() {
        Request request = new Request.Builder()
                .url(OLLAMA_BASE + "/tags")
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            String body = response.body().string();
            JsonObject json = gson.fromJson(body, JsonObject.class);
            JsonArray models = json.getAsJsonArray("models");

            System.out.println("本地模型列表:");
            for (int i = 0; i < models.size(); i++) {
                JsonObject model = models.get(i).getAsJsonObject();
                String name = model.get("name").getAsString();
                String size = model.get("size").getAsString();
                System.out.println("  - " + name + " (" + size + " bytes)");
            }
        } catch (IOException e) {
            System.err.println("获取模型列表失败: " + e.getMessage());
        }
    }

    /**
     * 查看模型详细信息
     */
    public static void showModelInfo(String modelName) {
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("name", modelName);

        Request request = new Request.Builder()
                .url(OLLAMA_BASE + "/show")
                .post(RequestBody.create(
                        MediaType.parse("application/json"),
                        gson.toJson(requestBody)))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            String body = response.body().string();
            System.out.println("模型 " + modelName + " 信息: " + body);
        } catch (IOException e) {
            System.err.println("获取模型信息失败: " + e.getMessage());
        }
    }

    /**
     * 删除模型
     */
    public static void deleteModel(String modelName) {
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("name", modelName);

        Request request = new Request.Builder()
                .url(OLLAMA_BASE + "/delete")
                .delete(RequestBody.create(
                        MediaType.parse("application/json"),
                        gson.toJson(requestBody)))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                System.out.println("模型 " + modelName + " 已删除");
            } else {
                System.out.println("删除失败: " + response.code());
            }
        } catch (IOException e) {
            System.err.println("删除模型失败: " + e.getMessage());
        }
    }
}