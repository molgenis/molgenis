package org.molgenis.semanticmapper.data.request;

import com.google.auto.value.AutoValue;
import java.util.List;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_GenerateAlgorithmRequest.class)
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class GenerateAlgorithmRequest {
  @NotNull
  public abstract String getTargetEntityTypeId();

  @NotNull
  public abstract String getTargetAttributeName();

  @NotNull
  public abstract String getSourceEntityTypeId();

  @NotEmpty
  public abstract List<String> getSourceAttributes();
}
