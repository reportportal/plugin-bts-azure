CREATE SCHEMA IF NOT EXISTS example;

CREATE TABLE IF NOT EXISTS example.entity
(
    id          BIGSERIAL PRIMARY KEY                 NOT NULL,
    name        VARCHAR(256)                          NOT NULL,
    description VARCHAR(256)                          NOT NULL,
    project_id  BIGINT REFERENCES public.project (id) NOT NULL
);

CREATE INDEX IF NOT EXISTS entity_project_id ON example.entity (project_id);