package org.molgenis.data.security.auth;

import static org.molgenis.data.security.auth.GroupMetadata.*;
import static org.molgenis.data.util.AttributeUtils.getI18nAttributeName;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.StaticEntity;

public class Group extends StaticEntity {
  public Group(Entity entity) {
    super(entity);
  }

  public Group(EntityType entityType) {
    super(entityType);
  }

  public Group(String id, EntityType entityType) {
    super(entityType);
    setId(id);
  }

  public void setId(String id) {
    set(ID, id);
  }

  public String getId() {
    return getString(ID);
  }

  public void setName(String name) {
    set(NAME, name);
  }

  public String getName() {
    return getString(NAME);
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

  public void setPublic(boolean isPublic) {
    set(PUBLIC, isPublic);
  }

  public boolean isPublic() {
    return getBoolean(PUBLIC);
  }

  public Iterable<Role> getRoles() {
    return getEntities(ROLES, Role.class);
  }

  public void setRoles(Iterable<Role> roles) {
    set(ROLES, roles);
  }

  public void setRootPackage(String rootPackage) {
    set(ROOT_PACKAGE, rootPackage);
  }

  public String getRootPackage() {
    return getString(ROOT_PACKAGE);
  }
}
