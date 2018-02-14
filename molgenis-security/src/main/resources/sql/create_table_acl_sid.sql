CREATE TABLE IF NOT EXISTS acl_sid (
  id        BIGSERIAL NOT NULL PRIMARY KEY,
  principal BOOLEAN   NOT NULL,
  sid       VARCHAR   NOT NULL,
  CONSTRAINT unique_uk_1 UNIQUE (sid, principal)
);