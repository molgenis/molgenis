package org.molgenis.api.permissions.model.service;

import com.google.auto.value.AutoValue;
import java.io.Serializable;
import org.molgenis.util.AutoGson;
import org.springframework.security.acls.model.ObjectIdentity;

@AutoValue
@AutoGson(autoValueClass = AutoValue_LabelledObjectIdentity.class)
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class LabelledObjectIdentity implements ObjectIdentity {

  public abstract String getType();

  public abstract String getTypeLabel();

  public abstract Serializable getIdentifier();

  public abstract String getIdentifierLabel();

  public static LabelledObjectIdentity create(
      String type, String typeLabel, Serializable identifier, String identifierLabel) {
    return new AutoValue_LabelledObjectIdentity(type, typeLabel, identifier, identifierLabel);
  }
}
