# Star Bank Recommendation

![Java CI](https://github.com/aaminter/star-bank-recommendation/actions/workflows/ci.yml/badge.svg)

Командный учебный backend-проект на Java и Spring Boot. Сервис формирует рекомендации банковских продуктов на основе транзакционных данных клиента, фиксированных и динамических правил.

## Реализованные возможности

- Java 17, Spring Boot 4.1.2, Spring Web MVC.
- H2 как read-only источник транзакций и продуктов.
- PostgreSQL как отдельное хранилище динамических правил и статистики.
- JDBC / JdbcTemplate для чтения исходной H2 БД.
- Spring Data JPA и Liquibase для PostgreSQL.
- Три фиксированных правила: Invest 500, Top Saving, Простой кредит.
- Динамические правила с условиями AND, поддержкой negate и четырьмя типами запросов.
- Caffeine-кеширование метрик клиента с централизованной инвалидацией.
- Статистика срабатываний динамических правил.
- Telegram-бот для операторского поиска рекомендаций по username.
- Management endpoints для очистки кешей и получения Maven Build Info.
- Unit- и integration-тесты; CI на Java 17.

## Архитектура и документация

- [Архитектура и диаграммы](docs/architecture.md)
- [Требования, User Stories и трассировка Issues](docs/requirements.md)
- [Развёртывание и конфигурация](docs/deployment.md)
- [Спецификация OpenAPI](docs/openapi.yaml)

## REST API

| Метод | Endpoint | Назначение |
|---|---|---|
| GET | `/recommendation/{user_id}` | Получить рекомендации клиента |
| POST | `/rule` | Создать динамическое правило |
| GET | `/rule` | Получить динамические правила |
| DELETE | `/rule/{id}` | Удалить динамическое правило |
| GET | `/rule/stats` | Получить статистику срабатываний |
| POST | `/management/clear-caches` | Очистить все зарегистрированные кеши |
| GET | `/management/info` | Получить artifactId и версию приложения |

Полная схема запросов и ответов находится в [OpenAPI-файле](docs/openapi.yaml).

## Ключевые архитектурные ограничения

- Исходная H2 база транзакций используется только для чтения.
- Динамические правила и статистика хранятся отдельно в PostgreSQL.
- Правила не изменяют исходные транзакционные данные.
- Фиксированные правила продолжают работать совместно с динамическими.
- Telegram-бот использует общий RecommendationService и не реализует собственный движок правил.
- Доступ к команде Telegram ограничен allowlist операторских Telegram ID. При пустом allowlist команды рекомендаций недоступны.
- Секреты и пароли передаются через переменные окружения.

## Локальная проверка

Требуются JDK 17 и доступная PostgreSQL БД с правами на создание схемы/таблиц для Liquibase.

```bash
./mvnw clean verify
./mvnw package
java -jar target/star-bank-recommendation-0.0.1-SNAPSHOT.jar
```

В Windows PowerShell используйте `mvnw.cmd` вместо `./mvnw`.

Переменные окружения и требования к БД описаны в [руководстве по развёртыванию](docs/deployment.md).

## Статус проекта

Учебный проект, не production-система. Реализованные возможности подтверждаются кодом, тестами, Pull Requests и историей GitHub Issues. Последняя проверенная сборка PR #30: 70 тестов, 0 ошибок, `BUILD SUCCESS`.
