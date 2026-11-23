# Требования и трассировка

## Акторы

- **User** — запрашивает рекомендации для клиента через REST API.
- **Manager** — управляет динамическими правилами, просматривает статистику и выполняет management-операции.
- **External system** — предоставляет исходные данные и внешние сообщения: H2, PostgreSQL и Telegram Bot API.

## Use Case Diagram

```mermaid
flowchart LR
    User[User]
    Manager[Manager]
    External[External system]
    UC1((Получить рекомендации))
    UC2((Создать динамическое правило))
    UC3((Получить список правил))
    UC4((Удалить правило))
    UC5((Просмотреть статистику))
    UC6((Очистить кеши))
    UC7((Получить информацию о сервисе))
    UC8((Передать Telegram update))
    UC9((Прочитать транзакционные данные))
    UC10((Сохранить правила и статистику))
    User --> UC1
    Manager --> UC2
    Manager --> UC3
    Manager --> UC4
    Manager --> UC5
    Manager --> UC6
    Manager --> UC7
    External --> UC8
    External --> UC9
    External --> UC10
```

## User Stories

| ID | User Story | Acceptance summary | GitHub Issue | PR |
|---|---|---|---:|---:|
| US-01 | Как User, я хочу получить рекомендации по UUID клиента | HTTP 200, user_id и список рекомендаций; пустой список допустим | #2 | #3 |
| US-02 | Как Manager, я хочу создавать, читать и удалять динамические правила | PostgreSQL, Liquibase, POST/GET/DELETE /rule, валидация | #4 | #25 |
| US-03 | Как User, я хочу получать рекомендации по динамическим правилам наряду с фиксированными | Четыре типа условий, AND, negate; статические правила сохранены | #8, #10 | #26, #29 |
| US-04 | Как Manager, я хочу видеть статистику срабатываний динамических правил | Атомарный счётчик и GET /rule/stats, включая нулевые значения | #20, #21 | #29 |
| US-05 | Как разрешённый оператор, я хочу запросить рекомендации через Telegram по username | /recommend username; только один найденный пользователь; единый сервис рекомендаций | #22 | #30 |
| US-06 | Как Manager, я хочу очищать кеши рекомендаций | POST /management/clear-caches очищает все зарегистрированные кеши | #23 | #30 |
| US-07 | Как Manager, я хочу получать сведения о версии сервиса | GET /management/info возвращает Maven artifactId и version | #24 | #30 |

## Non-functional Requirements

| ID | Требование | Проверка / Issue |
|---|---|---|
| NFR-01 | Приложение работает на Java 17 и Spring Boot | CI, Issue #1, #16 |
| NFR-02 | Исходная H2 БД используется только для чтения | Hikari read-only, параметры подключения; Issues #1, #2 |
| NFR-03 | Динамические правила и статистика хранятся отдельно от исходной H2 БД | PostgreSQL + Liquibase; Issue #4 |
| NFR-04 | Повторные запросы используют кеш метрик; кеш имеет ограничение размера и TTL | Caffeine, cache hit/miss tests; Issue #8 |
| NFR-05 | Условия динамических правил валидируются; сравнения не строятся из произвольного SQL | Белый список операторов и типизированные аргументы; Issues #4, #8 |
| NFR-06 | Секреты не хранятся в репозитории | Переменные окружения; Issue #22 |
| NFR-07 | Telegram не раскрывает рекомендации произвольным отправителям | Allowlist Telegram sender IDs; Issue #22 |
| NFR-08 | Счётчики статистики обновляются атомарно | SQL upsert/increment; Issues #20, #21 |
| NFR-09 | Ключевые сценарии покрыты автоматическими тестами и проверяются CI на Java 17 | GitHub Actions, `mvn clean verify`; Issue #16 |

## Requirements Traceability Matrix

| Requirement ID | GitHub Issue | Pull Request | Result |
|---|---:|---:|---|
| US-01 | #2 | #3 | Merged |
| US-02 | #4 | #25 | Merged |
| US-03 | #8, #10 | #26, #29 | Merged |
| US-04 | #20, #21 | #29 | Merged |
| US-05 | #22 | #30 | Merged |
| US-06 | #23 | #30 | Merged |
| US-07 | #24 | #30 | Merged |
| NFR-01 | #1, #16 | CI workflow | Java 17 CI passes; branch protection policy must be verified in repository settings |
| NFR-02 | #1, #2 | #3 | Merged |
| NFR-03 | #4 | #25 | Merged |
| NFR-04 | #8 | #26 | Merged |
| NFR-05 | #4, #8 | #25, #26, #29 | Merged |
| NFR-06, NFR-07 | #22 | #30 | Merged |
| NFR-08 | #20, #21 | #29 | Merged |
| NFR-09 | #16 | CI workflow | CI run passed; required-check branch protection remains a repository setting |

## GitHub Project workflow

Рекомендуемые статусы карточек: **To Do → In Progress → Done**. Перевод в Done выполняется после merge соответствующего PR и успешного CI. История переходов хранится в GitHub Project; Issue и PR остаются источниками технической трассировки.
