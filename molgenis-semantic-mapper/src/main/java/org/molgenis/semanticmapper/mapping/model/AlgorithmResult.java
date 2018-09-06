package org.molgenis.semanticmapper.mapping.model;

import com.google.auto.value.AutoValue;
import javax.annotation.Nullable;
import org.molgenis.data.Entity;

/** Result of applying algorithm to one source entity row */
@AutoValue
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class AlgorithmResult {
  @Nullable
  public abstract Object getValue();

  @Nullable
  public abstract Exception getException();

  public abstract Entity getSourceEntity();

  public boolean isSuccess() {
    return getException() == null;
  }

  public static AlgorithmResult createSuccess(Object object, Entity sourceEntity) {
    return new AutoValue_AlgorithmResult(object, null, sourceEntity);
  }

  public static AlgorithmResult createFailure(Exception e, Entity sourceEntity) {
    return new AutoValue_AlgorithmResult(null, e, sourceEntity);
  }
}
