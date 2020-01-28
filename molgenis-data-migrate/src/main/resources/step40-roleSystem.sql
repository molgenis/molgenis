-- insert role METRICS
INSERT INTO "sys_sec_Role#b6639604" (id, name, label, "labelEn", "labelNl", "labelDe", "labelEs", "labelIt", "labelPt", "labelFr", "labelXx", description, "descriptionEn", "descriptionNl", "descriptionDe", "descriptionEs", "descriptionIt", "descriptionPt", "descriptionFr", "descriptionXx", "group")
VALUES
  (md5(random() :: text || clock_timestamp() :: text) :: uuid, 'SYSTEM', 'System', null,
    null, null, null, null, null,
    null, null, 'Role granted to system.', null, null, null, null, null, null,
    null, null, null)
ON CONFLICT DO NOTHING;

-- add role METRICS to role SU
INSERT INTO "sys_sec_Role#b6639604_includes" ("order", id, includes)
  SELECT
    (SELECT MAX("order") + 1
     from "sys_sec_Role#b6639604_includes"
     WHERE id = su.id),
    su.id,
    sys.id
  FROM
    "sys_sec_Role#b6639604" as su,
    "sys_sec_Role#b6639604" as sys
  WHERE su.name = 'SU' AND
        sys.name = 'METRICS' AND NOT EXISTS(SELECT *
                                                FROM "sys_sec_Role#b6639604_includes"
                                                WHERE id = su.id AND
                                                      includes = sys.id);