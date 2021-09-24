package org.molgenis.semanticmapper.data.request;

import com.google.auto.value.AutoValue;
import java.util.List;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import org.molgenis.gson.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_GetOntologyTermRequest.class)
@SuppressWarnings("java:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class GetOntologyTermRequest {
  @NotBlank
  public abstract String getSearchTerm();

  @NotEmpty
  public abstract List<String> getOntologyIds();
}
