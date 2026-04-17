package com.example.app.client;

import com.example.app.dto.GetAllUsersResponse;
import com.example.app.dto.UpdateUserRequest;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class RemnawaveApiClient {
    private final String baseUrl;
    private final String serviceToken;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public RemnawaveApiClient(String baseUrl, String serviceToken) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.serviceToken = serviceToken;
        this.objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();
    }

    // Удалён sanitizeDescription — исправление существующих записей больше не выполняется

    public GetAllUsersResponse getAllUsers(int size, int start) {
        String url = baseUrl + "/api/users?size=" + size + "&start=" + start;
        System.out.println("[RemnawaveApiClient] Запрос пользователей: " + url);
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + serviceToken)
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return objectMapper.readValue(response.body(), GetAllUsersResponse.class);
            }
            throw new RuntimeException("Ошибка получения пользователей: HTTP " + response.statusCode() + " - " + response.body());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Ошибка получения пользователей", e);
        }
    }

    public void updateUserDescription(String uuid, String description) {
        System.out.println("[RemnawaveApiClient] Обновление description для пользователя " + uuid + " -> '" + description + "'");
        try {
            UpdateUserRequest requestDto = new UpdateUserRequest();
            requestDto.setUuid(uuid);
            requestDto.setDescription(description);

            String body = objectMapper.writeValueAsString(requestDto);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/users"))
                    .header("Authorization", "Bearer " + serviceToken)
                    .header("Content-Type", "application/json")
                    .method("PATCH", HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                System.out.println("[RemnawaveApiClient] Обновление успешно: HTTP " + response.statusCode());
                return;
            }
            throw new RuntimeException("Ошибка обновления пользователя: HTTP " + response.statusCode() + " - " + response.body());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Ошибка обновления пользователя", e);
        }
    }
}


