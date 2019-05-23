package org.molgenis.data.security.permission.model;

import com.google.auto.value.AutoValue;
import org.molgenis.util.AutoGson;
import org.springframework.security.acls.model.ObjectIdentity;

@AutoValue
@AutoGson(autoValueClass = AutoValue_LabelledObjectIdentity.class)
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class LabelledObjectIdentity implements ObjectIdentity {

  public abstract String getEntityTypeId();

  public abstract String getTypeLabel();

  public abstract String getIdentifierLabel();

  public static LabelledObjectIdentity create(
      String type,
      String entityTypeId,
      String typeLabel,
      String identifier,
      String identifierLabel) {
    return new AutoValue_LabelledObjectIdentity(
        identifier, type, entityTypeId, typeLabel, identifierLabel);
  }
}
