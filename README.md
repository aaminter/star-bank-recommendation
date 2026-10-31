# Star Bank Recommendation

![Java CI](https://github.com/aaminter/star-bank-recommendation/actions/workflows/ci.yml/badge.svg)

Командный учебный backend-проект на Java и Spring Boot. Сервис формирует рекомендации клиентам на основе транзакционных данных и набора правил.

## Текущая реализация

- Java 17, Spring Boot 4.1.2, Spring Web MVC;
- JDBC / JdbcTemplate;
- H2 как read-only источник транзакций и продуктов;
- три фиксированных правила рекомендаций;
- единая загрузка CustomerMetrics на запрос: один агрегирующий запрос передаёт снимок метрик всем правилам;
- JUnit и Spring Boot Test.

## В разработке

- отдельное хранилище динамических правил на PostgreSQL;
- CRUD API для правил;
- Liquibase migrations;
- движок интерпретации динамических условий;
- кеширование и инвалидация;
- интеграционные тесты PostgreSQL.

Эти возможности не считаются реализованными до merge соответствующих PR и прохождения проверок.

## Архитектурные ограничения

- Исходная H2 база транзакций остаётся read-only.
- Динамические правила не должны изменять исходные транзакционные данные.
- Фиксированные правила должны сохранять поведение.
- Секреты передаются через переменные окружения.
- Изменения проходят через Issues, feature branches, PR и CI.

## Запуск

Требуются JDK 17 и Maven.

```bash
mvn clean verify
mvn spring-boot:run
```

Локальная H2 база задаётся через DB_URL. Отдельные параметры PostgreSQL понадобятся после реализации хранилища динамических правил.

## Командная работа

Репозиторий содержит:
- CONTRIBUTING.md и SECURITY.md;
- PR template;
- архитектурный обзор и proposed ADR;
- Definition of Done, Code Review Guidelines, workflow и правила интеграции;
- кейс портфолио и отдельный план Team Lead Evidence Sprint.

ADR считаются принятыми только после обсуждения и явной фиксации решения командой.

## План подтверждения Team Lead

[Team Lead Evidence Sprint: 4 недели](docs/portfolio/team-lead-evidence-sprint.md)

[GitHub Issue #18](https://github.com/aaminter/star-bank-recommendation/issues/18) содержит этапы и критерии 5/5.

## Статус

Учебный проект в развитии, не production-система. Реализованные возможности отделены от запланированных; личный вклад и командные результаты должны подтверждаться кодом, PR, review, CI и историей Issues.
