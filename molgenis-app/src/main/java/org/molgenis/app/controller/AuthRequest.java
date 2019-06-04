package org.molgenis.app.controller;

import com.google.auto.value.AutoValue;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_AuthRequest.class)
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class AuthRequest {

  public abstract String getGrantType();

  public abstract String getUsername();

  public abstract String getPassword();

  public abstract String getClientId();

  public static AuthRequest create(
      String grantType, String username, String password, String clientId) {
    return new AutoValue_AuthRequest(grantType, username, password, clientId);
  }
}
