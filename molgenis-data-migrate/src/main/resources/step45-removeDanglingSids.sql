DELETE
FROM acl_sid AS sids
WHERE id IN (SELECT id
             FROM acl_sid
             WHERE NOT EXISTS(SELECT *
                              FROM "sys_sec_Role#b6639604" as roles
                              WHERE CONCAT('ROLE_', roles.name) = sids.sid)
               AND NOT sids.principal);