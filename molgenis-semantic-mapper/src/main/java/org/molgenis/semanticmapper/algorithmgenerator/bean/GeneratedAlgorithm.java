package org.molgenis.semanticmapper.algorithmgenerator.bean;

import com.google.auto.value.AutoValue;
import java.util.Set;
import javax.annotation.Nullable;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.semanticmapper.mapping.model.AttributeMapping.AlgorithmState;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_GeneratedAlgorithm.class)
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class GeneratedAlgorithm {
  public abstract String getAlgorithm();

  @Nullable
  public abstract Set<Attribute> getSourceAttributes();

  @Nullable
  public abstract AlgorithmState getAlgorithmState();

  public static GeneratedAlgorithm create(
      String algorithm, Set<Attribute> sourceAttributes, AlgorithmState algorithmState) {
    return new AutoValue_GeneratedAlgorithm(algorithm, sourceAttributes, algorithmState);
  }
}
