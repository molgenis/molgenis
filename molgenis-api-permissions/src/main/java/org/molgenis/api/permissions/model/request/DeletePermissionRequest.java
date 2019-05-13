package org.molgenis.api.permissions.model.request;

import static com.google.common.base.Strings.isNullOrEmpty;

import com.google.auto.value.AutoValue;
import org.molgenis.api.permissions.exceptions.MissingUserOrRoleException;
import org.molgenis.api.permissions.exceptions.UserAndRoleException;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_DeletePermissionRequest.class)
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class DeletePermissionRequest {

  public abstract String getUser();

  public abstract String getRole();

  public static DeletePermissionRequest create(String user, String role) {
    if (isNullOrEmpty(user) && isNullOrEmpty(role)) {
      throw new MissingUserOrRoleException();
    } else if (!isNullOrEmpty(user) && !isNullOrEmpty(role)) {
      throw new UserAndRoleException();
    }
    return new AutoValue_DeletePermissionRequest(user, role);
  }
}
