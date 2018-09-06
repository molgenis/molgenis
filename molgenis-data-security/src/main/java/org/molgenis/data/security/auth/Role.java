package org.molgenis.data.security.auth;

import static org.molgenis.data.security.auth.RoleMetadata.*;
import static org.molgenis.data.util.AttributeUtils.getI18nAttributeName;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.StaticEntity;

public class Role extends StaticEntity {
  public Role(Entity entity) {
    super(entity);
  }

  public Role(EntityType entityType) {
    super(entityType);
  }

  public Role(String id, EntityType entityType) {
    super(entityType);
    setId(id);
  }

  public String getId() {
    return getString(ID);
  }

  public void setId(String id) {
    set(ID, id);
  }

  public String getName() {
    return getString(NAME);
  }

  public void setName(String name) {
    set(NAME, name);
  }

  public String getLabel() {
    return getString(LABEL);
  }

  public String getLabel(String languageCode) {
    return getString(getI18nAttributeName(LABEL, languageCode));
  }

  public void setLabel(String label) {
    set(LABEL, label);
  }

  public void setLabel(String languageCode, String label) {
    set(getI18nAttributeName(LABEL, languageCode), label);
  }

  public String getDescription() {
    return getString(DESCRIPTION);
  }

  public String getDescription(String languageCode) {
    return getString(getI18nAttributeName(DESCRIPTION, languageCode));
  }

  public void setDescription(String description) {
    set(DESCRIPTION, description);
  }

  public void setDescription(String languageCode, String label) {
    set(getI18nAttributeName(DESCRIPTION, languageCode), label);
  }

  public void setGroup(Group group) {
    set(GROUP, group);
  }

  public Group getGroup() {
    return getEntity(GROUP, Group.class);
  }

  public Iterable<Role> getIncludes() {
    return getEntities(INCLUDES, Role.class);
  }

  public void setIncludes(Iterable<Role> roles) {
    set(INCLUDES, roles);
  }
}
