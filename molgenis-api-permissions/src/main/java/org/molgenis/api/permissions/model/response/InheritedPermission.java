package org.molgenis.api.permissions.model.response;

import com.google.auto.value.AutoValue;
import java.util.List;
import javax.annotation.Nullable;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_InheritedPermission.class)
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class InheritedPermission {
  @Nullable
  public abstract String getRole();

  @Nullable
  public abstract String getObjectId();

  @Nullable
  public abstract String getLabel();

  @Nullable
  public abstract String getTypeLabel();

  @Nullable
  public abstract String getTypeId();

  @Nullable
  public abstract String getPermission();

  @Nullable
  public abstract List<InheritedPermission> getInheritedPermissions();

  public static InheritedPermission create(
      String role,
      String typeId,
      String typeLabel,
      String identifier,
      String label,
      String permission,
      List<InheritedPermission> inheritedPermissions) {
    return builder()
        .setRole(role)
        .setTypeId(typeId)
        .setTypeLabel(typeLabel)
        .setObjectId(identifier)
        .setLabel(label)
        .setPermission(permission)
        .setInheritedPermissions(inheritedPermissions)
        .build();
  }

  public static Builder builder() {
    return new AutoValue_InheritedPermission.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setRole(String role);

    public abstract Builder setTypeId(String classId);

    public abstract Builder setTypeLabel(String classLabel);

    public abstract Builder setObjectId(String identifier);

    public abstract Builder setLabel(String label);

    public abstract Builder setPermission(String permissions);

    public abstract Builder setInheritedPermissions(List<InheritedPermission> permissions);

    public abstract InheritedPermission build();
  }
}
