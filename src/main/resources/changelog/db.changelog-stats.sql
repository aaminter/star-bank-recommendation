--liquibase formatted sql

--changeset tkuznetsova:2
CREATE TABLE dynamic_rule_stats (
    rule_id BIGINT PRIMARY KEY,
    count BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_dynamic_rule_stats_rule
        FOREIGN KEY (rule_id)
        REFERENCES dynamic_rules(id)
        ON DELETE CASCADE
);
