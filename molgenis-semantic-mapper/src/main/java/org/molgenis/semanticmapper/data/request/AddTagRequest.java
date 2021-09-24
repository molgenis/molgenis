package org.molgenis.semanticmapper.data.request;

import com.google.auto.value.AutoValue;
import java.util.List;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import org.molgenis.gson.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_AddTagRequest.class)
@SuppressWarnings("java:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class AddTagRequest {
  @NotNull
  public abstract String getEntityTypeId();

  @NotNull
  public abstract String getAttributeName();

  @NotNull
  public abstract String getRelationIRI();

  @NotEmpty
  public abstract List<String> getOntologyTermIRIs();
}
