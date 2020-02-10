DO $$
    DECLARE
        rec   record;
        groupName                   varchar;
        roleGroupManagerId          varchar;
        roleGroupEditorId           varchar;
        roleGroupViewerId           varchar;
        roleManagerId               varchar;
        roleEditorId                varchar;
        roleViewerId                varchar;
        aclSidSystemId              bigint;
        aclSidUserId                bigint;
        aclSidGroupManagerId        bigint;
        aclSidGroupEditorId         bigint;
        aclSidGroupViewerId         bigint;
        aclClassGroupId             bigint;
        aclObjectIdentityGroupId    bigint;
        aclEntryPackageManagerId    bigint;
        aclEntryPackageEditorId     bigint;
        aclEntryPackageViewerId     bigint;
    BEGIN
        FOR rec IN
            SELECT "sys_md_Package#a6dc6fe7".id AS package_id ,"sys_md_Package#a6dc6fe7".label AS package_label
            FROM "sys_md_Package#a6dc6fe7"
                     LEFT JOIN "sys_sec_Group#d325f6e2" ON "sys_sec_Group#d325f6e2"."rootPackage" = "sys_md_Package#a6dc6fe7".id
            WHERE "sys_md_Package#a6dc6fe7".id != 'sys' AND "sys_md_Package#a6dc6fe7".parent IS NULL AND "sys_sec_Group#d325f6e2".id IS NULL
            LOOP
                SELECT id INTO roleManagerId FROM "sys_sec_Role#b6639604" WHERE name = 'MANAGER';
                SELECT id INTO roleEditorId FROM "sys_sec_Role#b6639604" WHERE name = 'EDITOR';
                SELECT id INTO roleViewerId FROM "sys_sec_Role#b6639604" WHERE name = 'VIEWER';
                SELECT id INTO aclClassGroupId FROM "acl_class" WHERE class = 'group';
                SELECT id INTO aclSidSystemId FROM "acl_sid" WHERE sid = 'ROLE_SYSTEM';
                SELECT id INTO aclSidUserId FROM "acl_sid" WHERE sid = 'ROLE_USER';
                roleGroupManagerId := rec.package_id || 'm';
                roleGroupEditorId := rec.package_id || 'e';
                roleGroupViewerId := rec.package_id || 'v';

                SELECT "sys_sec_Group#d325f6e2"."name" INTO groupName FROM "sys_sec_Group#d325f6e2" WHERE "sys_sec_Group#d325f6e2"."name" = rec.package_id;
                IF groupName IS NOT NULL THEN
                    groupName := 'group-' || LOWER(SUBSTRING(MD5(''||NOW()::TEXT||RANDOM()::TEXT) FOR 8));
                ELSE
                    groupName := rec.package_id;
                end if;

                INSERT INTO "sys_sec_Group#d325f6e2" ("id", "name", "label", "public", "rootPackage") VALUES (rec.package_id, groupName, rec.package_label, true, rec.package_id);

                INSERT INTO "sys_sec_Role#b6639604" ("id", "name", "label", "description", "group") VALUES (roleGroupManagerId, rec.package_id || '_MANAGER', 'Manager', rec.package_label || ' Manager', rec.package_id);
                INSERT INTO "sys_sec_Role#b6639604" ("id", "name", "label", "description", "group") VALUES (roleGroupEditorId, rec.package_id || '_EDITOR', 'Editor', rec.package_label || ' Editor', rec.package_id);
                INSERT INTO "sys_sec_Role#b6639604" ("id", "name", "label", "description", "group") VALUES (roleGroupViewerId, rec.package_id || '_VIEWER', 'Viewer', rec.package_label || ' Viewer', rec.package_id);
                INSERT INTO "sys_sec_Role#b6639604_includes" ("order","id","includes") VALUES (0,roleGroupManagerId,roleManagerId);
                INSERT INTO "sys_sec_Role#b6639604_includes" ("order","id","includes") VALUES (1,roleGroupManagerId,roleGroupEditorId);
                INSERT INTO "sys_sec_Role#b6639604_includes" ("order","id","includes") VALUES (0,roleGroupEditorId,roleEditorId);
                INSERT INTO "sys_sec_Role#b6639604_includes" ("order","id","includes") VALUES (1,roleGroupEditorId,roleGroupViewerId);
                INSERT INTO "sys_sec_Role#b6639604_includes" ("order","id","includes") VALUES (0,roleGroupViewerId,roleViewerId);

                INSERT INTO acl_sid (principal, sid) values (false, 'ROLE_' || rec.package_id || '_MANAGER') RETURNING id INTO aclSidGroupManagerId;
                INSERT INTO acl_sid (principal, sid) values (false, 'ROLE_' || rec.package_id || '_EDITOR') RETURNING id INTO aclSidGroupEditorId;
                INSERT INTO acl_sid (principal, sid) values (false, 'ROLE_' || rec.package_id || '_VIEWER') RETURNING id INTO aclSidGroupViewerId;

                INSERT INTO acl_object_identity (object_id_class, object_id_identity, owner_sid, entries_inheriting) values (aclClassGroupId, rec.package_id, aclSidSystemId, true) RETURNING id INTO aclObjectIdentityGroupId;

                INSERT INTO acl_entry (acl_object_identity, ace_order, sid, mask, granting, audit_success, audit_failure) VALUES (aclObjectIdentityGroupId, 0, aclSidGroupManagerId, 16, true, false, false) RETURNING id INTO aclEntryPackageManagerId;
                INSERT INTO acl_entry (acl_object_identity, ace_order, sid, mask, granting, audit_success, audit_failure) VALUES (aclObjectIdentityGroupId, 1, aclSidGroupEditorId, 8, true, false, false) RETURNING id INTO aclEntryPackageEditorId;
                INSERT INTO acl_entry (acl_object_identity, ace_order, sid, mask, granting, audit_success, audit_failure) VALUES (aclObjectIdentityGroupId, 2, aclSidGroupViewerId, 4, true, false, false) RETURNING id INTO aclEntryPackageViewerId;
                INSERT INTO acl_entry (acl_object_identity, ace_order, sid, mask, granting, audit_success, audit_failure) VALUES (aclObjectIdentityGroupId, 3, aclSidUserId, 4, true, false, false) RETURNING id INTO aclEntryPackageViewerId;
            END LOOP;
    END; $$