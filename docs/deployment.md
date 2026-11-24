# Развёртывание

## Требования к окружению

- JDK 17 для локальной сборки; для запуска готового JAR достаточно совместимой среды выполнения Java 17.
- PostgreSQL, доступный приложению по JDBC.
- Существующая файловая база H2 с транзакциями и продуктами. Приложение подключается к ней только для чтения.
- Сетевой доступ к Telegram Bot API требуется только при включённом Telegram-боте.

## Переменные окружения

| Переменная | Назначение | Значение по умолчанию |
|---|---|---|
| DB_URL | JDBC URL исходной базы H2 | jdbc:h2:file:./transaction;ACCESS_MODE_DATA=r;IFEXISTS=TRUE |
| DB_USERNAME | Пользователь H2 | sa |
| DB_PASSWORD | Пароль H2 | пусто |
| REC_DB_URL | JDBC URL PostgreSQL для правил и статистики | jdbc:postgresql://localhost:5432/star_bank_db |
| REC_DB_USERNAME | Пользователь PostgreSQL | rec_db_user |
| REC_DB_PASSWORD | Пароль PostgreSQL | не задан; должен быть предоставлен окружением |
| TELEGRAM_BOT_TOKEN | Токен Telegram-бота; при наличии включает бота | не задан |
| TELEGRAM_BOT_ALLOWED_USER_IDS | Разделённый запятыми список разрешённых Telegram user ID | пусто, доступ запрещён |

Пароли и токен не следует записывать в application.properties или добавлять в Git. До запуска приложения необходимо создать базу PostgreSQL. Пользователь приложения должен иметь права, необходимые Liquibase для создания и изменения объектов схемы.

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

Maven Spring Boot Plugin выполняет goal `build-info` при выполнении `package`. Это формирует BuildProperties для GET /management/info.

## Проверка endpoints

```bash
curl http://localhost:8080/management/info
curl -X POST http://localhost:8080/management/clear-caches
curl http://localhost:8080/rule
curl http://localhost:8080/rule/stats
```

Получение рекомендаций:

```bash
curl http://localhost:8080/recommendation/<USER_UUID>
```

Создание и удаление правил выполняются через POST /rule и DELETE /rule/{id}; структура запросов описана в [спецификации OpenAPI](openapi.yaml).

## Управление кешем

Приложение кеширует агрегированные метрики клиентов, полученные из базы H2 транзакций и продуктов, доступной только для чтения. Динамические правила из PostgreSQL в текущей реализации не кешируются.

Если данные в исходной базе H2 были изменены вне приложения и требуется немедленно обновить уже кешированные метрики, выполните:

```bash
curl --fail-with-body -X POST http://localhost:8080/management/clear-caches
```

Успешный ответ: `204 No Content`. Тело запроса не требуется. Команда очищает все кеши компонентов, зарегистрированных через интерфейс `CacheClearable`; при следующем запросе метрики будут загружены из H2 заново.

Кеш локален для процесса приложения. При нескольких экземплярах запрос необходимо выполнить для каждого экземпляра отдельно: вызов на одном экземпляре не очищает кеши остальных. Не предоставляйте этот служебный endpoint для публичного доступа; ограничьте доступ доверенной сетью или средствами инфраструктуры.

## Telegram

Чтобы включить бота, задайте `TELEGRAM_BOT_TOKEN`. Для разрешения команды `/recommend username` перечислите числовые Telegram user ID в `TELEGRAM_BOT_ALLOWED_USER_IDS`, например `123456789,987654321`. Без списка разрешённых ID бот не выполняет поиск клиента и не возвращает рекомендации.

## CI

GitHub Actions выполняет сборку и тесты на Java 17. Перед сдачей следует убедиться, что последний запуск завершился успешно. Настройка защиты ветвей и обязательных проверок статуса выполняется отдельно в настройках репозитория GitHub.
