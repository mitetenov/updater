package com.remnaupdater.app.service;

import com.remnaupdater.app.client.RemnawaveApiClient;
import com.remnaupdater.app.csv.FileSubscribersRepository;
import com.remnaupdater.app.dto.GetAllUsersResponse;
import com.remnaupdater.app.dto.UserDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UserDescriptionUpdateService {
    private final RemnawaveApiClient apiClient;
    private final FileSubscribersRepository csvRepository;

    public UserDescriptionUpdateService(RemnawaveApiClient apiClient, FileSubscribersRepository csvRepository) {
        this.apiClient = apiClient;
        this.csvRepository = csvRepository;
    }

    public void run() {
        System.out.println("[UserDescriptionUpdateService] Старт обновления описаний пользователей");

        int size = 200; // разумный батч
        int start = 0;
        List<UpdatedUser> successfullyUpdated = new ArrayList<>();

        while (true) {
            GetAllUsersResponse page = apiClient.getAllUsers(size, start);
            List<UserDto> users = page.getResponse() != null ? page.getResponse().getUsers() : List.of();
            if (users.isEmpty()) {
                System.out.println("[UserDescriptionUpdateService] Больше пользователей нет. Чтение завершено.");
                break;
            }

            for (UserDto user : users) {
                UpdatedUser updated = processUser(user);
                if (updated != null) {
                    successfullyUpdated.add(updated);
                }
            }

            start += users.size();
        }

        System.out.println("\n--- ИТОГОВОЕ СООБЩЕНИЕ ---");
        System.out.println("Изменено пользователей: " + successfullyUpdated.size());
        for (UpdatedUser u : successfullyUpdated) {
            System.out.println("- Username: " + u.username + ", UUID: " + u.uuid + ", Telegram ник: " + u.telegramNick);
        }
        System.out.println("--------------------------");
    }

    private static class UpdatedUser {
        String username;
        String uuid;
        String telegramNick;

        UpdatedUser(String username, String uuid, String telegramNick) {
            this.username = username;
            this.uuid = uuid;
            this.telegramNick = telegramNick;
        }
    }

    private UpdatedUser processUser(UserDto user) {
        String desc = user.getDescription();
        boolean hasDescription = desc != null && !desc.trim().isEmpty();
        if (hasDescription) {
            return null; // уже есть описание, пропускаем тихо чтобы не спамить лог
        }
        Long tgId = user.getTelegramId();
        if (tgId == null) {
            return null; // нет id, пропускаем
        }
        String followerId = String.valueOf(tgId);
        
        return csvRepository.findFromByFollowerId(followerId).map(from -> {
            String cleaned = from == null ? null : from.replace("@", "").trim();
            if (cleaned == null || cleaned.isEmpty()) {
                return null;
            }
            apiClient.updateUserDescription(Objects.requireNonNull(user.getUuid()), cleaned);
            return new UpdatedUser(user.getUsername(), user.getUuid(), cleaned);
        }).orElse(null);
    }
}


