# Развёртывание

## Требования к окружению

- JDK 17 для локальной сборки; для запуска готового JAR достаточно совместимой Java 17 runtime.
- PostgreSQL, доступный приложению по JDBC.
- Существующая файловая H2 БД транзакций и продуктов. Приложение подключается к ней только для чтения.
- Сетевой доступ к Telegram Bot API нужен только при включённом Telegram-боте.

## Переменные окружения

| Переменная | Назначение | Значение по умолчанию |
|---|---|---|
| DB_URL | JDBC URL исходной H2 БД | jdbc:h2:file:./transaction;ACCESS_MODE_DATA=r;IFEXISTS=TRUE |
| DB_USERNAME | Пользователь H2 | sa |
| DB_PASSWORD | Пароль H2 | пусто |
| REC_DB_URL | JDBC URL PostgreSQL для правил и статистики | jdbc:postgresql://localhost:5432/star_bank_db |
| REC_DB_USERNAME | Пользователь PostgreSQL | rec_db_user |
| REC_DB_PASSWORD | Пароль PostgreSQL | не задан; должен быть предоставлен окружением |
| TELEGRAM_BOT_TOKEN | Токен Telegram-бота; при наличии включает бота | не задан |
| TELEGRAM_BOT_ALLOWED_USER_IDS | Список разрешённых Telegram user ID через запятую | пусто, доступ запрещён |

Пароли и токен не следует записывать в application.properties или коммитить в Git. Перед запуском PostgreSQL должен быть создан, а пользователь приложения должен иметь права, необходимые Liquibase для создания и изменения объектов схемы.

## Сборка и запуск

Linux/macOS:

```bash
./mvnw clean verify
./mvnw package
java -jar target/star-bank-recommendation-0.0.1-SNAPSHOT.jar
```

Windows PowerShell:

```powershell
.\mvnw.cmd clean verify
.\mvnw.cmd package
java -jar .\target\star-bank-recommendation-0.0.1-SNAPSHOT.jar
```

Maven Spring Boot Plugin выполняет goal `build-info` при package. Это формирует BuildProperties для GET /management/info.

## Проверка endpoints

```bash
curl http://localhost:8080/management/info
curl -X POST http://localhost:8080/management/clear-caches
curl http://localhost:8080/rule
curl http://localhost:8080/rule/stats
```

Рекомендации:

```bash
curl http://localhost:8080/recommendation/<USER_UUID>
```

Создание и удаление правил выполняются через POST /rule и DELETE /rule/{id}; структура запросов описана в [OpenAPI](openapi.yaml).

## Telegram

Чтобы включить бота, задайте TELEGRAM_BOT_TOKEN. Для разрешения команды `/recommend username` перечислите Telegram numeric user ID в TELEGRAM_BOT_ALLOWED_USER_IDS, например `123456789,987654321`. Без allowlist бот не выполняет поиск клиента и не возвращает рекомендации.

## CI

GitHub Actions выполняет сборку и тесты на Java 17. Перед сдачей следует убедиться, что последний run зелёный. Настройка branch protection / required status checks производится отдельно в GitHub repository settings.
