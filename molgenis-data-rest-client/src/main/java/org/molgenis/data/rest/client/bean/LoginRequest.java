package org.molgenis.data.rest.client.bean;

import com.google.auto.value.AutoValue;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_LoginRequest.class)
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class LoginRequest {
  public static LoginRequest create(String username, String password) {
    return new AutoValue_LoginRequest(username, password);
  }

  public abstract String getUsername();

  public abstract String getPassword();
}
