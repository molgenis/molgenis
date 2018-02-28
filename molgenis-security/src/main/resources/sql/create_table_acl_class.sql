CREATE TABLE IF NOT EXISTS acl_class (
  id            BIGSERIAL NOT NULL PRIMARY KEY,
  class         VARCHAR   NOT NULL,
  class_id_type VARCHAR   NOT NULL,
  CONSTRAINT unique_uk_2 UNIQUE (class)
);