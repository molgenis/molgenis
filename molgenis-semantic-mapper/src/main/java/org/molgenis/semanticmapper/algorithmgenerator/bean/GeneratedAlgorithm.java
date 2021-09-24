package org.molgenis.semanticmapper.algorithmgenerator.bean;

import com.google.auto.value.AutoValue;
import java.util.Set;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.gson.AutoGson;
import org.molgenis.semanticmapper.mapping.model.AttributeMapping.AlgorithmState;

@AutoValue
@AutoGson(autoValueClass = AutoValue_GeneratedAlgorithm.class)
@SuppressWarnings("java:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class GeneratedAlgorithm {
  public abstract String getAlgorithm();

  @Nullable
  @CheckForNull
  public abstract Set<Attribute> getSourceAttributes();

  @Nullable
  @CheckForNull
  public abstract AlgorithmState getAlgorithmState();

  public static GeneratedAlgorithm create(
      String algorithm, Set<Attribute> sourceAttributes, AlgorithmState algorithmState) {
    return new AutoValue_GeneratedAlgorithm(algorithm, sourceAttributes, algorithmState);
  }
}
