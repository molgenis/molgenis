ALTER TABLE acl_entry
    DROP CONSTRAINT foreign_fk_5,
    ADD CONSTRAINT foreign_fk_5
        FOREIGN KEY (sid)
            REFERENCES acl_sid (id)
            ON DELETE CASCADE;