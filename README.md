## Updater2 — сборка и запуск

Небольшое Java 21 CLI‑приложение (Maven) для обновления описаний пользователей Remnawave. Конфигурация — через переменные окружения и файл `.env`.

### Требования
- Java 21+
- Maven 3.9+ (`mvn` в PATH)

### Конфигурация окружения
Приложение читает следующие переменные:
- `REMW_BASE_URL` — базовый URL API Remnawave (обязательно)
- `REMW_SERVICE_TOKEN` — сервисный Bearer‑токен (обязательно)
- `SUBSCRIBERS_CSV_PATH` — путь к CSV с подписчиками (необязательно, по умолчанию `src/main/resources/subscribers.csv`)
- `SUBSCRIBERS_CSV_ENCODING` — кодировка CSV (необязательно, по умолчанию `UTF-8`). Для кириллицы из Excel/Windows может понадобиться `windows-1251`.

Переменные можно задать:
1) Явно в окружении процесса (они имеют приоритет),
2) В файле `.env` в корне проекта — будет прочитан только для тех переменных, которые не были переданы явно.

Пример `.env`:
```bash
# Remnawave API configuration
REMW_BASE_URL=http://localhost:3000
REMW_SERVICE_TOKEN=replace-with-token

# Data sources
SUBSCRIBERS_CSV_PATH=src/main/resources/subscribers.csv
# SUBSCRIBERS_CSV_ENCODING=windows-1251
```

### Сборка
```bash
mvn -DskipTests package
```
Собранный JAR: `target/app-0.0.1-SNAPSHOT.jar`

### Запуск
Есть два удобных способа:

1) Через Maven Exec Plugin (без изменения pom.xml):
```bash
mvn -DskipTests -Dexec.mainClass=com.example.app.App org.codehaus.mojo:exec-maven-plugin:3.5.0:java
```

2) Через `java` после сборки:
```bash
# вариант с классами компиляции и зависимостями из Maven
mvn -q -DincludeScope=runtime dependency:build-classpath -Dmdep.outputFile=cp.txt && \
java -cp target/classes:$(cat cp.txt) com.example.app.App
```
Если вы предпочитаете запуск из JAR, добавьте JAR в classpath вместе с зависимостями (см. команду `dependency:build-classpath` выше) или используйте первый способ с Maven Exec.

### Примеры запуска с переменными
```bash
# Переменные через окружение, .env будет использован только для отсутствующих
REMW_BASE_URL=https://api.example.com \
REMW_SERVICE_TOKEN=secret-token \
SUBSCRIBERS_CSV_PATH=/path/to/subscribers.csv \
mvn -DskipTests -Dexec.mainClass=com.example.app.App org.codehaus.mojo:exec-maven-plugin:3.5.0:java
```

Если в описаниях встречается "кракозябра" (Ã, Ð, Ñ, Â, �), попробуйте указать кодировку CSV:
```bash
mvn -DskipTests -Dexec.mainClass=com.example.app.App org.codehaus.mojo:exec-maven-plugin:3.5.0:java
```

### Логика чтения конфигурации
- Сначала читаются переменные процесса (`System.getenv`).
- Если какие‑то отсутствуют — читается `.env` (если есть) и подставляются недостающие значения.
- Обязательные: `REMW_BASE_URL`, `REMW_SERVICE_TOKEN`. Если они пустые после всех попыток — приложение завершится с ошибкой.
- `SUBSCRIBERS_CSV_PATH` имеет дефолт `src/main/resources/subscribers.csv`.

### Полезно
- Для защиты чувствительных данных (токенов, паролей, списка пользователей) используется файл конфигурации `.env`, а также директория `src/main/resources/`. Оба этих пути добавлены в `.gitignore`, чтобы избежать их случайного коммита в репозиторий.
