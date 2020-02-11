package org.molgenis.security.core;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class SecurityContextUpdateAction {
  public enum Type {
    ADD,
    DELETE
  }

  public abstract String getUsername();

  public abstract String getRoleName();

  public abstract Type getType();

  public static SecurityContextUpdateAction create(
      String newUsername, String newRoleName, Type newType) {
    return builder().setUsername(newUsername).setRoleName(newRoleName).setType(newType).build();
  }

  public static Builder builder() {
    return new AutoValue_SecurityContextUpdateAction.Builder();
  }

  @SuppressWarnings(
      "squid:S1610") // Abstract classes without fields should be converted to interfaces
  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setUsername(String newUsername);

    public abstract Builder setRoleName(String newRoleName);

    public abstract Builder setType(Type newType);

    public abstract SecurityContextUpdateAction build();
  }
}
