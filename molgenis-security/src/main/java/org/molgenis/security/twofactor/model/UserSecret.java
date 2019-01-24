package org.molgenis.security.twofactor.model;

import java.time.Instant;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.StaticEntity;

public class UserSecret extends StaticEntity {
  public UserSecret(Entity entity) {
    super(entity);
  }

  public UserSecret(EntityType entityType) {
    super(entityType);
  }

  public UserSecret(String id, EntityType entityType) {
    super(entityType);
    setId(id);
  }

  public String getId() {
    return getString(UserSecretMetadata.ID);
  }

  public void setId(String id) {
    set(UserSecretMetadata.ID, id);
  }

  public String getUserId() {
    return getString(UserSecretMetadata.USER_ID);
  }

  public void setUserId(String userId) {
    set(UserSecretMetadata.USER_ID, userId);
  }

  public String getSecret() {
    return getString(UserSecretMetadata.SECRET);
  }

  public void setSecret(String secret) {
    set(UserSecretMetadata.SECRET, secret);
  }

  public Instant getLastFailedAuthentication() {
    return getInstant(UserSecretMetadata.LAST_FAILED_AUTHENICATION);
  }

  public void setLastFailedAuthentication(Instant lastFailedAuthentication) {
    set(UserSecretMetadata.LAST_FAILED_AUTHENICATION, lastFailedAuthentication);
  }

  public int getFailedLoginAttempts() {
    return getInt(UserSecretMetadata.FAILED_LOGIN_ATTEMPTS);
  }

  public void setFailedLoginAttempts(int failedLoginAttempts) {
    set(UserSecretMetadata.FAILED_LOGIN_ATTEMPTS, failedLoginAttempts);
  }
}
