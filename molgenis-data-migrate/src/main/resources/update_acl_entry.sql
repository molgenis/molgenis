CREATE OR REPLACE FUNCTION update_acl_entry(role VARCHAR, object_identifier VARCHAR, new_mask INTEGER) RETURNS BOOLEAN AS $$
  DECLARE object_id INTEGER;
  DECLARE role_id INTEGER;
  DECLARE entry_id INTEGER;
  DECLARE current_ace_order INTEGER;
  DECLARE new_ace_order INTEGER;
  DECLARE current_mask INTEGER;
BEGIN
    SELECT id INTO object_id FROM acl_object_identity WHERE object_id_identity = object_identifier;
    SELECT id INTO role_id FROM acl_sid WHERE sid = role;
    SELECT MAX(ace_order) INTO current_ace_order FROM acl_entry WHERE acl_object_identity = object_id;
    SELECT id, mask INTO entry_id, current_mask FROM acl_entry WHERE sid = role_id AND acl_object_identity = object_id;

    IF current_ace_order IS NULL THEN
      new_ace_order = 0;
    ELSE
      new_ace_order = current_ace_order + 1;
    END IF;

    IF entry_id IS NULL THEN
      INSERT INTO acl_entry (acl_object_identity, sid, mask, ace_order, granting, audit_success, audit_failure) VALUES (object_Id,role_id,new_mask,new_ace_order, TRUE, FALSE, FALSE);
    ELSE
      IF current_mask < new_mask THEN
        UPDATE acl_entry SET mask = new_mask WHERE id = entry_id;
      END IF;
    END IF;
  RETURN TRUE;
END;
$$ LANGUAGE plpgsql