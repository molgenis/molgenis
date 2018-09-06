package org.molgenis.semanticmapper.data.request;

import com.google.auto.value.AutoValue;
import java.util.List;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_AutoTagRequest.class)
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class AutoTagRequest {
  @NotBlank
  public abstract String getEntityTypeId();

  @NotEmpty
  public abstract List<String> getOntologyIds();
}
