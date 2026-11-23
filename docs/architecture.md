# Архитектура Star Bank Recommendation

## Обзор

Приложение построено как Spring Boot backend. REST-контроллеры и Telegram-обработчик используют общий сервис рекомендаций. Сервис загружает метрики клиента из read-only H2 через JdbcTemplate, проверяет фиксированные наборы правил и динамические правила из PostgreSQL. При совпадении динамического правила увеличивается его счётчик.

## Component Diagram

```mermaid
flowchart LR
    User[Пользователь]
    Manager[Менеджер]
    External[Внешняя система]
    TelegramPlatform[Telegram Bot API]
    H2[(H2 source DB<br/>read-only)]
    PostgreSQL[(PostgreSQL<br/>rules and stats)]

    subgraph App[Spring Boot application]
        RC[RecommendationController]
        DC[DynamicRuleController]
        MC[ManagementController]
        IC[ManagementInfoController]
        TH[TelegramUpdateHandler]
        RS[RecommendationService]
        StaticRules[Static Rule Sets<br/>Invest 500 / Top Saving / Simple Credit]
        DRService[DynamicRuleService]
        Engine[DynamicRuleEngine]
        Stats[RuleStatsService]
        Repo[RecommendationRepository]
        Cache[Caffeine metrics cache]
        UserRepo[TelegramUserRepository]
        CacheAdmin[CacheManagementService]
        BuildInfo[BuildProperties]
    end

    User -->|GET recommendations by user ID| RC
    User -->|/recommend username via bot| TelegramPlatform
    TelegramPlatform <-->|Updates / SendMessage| TH
    Manager -->|POST/GET/DELETE rules| DC
    Manager -->|GET rule stats| DC
    External -->|POST clear caches| MC
    External -->|GET application info| IC
    RC --> RS
    TH --> UserRepo
    TH --> RS
    UserRepo --> H2
    RS --> StaticRules
    RS --> DRService
    RS --> Engine
    RS --> Stats
    RS --> Repo
    Repo --> Cache
    Cache --> H2
    DRService --> PostgreSQL
    Stats --> PostgreSQL
    MC --> CacheAdmin
    CacheAdmin --> Repo
    IC --> BuildInfo
```

## Activity Diagram: формирование рекомендаций

```mermaid
flowchart TD
    A([Get recommendations for user ID]) --> B[Load customer metrics]
    B --> C{Metrics in Caffeine cache?}
    C -->|Yes| D[Use cached metrics]
    C -->|No| E[Read aggregates from H2]
    E --> F[Put metrics into cache]
    F --> D
    D --> G[Evaluate all static rule sets]
    G --> H[Add matching static recommendations]
    H --> I[Load dynamic rules from PostgreSQL]
    I --> J{More dynamic rules?}
    J -->|No| R[Build RecommendationResponse]
    J -->|Yes| K[Evaluate each condition]
    K --> L[Apply negate to condition result]
    L --> M{Do all conditions match?}
    M -->|No| J
    M -->|Yes| N[Add dynamic recommendation]
    N --> O[Increment rule hit counter]
    O --> J
    R --> S([HTTP 200 response])
```

## Компоненты

| Компонент | Ответственность |
|---|---|
| RecommendationController | REST endpoint получения рекомендаций |
| RecommendationService | Оркестрация статических и динамических правил |
| RecommendationRepository | Агрегация транзакций в H2 и кеширование метрик |
| RecommendationRuleSet | Контракт фиксированного правила |
| DynamicRuleController | CRUD динамических правил и выдача статистики |
| DynamicRuleService | Хранение и чтение динамических правил |
| DynamicRuleEngine | Проверка типов условий, аргументов, AND и negate |
| RuleStatsService | Атомарный учёт срабатываний и чтение статистики |
| CacheManagementService | Очистка всех компонентов, реализующих CacheClearable |
| ManagementInfoController | Выдача Maven artifactId и version из BuildProperties |
| TelegramUpdateHandler | Обработка /recommend username с проверкой allowlist |
| TelegramUserRepository | Поиск единственного пользователя в read-only H2 |

## Хранилища и внешние системы

- H2 содержит исходные таблицы клиентов, продуктов и транзакций. Приложение подключается к ней в режиме read-only.
- PostgreSQL хранит динамические правила и статистику. Структура создаётся Liquibase.
- Telegram Bot API доставляет updates и принимает исходящие сообщения. Бот запускается только при наличии токена.
- Caffeine хранит агрегированные метрики клиента в памяти процесса; TTL 10 минут, максимальный размер 10 000 записей.

## Потоки данных

REST и Telegram используют один RecommendationService. Telegram не вычисляет правила самостоятельно. Доступ Telegram к поиску клиентов и рекомендациям разрешён только sender ID, перечисленным в TELEGRAM_BOT_ALLOWED_USER_IDS. Пустой список означает запрет доступа всем отправителям.
