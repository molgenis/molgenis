package org.molgenis.api.permissions.model.service;

import com.google.auto.value.AutoValue;
import java.util.Set;
import javax.annotation.Nullable;
import org.molgenis.util.AutoGson;
import org.springframework.security.acls.model.Sid;

@AutoValue
@AutoGson(autoValueClass = AutoValue_LabelledPermission.class)
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class LabelledPermission {
  @Nullable
  public abstract Sid getSid();

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
  public abstract Set<LabelledPermission> getLabelledPermissions();

  public static LabelledPermission create(
      Sid sid,
      String typeId,
      String typeLabel,
      String identifier,
      String label,
      String permission,
      Set<LabelledPermission> labelledPermissions) {
    return new AutoValue_LabelledPermission(
        sid, typeId, typeLabel, identifier, label, permission, labelledPermissions);
  }
}
