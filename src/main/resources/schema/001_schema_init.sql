CREATE SCHEMA IF NOT EXISTS azure;

CREATE TABLE IF NOT EXISTS azure.entity
(
    id          BIGSERIAL PRIMARY KEY                 NOT NULL,
    name        VARCHAR(256)                          NOT NULL,
    description VARCHAR(256)                          NOT NULL,
    project_id  BIGINT REFERENCES public.project (id) NOT NULL
);

CREATE INDEX IF NOT EXISTS entity_project_id ON azure.entity (project_id);