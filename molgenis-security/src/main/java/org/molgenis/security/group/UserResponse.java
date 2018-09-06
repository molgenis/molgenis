package org.molgenis.security.group;

import com.google.auto.value.AutoValue;
import org.molgenis.data.security.auth.User;

@AutoValue
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class UserResponse {
  public abstract String getId();

  public abstract String getUsername();

  static UserResponse fromEntity(User user) {
    return new AutoValue_UserResponse(user.getId(), user.getUsername());
  }
}
