package org.molgenis.semanticmapper.data.request;

import com.google.auto.value.AutoValue;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_RemoveTagRequest.class)
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class RemoveTagRequest {
  @NotNull
  public abstract String getEntityTypeId();

  @NotNull
  public abstract String getAttributeName();

  @NotNull
  public abstract String getRelationIRI();

  @NotEmpty
  public abstract String getOntologyTermIRI();
}
