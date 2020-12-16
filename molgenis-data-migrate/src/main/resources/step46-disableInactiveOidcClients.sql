DO $$
DECLARE
    settings public."sys_set_auth#98c4c015"%ROWTYPE;

BEGIN
    SELECT *
    INTO settings
    FROM "sys_set_auth#98c4c015"
    WHERE signup IS TRUE;

    IF FOUND THEN
        TRUNCATE public."sys_set_auth#98c4c015_oidcClients";
    END IF;
END $$