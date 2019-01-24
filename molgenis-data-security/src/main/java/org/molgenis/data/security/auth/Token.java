package org.molgenis.data.security.auth;

import static java.time.Instant.now;
import static org.molgenis.data.security.auth.TokenMetadata.CREATIONDATE;
import static org.molgenis.data.security.auth.TokenMetadata.DESCRIPTION;
import static org.molgenis.data.security.auth.TokenMetadata.EXPIRATIONDATE;
import static org.molgenis.data.security.auth.TokenMetadata.ID;
import static org.molgenis.data.security.auth.TokenMetadata.TOKEN_ATTR;
import static org.molgenis.data.security.auth.TokenMetadata.USER;

import java.time.Instant;
import java.util.Optional;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.StaticEntity;

public class Token extends StaticEntity {
  public Token(Entity entity) {
    super(entity);
  }

  public Token(EntityType entityType) {
    super(entityType);
  }

  public Token(String id, EntityType entityType) {
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
    return getString(TOKEN_ATTR);
  }

  public void setToken(String token) {
    set(TOKEN_ATTR, token);
  }

  public Optional<Instant> getExpirationDate() {
    return Optional.ofNullable(getInstant(EXPIRATIONDATE));
  }

  public void setExpirationDate(Instant expirationDate) {
    set(EXPIRATIONDATE, expirationDate);
  }

  public Instant getCreationDate() {
    return getInstant(CREATIONDATE);
  }

  public void setCreationDate(Instant creationDate) {
    set(CREATIONDATE, creationDate);
  }

  @Nullable
  @CheckForNull
  public String getDescription() {
    return getString(DESCRIPTION);
  }

  public void setDescription(String description) {
    set(DESCRIPTION, description);
  }

  public boolean isExpired() {
    Optional<Instant> expirationDate = getExpirationDate();
    return expirationDate.isPresent() && expirationDate.get().isBefore(now());
  }
}
