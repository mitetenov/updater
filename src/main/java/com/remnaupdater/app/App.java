package com.remnaupdater.app;

import com.remnaupdater.app.client.RemnawaveApiClient;
import com.remnaupdater.app.csv.FileSubscribersRepository;
import com.remnaupdater.app.service.UserDescriptionUpdateService;
import io.github.cdimascio.dotenv.Dotenv;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class App {
    public static void main(String[] args) {
        System.out.println("[App] Запуск обновления описаний пользователей Remnawave");

        // Загружаем .env только если необходимые переменные не заданы явно
        String baseUrl = System.getenv("REMW_BASE_URL");
        String serviceToken = System.getenv("REMW_SERVICE_TOKEN");
        String csvPath = System.getenv("SUBSCRIBERS_CSV_PATH");
        String csvEncoding = System.getenv("SUBSCRIBERS_CSV_ENCODING");

        if (baseUrl == null || csvPath == null) {
            Dotenv dotenv = Dotenv.configure()
                    .ignoreIfMissing()
                    .load();
            if (baseUrl == null) baseUrl = dotenv.get("REMW_BASE_URL");
            if (serviceToken == null) serviceToken = dotenv.get("REMW_SERVICE_TOKEN");
            if (csvPath == null) csvPath = dotenv.get("SUBSCRIBERS_CSV_PATH");
            if (csvEncoding == null) csvEncoding = dotenv.get("SUBSCRIBERS_CSV_ENCODING");
        }

        // Валидация обязательных переменных, значения по умолчанию допускаются только для csvPath
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalArgumentException("Отсутствует обязательная переменная окружения REMW_BASE_URL (в env или процессе)");
        }
        if (serviceToken == null || serviceToken.isBlank()) {
            throw new IllegalArgumentException("Отсутствует обязательная переменная окружения REMW_SERVICE_TOKEN (в env или процессе)");
        }
        if (csvPath == null || csvPath.isBlank()) {
            if (new java.io.File("src/main/resources/subscribers.xlsx").exists()) {
                csvPath = "src/main/resources/subscribers.xlsx";
            } else {
                csvPath = "src/main/resources/subscribers.csv";
            }
        }
        Charset charset = (csvEncoding == null || csvEncoding.isBlank()) ? StandardCharsets.UTF_8 : Charset.forName(csvEncoding);

        RemnawaveApiClient client = new RemnawaveApiClient(baseUrl, serviceToken);

        FileSubscribersRepository csvRepo = new FileSubscribersRepository(csvPath, charset);
        UserDescriptionUpdateService service = new UserDescriptionUpdateService(client, csvRepo);
        service.run();

        System.out.println("[App] Готово");
    }
}


