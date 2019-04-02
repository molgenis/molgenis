package org.molgenis.data.security.auth;

import static org.molgenis.data.security.auth.PasswordResetTokenMetadata.EXPIRATION_DATE;
import static org.molgenis.data.security.auth.PasswordResetTokenMetadata.ID;
import static org.molgenis.data.security.auth.PasswordResetTokenMetadata.TOKEN;
import static org.molgenis.data.security.auth.PasswordResetTokenMetadata.USER;

import java.time.Instant;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.StaticEntity;

public class PasswordResetToken extends StaticEntity {
  @SuppressWarnings("unused") // used via reflection
  public PasswordResetToken(Entity entity) {
    super(entity);
  }

  @SuppressWarnings("unused") // used via reflection
  public PasswordResetToken(EntityType entityType) {
    super(entityType);
  }

  @SuppressWarnings("unused") // used via reflection
  public PasswordResetToken(String id, EntityType entityType) {
    super(entityType);
    setId(id);
  }

  public String getId() {
    return getString(ID);
  }

  public void setId(String id) {
    set(ID, id);
  }

  public User getUser() {
    return getEntity(USER, User.class);
  }

  public void setUser(User user) {
    set(USER, user);
  }

  public String getToken() {
    return getString(TOKEN);
  }

  public void setToken(String token) {
    set(TOKEN, token);
  }

  public Instant getExpirationDate() {
    return getInstant(EXPIRATION_DATE);
  }

  public void setExpirationDate(Instant expirationDate) {
    set(EXPIRATION_DATE, expirationDate);
  }
}
