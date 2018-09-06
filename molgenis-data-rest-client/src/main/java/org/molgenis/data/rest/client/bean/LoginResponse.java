package org.molgenis.data.rest.client.bean;

import com.google.auto.value.AutoValue;
import javax.annotation.Nullable;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_LoginResponse.class)
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class LoginResponse {
  public static LoginResponse create(
      String token, String username, String firstname, String lastname) {
    return new AutoValue_LoginResponse(token, username, firstname, lastname);
  }

  public abstract String getToken();

  public abstract String getUsername();

  @Nullable
  public abstract String getFirstname();

  @Nullable
  public abstract String getLastname();
}
