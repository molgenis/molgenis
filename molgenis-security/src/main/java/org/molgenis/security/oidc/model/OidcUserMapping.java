package org.molgenis.security.oidc.model;

import static org.molgenis.security.oidc.model.OidcUserMappingMetadata.ID;
import static org.molgenis.security.oidc.model.OidcUserMappingMetadata.LABEL;
import static org.molgenis.security.oidc.model.OidcUserMappingMetadata.OIDC_CLIENT;
import static org.molgenis.security.oidc.model.OidcUserMappingMetadata.OIDC_USERNAME;
import static org.molgenis.security.oidc.model.OidcUserMappingMetadata.USER;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.security.auth.User;
import org.molgenis.data.support.StaticEntity;

public class OidcUserMapping extends StaticEntity {
  @SuppressWarnings("unused")
  public OidcUserMapping(Entity entity) {
    super(entity);
  }

  @SuppressWarnings("unused")
  public OidcUserMapping(EntityType entityType) {
    super(entityType);
  }

  @SuppressWarnings("unused")
  public OidcUserMapping(String id, EntityType entityType) {
    super(entityType);
    setId(id);
  }

  public void setId(String id) {
    set(ID, id);
  }

  public String getId() {
    return getString(ID);
  }

  public void setLabel(String label) {
    set(LABEL, label);
  }

  public String getLabel() {
    return getString(LABEL);
  }

  public void setOidcClient(OidcClient oidcClient) {
    set(OIDC_CLIENT, oidcClient);
  }

  @SuppressWarnings("unused")
  public OidcClient getOidcClient() {
    return getEntity(OIDC_CLIENT, OidcClient.class);
  }

  public void setOidcUsername(String oidcUsername) {
    set(OIDC_USERNAME, oidcUsername);
  }

  @SuppressWarnings("unused")
  public String getOidcUsername() {
    return getString(OIDC_USERNAME);
  }

  public void setUser(User user) {
    set(USER, user);
  }

  public User getUser() {
    return getEntity(USER, User.class);
  }
}
