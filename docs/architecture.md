# Архитектура Star Bank Recommendation

## Обзор

Приложение построено как backend на Spring Boot. REST-контроллеры и обработчик Telegram используют общий сервис рекомендаций. Сервис загружает метрики клиента из базы H2, доступной только для чтения, через JdbcTemplate, проверяет фиксированные наборы правил и динамические правила из PostgreSQL. При совпадении динамического правила увеличивается его счётчик срабатываний.

## Диаграмма компонентов

```mermaid
flowchart LR
    User[Пользователь]
    Manager[Менеджер]
    External[Внешняя система]
    TelegramPlatform[Telegram Bot API]
    H2[(Исходная БД H2<br/>только чтение)]
    PostgreSQL[(PostgreSQL<br/>правила и статистика)]

    subgraph App[Приложение Spring Boot]
        RC[RecommendationController]
        DC[DynamicRuleController]
        MC[ManagementController]
        IC[ManagementInfoController]
        TH[TelegramUpdateHandler]
        RS[RecommendationService]
        StaticRules[Наборы фиксированных правил<br/>Invest 500 / Top Saving / Simple Credit]
        DRService[DynamicRuleService]
        Engine[DynamicRuleEngine]
        Stats[RuleStatsService]
        Repo[RecommendationRepository]
        Cache[Кеш метрик Caffeine]
        UserRepo[TelegramUserRepository]
        CacheAdmin[CacheManagementService]
        BuildInfo[BuildProperties]
    end

    User -->|GET рекомендации по ID клиента| RC
    User -->|/recommend username через бота| TelegramPlatform
    TelegramPlatform <-->|Обновления / SendMessage| TH
    Manager -->|POST/GET/DELETE правила| DC
    Manager -->|GET статистика правил| DC
    External -->|POST очистить кеши| MC
    External -->|GET информация о приложении| IC
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

## Диаграмма последовательности действий: формирование рекомендаций

```mermaid
flowchart TD
    A([Получить рекомендации по ID клиента]) --> B[Загрузить метрики клиента]
    B --> C{Метрики есть в кеше Caffeine?}
    C -->|Да| D[Использовать кешированные метрики]
    C -->|Нет| E[Прочитать агрегаты из H2]
    E --> F[Поместить метрики в кеш]
    F --> D
    D --> G[Проверить все наборы фиксированных правил]
    G --> H[Добавить совпавшие фиксированные рекомендации]
    H --> I[Загрузить динамические правила из PostgreSQL]
    I --> J{Остались динамические правила?}
    J -->|Нет| R[Сформировать RecommendationResponse]
    J -->|Да| K[Проверить каждое условие]
    K --> L[Применить negate к результату условия]
    L --> M{Совпали все условия?}
    M -->|Нет| J
    M -->|Да| N[Добавить динамическую рекомендацию]
    N --> O[Увеличить счётчик срабатываний правила]
    O --> J
    R --> S([Ответ HTTP 200])
```

## Компоненты

| Компонент | Ответственность |
|---|---|
| RecommendationController | REST endpoint получения рекомендаций |
| RecommendationService | Координация фиксированных и динамических правил |
| RecommendationRepository | Агрегация транзакций в H2 и кеширование метрик |
| RecommendationRuleSet | Контракт фиксированного правила |
| DynamicRuleController | CRUD динамических правил и выдача статистики |
| DynamicRuleService | Хранение и чтение динамических правил |
| DynamicRuleEngine | Проверка типов условий, аргументов, AND и negate |
| RuleStatsService | Атомарный учёт срабатываний и чтение статистики |
| CacheManagementService | Очистка всех компонентов, реализующих CacheClearable |
| ManagementInfoController | Выдача Maven artifactId и version из BuildProperties |
| TelegramUpdateHandler | Обработка /recommend username с проверкой allowlist |
| TelegramUserRepository | Поиск единственного пользователя в H2, доступной только для чтения |

## Хранилища и внешние системы

- H2 содержит исходные таблицы клиентов, продуктов и транзакций. Приложение подключается к ней в режиме «только чтение».
- PostgreSQL хранит динамические правила и статистику. Структура создаётся с помощью Liquibase.
- Telegram Bot API доставляет updates и принимает исходящие сообщения. Бот запускается только при наличии токена.
- Caffeine хранит агрегированные метрики клиента в памяти процесса; TTL составляет 10 минут, максимальный размер — 10 000 записей.

## Потоки данных

REST API и Telegram используют один RecommendationService. Telegram не вычисляет правила самостоятельно. Доступ Telegram к поиску клиентов и рекомендациям разрешён только отправителям, чьи ID перечислены в TELEGRAM_BOT_ALLOWED_USER_IDS. Пустой список означает запрет доступа для всех отправителей.
